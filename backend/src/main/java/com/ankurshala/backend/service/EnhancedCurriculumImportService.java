package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced service for importing structured curriculum data from XLSX files
 * Supports hierarchical taxonomy: Board -> Grade -> Subject -> Chapter -> Topics
 * Handles topic relationships (prerequisites and related topics)
 */
@Service
public class EnhancedCurriculumImportService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedCurriculumImportService.class);

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicLinkRepository topicLinkRepository;

    @Autowired
    private UserRepository userRepository;

    // Expected column headers in XLSX
    private static final String COL_BOARD = "Board";
    private static final String COL_GRADE = "Grade";
    private static final String COL_SUBJECT = "Subject";
    private static final String COL_CHAPTER = "Chapter";
    private static final String COL_CHAPTERS = "Chapters";  // Alternative column name
    private static final String COL_TOPICS = "Topics";
    private static final String COL_RELATED_TOPICS = "Related Topics";
    private static final String COL_BRIEF_DESCRIPTION = "Brief Description";
    private static final String COL_DURATION = "Duration";
    private static final String COL_SUGGESTED_TOPICS = "Suggested Topics";

    /**
     * Create a new import job
     */
    @Transactional
    public ImportJob createImportJob(String fileName, Long fileSize, Long userId) {
        String fileType = fileName.toLowerCase().endsWith(".csv") ? "CSV" : "XLSX";
        ImportJob importJob = new ImportJob(fileName, fileType, fileSize);
        importJob.setType("CURRICULUM_" + fileType);
        importJob.setStatus(ImportJobStatus.PENDING);
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            importJob.setCreatedBy(user);
        }
        
        return importJobRepository.save(importJob);
    }

    /**
     * Get import job by ID
     */
    public ImportJob getImportJobById(Long jobId) {
        return importJobRepository.findById(jobId).orElse(null);
    }

    /**
     * Process curriculum file asynchronously (supports both CSV and XLSX)
     */
    @Async
    public CompletableFuture<ImportJob> processFileAsync(ImportJob importJob, byte[] fileBytes, String fileName, boolean dryRun) {
        try {
            importJob.setStatus(ImportJobStatus.RUNNING);
            importJob.setStartedAt(LocalDateTime.now());
            importJobRepository.save(importJob);

            List<CurriculumRow> rows;
            if (fileName.toLowerCase().endsWith(".csv")) {
                rows = parseCsvFile(fileBytes, fileName, importJob);
            } else {
                rows = parseXlsxFile(fileBytes, fileName, importJob);
            }
            
            if (!dryRun && !rows.isEmpty()) {
                processAndSaveCurriculum(rows, importJob);
            }
            
            importJob.setStatus(ImportJobStatus.SUCCEEDED);
            importJob.setCompletedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            importJob.setStatus(ImportJobStatus.FAILED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJob.setErrorMessage(e.getMessage());
            logger.error("Curriculum import failed: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(importJobRepository.save(importJob));
    }

    /**
     * Parse CSV file and extract curriculum data
     */
    private List<CurriculumRow> parseCsvFile(byte[] fileBytes, String fileName, ImportJob importJob) throws IOException {
        List<CurriculumRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            // Get header map
            Map<String, Integer> headerMap = parser.getHeaderMap();
            Map<String, Integer> columnMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                columnMap.put(entry.getKey(), entry.getValue());
            }
            
            validateColumns(columnMap);
            
            List<CSVRecord> csvRecords = parser.getRecords();
            int successCount = 0;
            int errorCount = 0;
            
            // Process data rows
            for (int i = 0; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);
                try {
                    CurriculumRow curriculumRow = parseCsvRecord(record, columnMap, i + 2); // +2 because header is row 1
                    rows.add(curriculumRow);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    String error = String.format("Row %d: %s", i + 2, e.getMessage());
                    errors.add(error);
                    logger.warn(error);
                    
                    if (errors.size() > 100) {
                        errors.add("... and more errors (limit reached)");
                        break;
                    }
                }
            }
            
            importJob.setTotalRows(successCount + errorCount);
            importJob.setSuccessRows(successCount);
            importJob.setErrorRows(errorCount);
            
            if (!errors.isEmpty()) {
                importJob.setErrors(convertErrorsToJson(errors));
            }
        }
        
        return rows;
    }
    
    /**
     * Parse a CSV record into CurriculumRow object
     */
    private CurriculumRow parseCsvRecord(CSVRecord record, Map<String, Integer> columnMap, int rowNumber) {
        String board = getCsvFieldValue(record, COL_BOARD);
        String grade = getCsvFieldValue(record, COL_GRADE);
        String subject = getCsvFieldValue(record, COL_SUBJECT);
        
        // Try both "Chapter" and "Chapters"
        String chapter = getCsvFieldValue(record, COL_CHAPTER);
        if (chapter == null || chapter.trim().isEmpty()) {
            chapter = getCsvFieldValue(record, COL_CHAPTERS);
        }
        
        String topics = getCsvFieldValue(record, COL_TOPICS);
        String relatedTopics = getCsvFieldValue(record, COL_RELATED_TOPICS);
        String briefDescription = getCsvFieldValue(record, COL_BRIEF_DESCRIPTION);
        String duration = getCsvFieldValue(record, COL_DURATION);
        String suggestedTopics = getCsvFieldValue(record, COL_SUGGESTED_TOPICS);
        
        // Validate required fields
        if (isBlank(board)) throw new IllegalArgumentException("Board is required");
        if (isBlank(grade)) throw new IllegalArgumentException("Grade is required");
        if (isBlank(subject)) throw new IllegalArgumentException("Subject is required");
        if (isBlank(chapter)) throw new IllegalArgumentException("Chapter is required");
        if (isBlank(topics)) throw new IllegalArgumentException("Topics is required");
        
        // Parse topics (comma-separated or single topic)
        List<String> topicList = parseTopicsList(topics);
        if (topicList.isEmpty()) {
            throw new IllegalArgumentException("At least one topic must be provided");
        }
        
        // Parse related topics
        List<RelatedTopicLink> relatedTopicLinks = parseRelatedTopics(relatedTopics);
        
        // Parse duration to minutes
        Integer durationMinutes = parseDurationToMinutes(duration);
        
        // Build description from brief description and suggested topics
        String description = buildDescription(briefDescription, suggestedTopics);
        
        return new CurriculumRow(
            board.trim(),
            normalizeGrade(grade),
            subject.trim(),
            chapter.trim(),
            topicList,
            relatedTopicLinks,
            description,
            durationMinutes,
            suggestedTopics
        );
    }
    
    /**
     * Get CSV field value (case-insensitive)
     */
    private String getCsvFieldValue(CSVRecord record, String fieldName) {
        try {
            // Try exact match first
            if (record.isSet(fieldName)) {
                String value = record.get(fieldName);
                return (value != null && !value.trim().isEmpty()) ? value : null;
            }
            
            // Try case-insensitive match by checking all headers
            Map<String, Integer> headerMap = record.getParser().getHeaderMap();
            for (String header : headerMap.keySet()) {
                if (header.equalsIgnoreCase(fieldName)) {
                    String value = record.get(header);
                    return (value != null && !value.trim().isEmpty()) ? value : null;
                }
            }
            
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parse XLSX file and extract curriculum data
     */
    private List<CurriculumRow> parseXlsxFile(byte[] fileBytes, String fileName, ImportJob importJob) throws IOException {
        List<CurriculumRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Empty worksheet - no header row found");
            }
            
            Map<String, Integer> columnMap = mapColumns(headerRow);
            validateColumns(columnMap);
            
            int totalRows = sheet.getLastRowNum();
            int successCount = 0;
            int errorCount = 0;
            
            // Process data rows
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                
                try {
                    CurriculumRow curriculumRow = parseRow(row, columnMap, i + 1);
                    rows.add(curriculumRow);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    String error = String.format("Row %d: %s", i + 1, e.getMessage());
                    errors.add(error);
                    logger.warn(error);
                    
                    if (errors.size() > 100) {
                        errors.add("... and more errors (limit reached)");
                        break;
                    }
                }
            }
            
            importJob.setTotalRows(successCount + errorCount);
            importJob.setSuccessRows(successCount);
            importJob.setErrorRows(errorCount);
            
            if (!errors.isEmpty()) {
                importJob.setErrors(convertErrorsToJson(errors));
            }
        }
        
        return rows;
    }

    /**
     * Map column names to their indices
     */
    private Map<String, Integer> mapColumns(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = cell.getStringCellValue().trim();
                columnMap.put(columnName, i);
            }
        }
        
        return columnMap;
    }

    /**
     * Validate required columns are present
     */
    private void validateColumns(Map<String, Integer> columnMap) {
        // Required columns: Board, Grade, Subject, Chapter/Chapters, Topics
        List<String> missingColumns = new ArrayList<>();
        
        if (!columnMap.containsKey(COL_BOARD)) {
            missingColumns.add(COL_BOARD);
        }
        if (!columnMap.containsKey(COL_GRADE)) {
            missingColumns.add(COL_GRADE);
        }
        if (!columnMap.containsKey(COL_SUBJECT)) {
            missingColumns.add(COL_SUBJECT);
        }
        // Check for either "Chapter" or "Chapters"
        if (!columnMap.containsKey(COL_CHAPTER) && !columnMap.containsKey(COL_CHAPTERS)) {
            missingColumns.add(COL_CHAPTER + " or " + COL_CHAPTERS);
        }
        if (!columnMap.containsKey(COL_TOPICS)) {
            missingColumns.add(COL_TOPICS);
        }
        
        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException("Missing required columns: " + String.join(", ", missingColumns));
        }
    }
    
    /**
     * Get chapter column name (supports both "Chapter" and "Chapters")
     */
    private String getChapterColumnName(Map<String, Integer> columnMap) {
        if (columnMap.containsKey(COL_CHAPTER)) {
            return COL_CHAPTER;
        } else if (columnMap.containsKey(COL_CHAPTERS)) {
            return COL_CHAPTERS;
        }
        return COL_CHAPTER; // Default fallback
    }

    /**
     * Parse a single row into CurriculumRow object
     */
    private CurriculumRow parseRow(Row row, Map<String, Integer> columnMap, int rowNumber) {
        String board = getCellValue(row, columnMap, COL_BOARD);
        String grade = getCellValue(row, columnMap, COL_GRADE);
        String subject = getCellValue(row, columnMap, COL_SUBJECT);
        String chapterColumnName = getChapterColumnName(columnMap);
        String chapter = getCellValue(row, columnMap, chapterColumnName);
        String topics = getCellValue(row, columnMap, COL_TOPICS);
        String relatedTopics = getCellValue(row, columnMap, COL_RELATED_TOPICS);
        String briefDescription = getCellValue(row, columnMap, COL_BRIEF_DESCRIPTION);
        String duration = getCellValue(row, columnMap, COL_DURATION);
        String suggestedTopics = getCellValue(row, columnMap, COL_SUGGESTED_TOPICS);
        
        // Validate required fields
        if (isBlank(board)) throw new IllegalArgumentException("Board is required");
        if (isBlank(grade)) throw new IllegalArgumentException("Grade is required");
        if (isBlank(subject)) throw new IllegalArgumentException("Subject is required");
        if (isBlank(chapter)) throw new IllegalArgumentException("Chapter is required");
        if (isBlank(topics)) throw new IllegalArgumentException("Topics is required");
        
        // Parse topics (comma-separated or single topic)
        List<String> topicList = parseTopicsList(topics);
        if (topicList.isEmpty()) {
            throw new IllegalArgumentException("At least one topic must be provided");
        }
        
        // Parse related topics
        List<RelatedTopicLink> relatedTopicLinks = parseRelatedTopics(relatedTopics);
        
        // Parse duration to minutes
        Integer durationMinutes = parseDurationToMinutes(duration);
        
        // Build description from brief description and suggested topics
        String description = buildDescription(briefDescription, suggestedTopics);
        
        return new CurriculumRow(
            board.trim(),
            normalizeGrade(grade),
            subject.trim(),
            chapter.trim(),
            topicList,
            relatedTopicLinks,
            description,
            durationMinutes,
            suggestedTopics
        );
    }
    
    /**
     * Parse duration string to minutes (e.g., "2 hrs" -> 120, "1.5 hrs" -> 90)
     */
    private Integer parseDurationToMinutes(String durationStr) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            String normalized = durationStr.trim().toLowerCase();
            // Match patterns like "2 hrs", "1.5 hrs", "2 hours", "90 mins", etc.
            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(hrs?|hours?|mins?|minutes?)");
            Matcher matcher = pattern.matcher(normalized);
            
            if (matcher.find()) {
                double value = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2);
                
                if (unit.startsWith("hr") || unit.startsWith("hour")) {
                    return (int) (value * 60);
                } else if (unit.startsWith("min") || unit.startsWith("minute")) {
                    return (int) value;
                }
            }
            
            // Try parsing as plain number (assume minutes)
            return Integer.parseInt(durationStr.trim());
        } catch (Exception e) {
            logger.warn("Could not parse duration: {}", durationStr);
            return null;
        }
    }
    
    /**
     * Build description from brief description and suggested topics
     */
    private String buildDescription(String briefDescription, String suggestedTopics) {
        StringBuilder desc = new StringBuilder();
        
        if (briefDescription != null && !briefDescription.trim().isEmpty()) {
            desc.append(briefDescription.trim());
        }
        
        if (suggestedTopics != null && !suggestedTopics.trim().isEmpty()) {
            if (desc.length() > 0) {
                desc.append("\n\n");
            }
            desc.append("Suggested Topics: ").append(suggestedTopics.trim());
        }
        
        return desc.length() > 0 ? desc.toString() : null;
    }

    /**
     * Get cell value as string
     */
    private String getCellValue(Row row, Map<String, Integer> columnMap, String columnName) {
        Integer colIndex = columnMap.get(columnName);
        if (colIndex == null) return null;
        
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Check if row is empty
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Parse comma-separated topics list
     */
    private List<String> parseTopicsList(String topicsStr) {
        if (topicsStr == null || topicsStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> topics = new ArrayList<>();
        String[] parts = topicsStr.split(",");
        
        for (String part : parts) {
            String topic = part.trim();
            if (!topic.isEmpty()) {
                topics.add(topic);
            }
        }
        
        return topics;
    }

    /**
     * Parse related topics from format: "Class X Subject - Topic; Class Y Subject - Topic"
     */
    private List<RelatedTopicLink> parseRelatedTopics(String relatedTopicsStr) {
        List<RelatedTopicLink> links = new ArrayList<>();
        
        if (relatedTopicsStr == null || relatedTopicsStr.trim().isEmpty()) {
            return links;
        }
        
        // Pattern to match: "Class 6 Maths - Topic name" or "Grade 8 Science - Topic name"
        Pattern pattern = Pattern.compile("(Class|Grade)\\s+(\\d+)\\s+([^-]+)\\s*-\\s*([^;]+)");
        
        String[] parts = relatedTopicsStr.split(";");
        for (String part : parts) {
            Matcher matcher = pattern.matcher(part.trim());
            if (matcher.find()) {
                String grade = matcher.group(2);
                String subject = matcher.group(3).trim();
                String topicHint = matcher.group(4).trim();
                
                links.add(new RelatedTopicLink(normalizeGrade(grade), subject, topicHint));
            }
        }
        
        return links;
    }

    /**
     * Normalize grade format (7, Grade 7, GRADE_7 -> "7")
     */
    private String normalizeGrade(String grade) {
        if (grade == null) return null;
        
        String normalized = grade.trim().toUpperCase()
            .replace("GRADE", "")
            .replace("CLASS", "")
            .replace("_", "")
            .trim();
        
        return normalized;
    }

    /**
     * Check if string is blank
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Convert errors list to JSON array string
     */
    private String convertErrorsToJson(List<String> errors) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"")
                .append(errors.get(i).replace("\"", "\\\"").replace("\n", "\\n"))
                .append("\"");
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Process curriculum rows and save to database
     */
    @Transactional
    public void processAndSaveCurriculum(List<CurriculumRow> rows, ImportJob importJob) {
        logger.info("Processing {} curriculum rows", rows.size());
        
        // Group rows by Board -> Grade -> Subject -> Chapter
        Map<String, Map<String, Map<String, Map<String, List<CurriculumRow>>>>> grouped = groupRows(rows);
        
        int topicsCreated = 0;
        int linksCreated = 0;
        
        for (Map.Entry<String, Map<String, Map<String, Map<String, List<CurriculumRow>>>>> boardEntry : grouped.entrySet()) {
            String boardName = boardEntry.getKey();
            Board board = getOrCreateBoard(boardName);
            
            for (Map.Entry<String, Map<String, Map<String, List<CurriculumRow>>>> gradeEntry : boardEntry.getValue().entrySet()) {
                String gradeName = gradeEntry.getKey();
                Grade grade = getOrCreateGrade(gradeName, board);
                
                for (Map.Entry<String, Map<String, List<CurriculumRow>>> subjectEntry : gradeEntry.getValue().entrySet()) {
                    String subjectName = subjectEntry.getKey();
                    Subject subject = getOrCreateSubject(subjectName, board, grade);
                    
                    for (Map.Entry<String, List<CurriculumRow>> chapterEntry : subjectEntry.getValue().entrySet()) {
                        String chapterName = chapterEntry.getKey();
                        Chapter chapter = getOrCreateChapter(chapterName, subject, board);
                        
                        // Create topics for this chapter
                        List<CurriculumRow> chapterRows = chapterEntry.getValue();
                        for (CurriculumRow currRow : chapterRows) {
                            for (String topicTitle : currRow.topics) {
                                Topic topic = getOrCreateTopic(topicTitle, chapter, subject, board,
                                    currRow.description, currRow.durationMinutes);
                                topicsCreated++;
                                
                                // Store related topic links for later processing
                                for (RelatedTopicLink link : currRow.relatedTopicLinks) {
                                    // We'll process these in a second pass after all topics are created
                                    // For now, just log them
                                    logger.debug("Related topic link: {} -> {}/{}/{}",
                                        topic.getTitle(), link.grade, link.subject, link.topicHint);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("Created {} topics, {} topic links", topicsCreated, linksCreated);
        
        // Update import job stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("topicsCreated", topicsCreated);
        stats.put("linksCreated", linksCreated);
        stats.put("boards", grouped.size());
        
        try {
            // Convert stats to JSON string manually
            StringBuilder statsJson = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                if (!first) statsJson.append(",");
                statsJson.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    statsJson.append("\"").append(entry.getValue()).append("\"");
                } else {
                    statsJson.append(entry.getValue());
                }
                first = false;
            }
            statsJson.append("}");
            
            importJob.setStats(statsJson.toString());
        } catch (Exception e) {
            logger.warn("Failed to set stats: {}", e.getMessage());
        }
    }

    /**
     * Group rows hierarchically
     */
    private Map<String, Map<String, Map<String, Map<String, List<CurriculumRow>>>>> groupRows(List<CurriculumRow> rows) {
        Map<String, Map<String, Map<String, Map<String, List<CurriculumRow>>>>> grouped = new HashMap<>();
        
        for (CurriculumRow row : rows) {
            grouped.computeIfAbsent(row.board, k -> new HashMap<>())
                .computeIfAbsent(row.grade, k -> new HashMap<>())
                .computeIfAbsent(row.subject, k -> new HashMap<>())
                .computeIfAbsent(row.chapter, k -> new ArrayList<>())
                .add(row);
        }
        
        return grouped;
    }

    /**
     * Get or create Board entity
     */
    private Board getOrCreateBoard(String name) {
        return boardRepository.findByName(name)
            .orElseGet(() -> {
                Board board = new Board();
                board.setName(name);
                board.setActive(true);
                return boardRepository.save(board);
            });
    }

    /**
     * Get or create Grade entity
     */
    private Grade getOrCreateGrade(String name, Board board) {
        return gradeRepository.findByBoardIdAndName(board.getId(), name)
            .orElseGet(() -> {
                Grade grade = new Grade();
                grade.setName(name);
                grade.setDisplayName("Grade " + name);
                grade.setBoardId(board.getId());
                grade.setActive(true);
                return gradeRepository.save(grade);
            });
    }

    /**
     * Get or create Subject entity
     */
    private Subject getOrCreateSubject(String name, Board board, Grade grade) {
        return subjectRepository.findByGradeIdAndName(grade.getId(), name)
            .orElseGet(() -> {
                Subject subject = new Subject();
                subject.setName(name);
                subject.setBoardId(board.getId());
                subject.setGradeId(grade.getId());
                subject.setActive(true);
                return subjectRepository.save(subject);
            });
    }

    /**
     * Get or create Chapter entity
     */
    private Chapter getOrCreateChapter(String name, Subject subject, Board board) {
        return chapterRepository.findByNameAndSubjectId(name, subject.getId())
            .orElseGet(() -> {
                Chapter chapter = new Chapter();
                chapter.setName(name);
                chapter.setSubjectId(subject.getId());
                chapter.setBoardId(board.getId());
                chapter.setGradeId(subject.getGradeId());
                chapter.setActive(true);
                return chapterRepository.save(chapter);
            });
    }

    /**
     * Get or create Topic entity
     */
    private Topic getOrCreateTopic(String title, Chapter chapter, Subject subject, Board board) {
        return getOrCreateTopic(title, chapter, subject, board, null, null);
    }
    
    private Topic getOrCreateTopic(String title, Chapter chapter, Subject subject, Board board,
                                   String description, Integer durationMinutes) {
        return topicRepository.findByTitleAndChapterId(title, chapter.getId())
            .map(existingTopic -> {
                // Update existing topic with new information if provided
                boolean updated = false;
                if (description != null && !description.trim().isEmpty() && 
                    (existingTopic.getDescription() == null || existingTopic.getDescription().trim().isEmpty())) {
                    existingTopic.setDescription(description);
                    updated = true;
                }
                if (durationMinutes != null && existingTopic.getExpectedTimeMins() == null) {
                    existingTopic.setExpectedTimeMins(durationMinutes);
                    updated = true;
                }
                if (updated) {
                    return topicRepository.save(existingTopic);
                }
                return existingTopic;
            })
            .orElseGet(() -> {
                Topic topic = new Topic();
                topic.setTitle(title);
                topic.setChapterId(chapter.getId());
                topic.setSubjectId(subject.getId());
                topic.setBoardId(board.getId());
                topic.setGradeId(chapter.getGradeId());
                topic.setActive(true);
                
                // Generate topic code
                String code = generateTopicCode(subject.getName(), chapter.getName(), title);
                topic.setCode(code);
                
                // Set description if provided
                if (description != null && !description.trim().isEmpty()) {
                    topic.setDescription(description);
                }
                
                // Set expected time: use provided duration or default to 60 minutes
                topic.setExpectedTimeMins(durationMinutes != null ? durationMinutes : 60);
                
                return topicRepository.save(topic);
            });
    }

    /**
     * Generate unique topic code
     */
    private String generateTopicCode(String subject, String chapter, String title) {
        String subjectCode = getSubjectPrefix(subject);
        String chapterCode = getPrefix(chapter, 3);
        String topicCode = getPrefix(title, 5);
        
        String baseCode = String.format("%s_%s_%s", subjectCode, chapterCode, topicCode).toUpperCase();
        
        // Ensure uniqueness
        String uniqueCode = baseCode;
        int counter = 1;
        while (topicRepository.findByCode(uniqueCode).isPresent()) {
            uniqueCode = baseCode + "_" + counter;
            counter++;
        }
        
        return uniqueCode;
    }

    /**
     * Get subject prefix for code generation
     */
    private String getSubjectPrefix(String subject) {
        if (subject == null) return "SUB";
        
        String normalized = subject.toUpperCase().trim();
        if (normalized.contains("MATH")) return "MATH";
        if (normalized.contains("PHYSIC")) return "PHY";
        if (normalized.contains("CHEM")) return "CHEM";
        if (normalized.contains("BIO")) return "BIO";
        if (normalized.contains("ENG")) return "ENG";
        if (normalized.contains("HIST")) return "HIST";
        if (normalized.contains("GEO")) return "GEO";
        if (normalized.contains("SOCIAL")) return "SOC";
        if (normalized.contains("ECON")) return "ECO";
        if (normalized.contains("POLIT")) return "POL";
        if (normalized.contains("COMP")) return "CS";
        
        return getPrefix(subject, 3);
    }

    /**
     * Get prefix from string
     */
    private String getPrefix(String str, int length) {
        if (str == null || str.isEmpty()) return "XXX";
        
        String cleaned = str.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) cleaned = str;
        
        return cleaned.length() >= length 
            ? cleaned.substring(0, length).toUpperCase() 
            : cleaned.toUpperCase();
    }

    /**
     * Internal class to hold parsed row data
     */
    private static class CurriculumRow {
        String board;
        String grade;
        String subject;
        String chapter;
        List<String> topics;
        List<RelatedTopicLink> relatedTopicLinks;
        String description;
        Integer durationMinutes;
        String suggestedTopics;

        CurriculumRow(String board, String grade, String subject, String chapter,
                     List<String> topics, List<RelatedTopicLink> relatedTopicLinks) {
            this(board, grade, subject, chapter, topics, relatedTopicLinks, null, null, null);
        }
        
        CurriculumRow(String board, String grade, String subject, String chapter,
                     List<String> topics, List<RelatedTopicLink> relatedTopicLinks,
                     String description, Integer durationMinutes, String suggestedTopics) {
            this.board = board;
            this.grade = grade;
            this.subject = subject;
            this.chapter = chapter;
            this.topics = topics;
            this.relatedTopicLinks = relatedTopicLinks;
            this.description = description;
            this.durationMinutes = durationMinutes;
            this.suggestedTopics = suggestedTopics;
        }
    }

    /**
     * Internal class to hold related topic link info
     */
    private static class RelatedTopicLink {
        String grade;
        String subject;
        String topicHint;

        RelatedTopicLink(String grade, String subject, String topicHint) {
            this.grade = grade;
            this.subject = subject;
            this.topicHint = topicHint;
        }
    }
}
