package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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

@Service
public class CsvContentImportService {

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
    private UserRepository userRepository;

    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "board", "grade", "subject", "chapter", "topictitle", "hours"
    );

    public void validateCsvHeaders(byte[] csvContent) throws IllegalArgumentException {
        try {
            CSVParser parser = CSVParser.parse(
                new InputStreamReader(new ByteArrayInputStream(csvContent), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
            );
            
            // Validate headers
            Set<String> headerSet = parser.getHeaderMap().keySet().stream()
                .map(String::toLowerCase)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
            
            Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
            missingHeaders.removeAll(headerSet);
            
            if (!missingHeaders.isEmpty()) {
                throw new IllegalArgumentException("Missing required headers: " + missingHeaders);
            }
            
            parser.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid CSV format: " + e.getMessage());
        }
    }


    @Transactional
    public ImportJob createImportJob(String fileName, Long fileSize, Long userId) {
        ImportJob importJob = new ImportJob(fileName, "CSV", fileSize);
        importJob.setType("CONTENT_CSV");
        importJob.setStatus(ImportJobStatus.PENDING);
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            importJob.setCreatedBy(user);
        }
        
        return importJobRepository.save(importJob);
    }

    @Async
    public CompletableFuture<ImportJob> processFileAsync(ImportJob importJob, byte[] csvContent, boolean dryRun) {
        try {
            importJob.setStatus(ImportJobStatus.RUNNING);
            importJob.setStartedAt(LocalDateTime.now());
            importJobRepository.save(importJob);

            // Parse CSV and validate headers
            List<Map<String, String>> records = parseCsvContent(csvContent, importJob);
            
            if (!dryRun) {
                // Process records and create/update content
                processRecords(records, importJob);
            }
            
            importJob.setStatus(ImportJobStatus.SUCCEEDED);
            importJob.setCompletedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            importJob.setStatus(ImportJobStatus.FAILED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJob.setErrorMessage(e.getMessage());
            System.err.println("Import failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return CompletableFuture.completedFuture(importJobRepository.save(importJob));
    }

    private List<Map<String, String>> parseCsvContent(byte[] csvContent, ImportJob importJob) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            // Validate headers
            Set<String> headerSet = parser.getHeaderMap().keySet().stream()
                .map(String::toLowerCase)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
            
            Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
            missingHeaders.removeAll(headerSet);
            
            if (!missingHeaders.isEmpty()) {
                throw new IllegalArgumentException("Missing required headers: " + missingHeaders);
            }
            
            List<CSVRecord> csvRecords = parser.getRecords();
            importJob.setTotalRows(csvRecords.size());
            
            int successCount = 0;
            int errorCount = 0;
            
            for (int i = 0; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);
                try {
                    Map<String, String> recordMap = parseRecord(record, i + 2); // +2 because header is row 1
                    records.add(recordMap);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    errors.add("Row " + (i + 2) + ": " + e.getMessage());
                    if (errors.size() > 20) { // Limit errors stored
                        errors.add("... and more errors");
                        break;
                    }
                }
            }
            
            importJob.setSuccessRows(successCount);
            importJob.setErrorRows(errorCount);
            
            if (!errors.isEmpty()) {
                // Store first 20 errors as JSON
                // Convert errors to JSON array format
                StringBuilder errorsJson = new StringBuilder("[");
                for (int i = 0; i < Math.min(errors.size(), 20); i++) {
                    if (i > 0) errorsJson.append(",");
                    errorsJson.append("\"").append(errors.get(i).replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
                }
                errorsJson.append("]");
                importJob.setErrors(errorsJson.toString());
                
                if (errorCount > successCount) {
                    importJob.setStatus(ImportJobStatus.PARTIALLY_SUCCEEDED);
                }
            }
        }
        
        return records;
    }

    private Map<String, String> parseRecord(CSVRecord record, int rowNumber) {
        Map<String, String> recordMap = new HashMap<>();
        
        // Validate required fields
        String board = getFieldValue(record, "board");
        String grade = getFieldValue(record, "grade");
        String subject = getFieldValue(record, "subject");
        String chapter = getFieldValue(record, "chapter");
        String topicTitle = getFieldValue(record, "topictitle");
        String hours = getFieldValue(record, "hours");
        
        if (board == null || board.trim().isEmpty()) {
            throw new IllegalArgumentException("Board is required");
        }
        if (grade == null || grade.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade is required");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (chapter == null || chapter.trim().isEmpty()) {
            throw new IllegalArgumentException("Chapter is required");
        }
        if (topicTitle == null || topicTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("TopicTitle is required");
        }
        if (hours == null || hours.trim().isEmpty()) {
            throw new IllegalArgumentException("Hours is required");
        }
        
        // Validate and convert hours to minutes
        Integer expectedTimeMins = parseHoursToMinutes(hours);
        if (expectedTimeMins == null) {
            throw new IllegalArgumentException("Invalid Hours value: " + hours);
        }
        
        // Normalize grade (accept "9" or "Grade 9" -> store "9")
        String normalizedGrade = normalizeGrade(grade);
        
        recordMap.put("board", board.trim());
        recordMap.put("grade", normalizedGrade);
        recordMap.put("subject", subject.trim());
        recordMap.put("chapter", chapter.trim());
        recordMap.put("topictitle", topicTitle.trim());
        recordMap.put("expectedTimeMins", expectedTimeMins.toString());
        
        // Optional fields
        recordMap.put("description", getFieldValue(record, "description"));
        recordMap.put("summary", getFieldValue(record, "summary"));
        recordMap.put("topiccode", getFieldValue(record, "topiccode"));
        recordMap.put("prerequisites", getFieldValue(record, "prerequisites"));
        recordMap.put("relatedtopics", getFieldValue(record, "relatedtopics"));
        
        String active = getFieldValue(record, "active");
        recordMap.put("active", active != null ? active.trim() : "true");
        
        return recordMap;
    }

    private String getFieldValue(CSVRecord record, String fieldName) {
        try {
            return record.get(fieldName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer parseHoursToMinutes(String hoursStr) {
        try {
            double hours = Double.parseDouble(hoursStr.trim());
            return (int) Math.round(hours * 60);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String generateTopicCode(String subject, String grade, String chapter, String topicTitle) {
        // Generate a unique topic code based on subject, grade, chapter, and topic title
        String subjectPrefix = getSubjectPrefix(subject);
        String gradePrefix = grade.toUpperCase();
        String chapterPrefix = getChapterPrefix(chapter);
        String topicPrefix = getTopicPrefix(topicTitle);
        
        // Create base code: SUBJECT_GRADE_CHAPTER_TOPIC
        String baseCode = String.format("%s%s_%s_%s", 
            subjectPrefix, gradePrefix, chapterPrefix, topicPrefix);
        
        // Ensure uniqueness by checking existing codes
        String uniqueCode = baseCode;
        int counter = 1;
        while (topicRepository.findByCode(uniqueCode).isPresent()) {
            uniqueCode = baseCode + "_" + counter;
            counter++;
        }
        
        return uniqueCode;
    }
    
    private String getSubjectPrefix(String subject) {
        if (subject == null) return "SUB";
        
        String normalized = subject.toUpperCase().trim();
        if (normalized.contains("PHYSICS")) return "PHY";
        if (normalized.contains("CHEMISTRY")) return "CHEM";
        if (normalized.contains("MATHEMATICS") || normalized.contains("MATH")) return "MATH";
        if (normalized.contains("BIOLOGY")) return "BIO";
        if (normalized.contains("ENGLISH")) return "ENG";
        if (normalized.contains("HISTORY")) return "HIST";
        if (normalized.contains("GEOGRAPHY")) return "GEO";
        if (normalized.contains("ECONOMICS")) return "ECO";
        if (normalized.contains("POLITICAL") || normalized.contains("POLITICS")) return "POL";
        
        // Default: take first 3 characters
        return normalized.length() >= 3 ? normalized.substring(0, 3) : normalized;
    }
    
    private String getChapterPrefix(String chapter) {
        if (chapter == null) return "CH";
        
        // Extract first word or first few characters
        String[] words = chapter.trim().split("\\s+");
        if (words.length > 0) {
            String firstWord = words[0].toUpperCase();
            return firstWord.length() >= 3 ? firstWord.substring(0, 3) : firstWord;
        }
        return "CH";
    }
    
    private String getTopicPrefix(String topicTitle) {
        if (topicTitle == null) return "T";
        
        // Extract first word or first few characters
        String[] words = topicTitle.trim().split("\\s+");
        if (words.length > 0) {
            String firstWord = words[0].toUpperCase();
            return firstWord.length() >= 3 ? firstWord.substring(0, 3) : firstWord;
        }
        return "T";
    }

    private String normalizeGrade(String grade) {
        if (grade == null) return null;
        
        String normalized = grade.trim().toLowerCase();
        
        // Remove "grade " prefix if present
        if (normalized.startsWith("grade ")) {
            normalized = normalized.substring(6);
        }
        
        return normalized;
    }

    @Transactional
    private void processRecords(List<Map<String, String>> records, ImportJob importJob) {
        for (Map<String, String> record : records) {
            try {
                processRecord(record);
            } catch (Exception e) {
                // Log error but continue processing
                System.err.println("Error processing record: " + e.getMessage());
            }
        }
    }

    private void processRecord(Map<String, String> record) {
        // Upsert taxonomy: Board -> Grade -> Subject -> Chapter -> Topic
        String boardName = record.get("board");
        String gradeName = record.get("grade");
        String subjectName = record.get("subject");
        String chapterName = record.get("chapter");
        String topicTitle = record.get("topictitle");
        String topicCode = record.get("topiccode");
        
        // Find or create Board
        Board board = boardRepository.findByName(boardName)
                .orElseGet(() -> boardRepository.save(new Board(boardName)));
        
        // Find or create Grade
        gradeRepository.findByName(gradeName)
                .orElseGet(() -> {
                    String displayName = "Grade " + gradeName;
                    Grade grade = new Grade(gradeName, displayName, board.getId());
                    return gradeRepository.save(grade);
                });
        
        // Find or create Subject
        Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> subjectRepository.save(new Subject(subjectName)));
        
        // Find or create Chapter
        Chapter chapter = chapterRepository.findBySubjectIdAndName(subject.getId(), chapterName)
                .orElseGet(() -> chapterRepository.save(new Chapter(subject, chapterName)));
        
        // Find or create Topic (always generate new code if not provided)
        Topic topic;
        String providedTopicCode = record.get("topiccode");
        
        if (providedTopicCode != null && !providedTopicCode.trim().isEmpty()) {
            // Use provided code if available
            topic = topicRepository.findByCode(providedTopicCode)
                    .orElseGet(() -> createNewTopic(chapter, record, providedTopicCode));
        } else {
            // Generate unique code automatically
            String generatedCode = generateTopicCode(subjectName, gradeName, chapterName, topicTitle);
            topic = topicRepository.findByCode(generatedCode)
                    .orElseGet(() -> createNewTopic(chapter, record, generatedCode));
        }
        
        // Update topic with new data
        updateTopicFromRecord(topic, record);
        topicRepository.save(topic);
    }

    private Topic createNewTopic(Chapter chapter, Map<String, String> record, String topicCode) {
        Topic topic = new Topic(chapter, record.get("topictitle"));
        topic.setCode(topicCode); // Set the provided or generated code
        updateTopicFromRecord(topic, record);
        return topic;
    }

    private void updateTopicFromRecord(Topic topic, Map<String, String> record) {
        topic.setTitle(record.get("topictitle"));
        
        // Only update code if it's not already set (for auto-generated codes)
        String providedTopicCode = record.get("topiccode");
        if (providedTopicCode != null && !providedTopicCode.trim().isEmpty() && topic.getCode() == null) {
            topic.setCode(providedTopicCode.trim());
        }
        
        String description = record.get("description");
        if (description != null && !description.trim().isEmpty()) {
            topic.setDescription(description.trim());
        }
        
        String summary = record.get("summary");
        if (summary != null && !summary.trim().isEmpty()) {
            topic.setSummary(summary.trim());
        }
        
        String expectedTimeMinsStr = record.get("expectedTimeMins");
        if (expectedTimeMinsStr != null) {
            topic.setExpectedTimeMins(Integer.parseInt(expectedTimeMinsStr));
        }
        
        String active = record.get("active");
        if (active != null) {
            topic.setActive(Boolean.parseBoolean(active));
        }
    }

    public String generateSampleCsv() {
        return "Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary,Prerequisites,RelatedTopics,Active\n" +
               "CBSE,9,Physics,Motion,Introduction to Motion,1.5,Basic concepts of motion,Understanding motion and its types,Basic Mathematics,Force and Energy,true\n" +
               "CBSE,9,Physics,Motion,Types of Motion,2.0,Different types of motion,Linear rotational and oscillatory motion,Introduction to Motion,,true\n" +
               "CBSE,9,Chemistry,Atoms and Molecules,Atomic Structure,1.0,Structure of atoms,Protons neutrons and electrons,Basic Chemistry,,true\n" +
               "CBSE,10,Mathematics,Algebra,Quadratic Equations,2.5,Solving quadratic equations,Methods to solve quadratic equations,Linear Equations,Polynomials,true";
    }

    @Transactional
    public void deleteImportJobAndContent(Long jobId) {
        ImportJob importJob = importJobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Import job not found with id: " + jobId));
        
        // Delete all topics created by this import job
        // Note: This is a simplified approach. In a real implementation, you might want to
        // track which topics were created by which import job for more precise deletion
        // For now, we'll delete the import job record itself
        
        importJobRepository.delete(importJob);
    }

    public Map<String, Object> validateForDuplicatesAndUpdates(byte[] csvContent) {
        try {
            List<Map<String, String>> records = parseCsvContent(csvContent, null);
            Map<String, Object> result = new HashMap<>();
            
            List<Map<String, Object>> duplicates = new ArrayList<>();
            List<Map<String, Object>> updates = new ArrayList<>();
            List<Map<String, Object>> newContent = new ArrayList<>();
            
            for (Map<String, String> record : records) {
                String boardName = record.get("board");
                String gradeName = record.get("grade");
                String subjectName = record.get("subject");
                String chapterName = record.get("chapter");
                String topicTitle = record.get("topictitle");
                
                // Find existing content
                Optional<Topic> existingTopic = findExistingTopic(boardName, gradeName, subjectName, chapterName, topicTitle);
                
                if (existingTopic.isPresent()) {
                    Topic topic = existingTopic.get();
                    Map<String, Object> comparison = compareContent(topic, record);
                    
                    if (comparison.get("isDuplicate").equals(true)) {
                        duplicates.add(Map.of(
                            "board", boardName,
                            "grade", gradeName,
                            "subject", subjectName,
                            "chapter", chapterName,
                            "topicTitle", topicTitle,
                            "existingId", topic.getId(),
                            "message", "Content is identical to existing record"
                        ));
                    } else {
                        updates.add(Map.of(
                            "board", boardName,
                            "grade", gradeName,
                            "subject", subjectName,
                            "chapter", chapterName,
                            "topicTitle", topicTitle,
                            "existingId", topic.getId(),
                            "changes", comparison.get("changes"),
                            "message", "Content has updates"
                        ));
                    }
                } else {
                    newContent.add(Map.of(
                        "board", boardName,
                        "grade", gradeName,
                        "subject", subjectName,
                        "chapter", chapterName,
                        "topicTitle", topicTitle,
                        "message", "New content to be added"
                    ));
                }
            }
            
            result.put("duplicates", duplicates);
            result.put("updates", updates);
            result.put("newContent", newContent);
            result.put("totalRecords", records.size());
            result.put("duplicateCount", duplicates.size());
            result.put("updateCount", updates.size());
            result.put("newCount", newContent.size());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Error validating content: " + e.getMessage(), e);
        }
    }
    
    private Optional<Topic> findExistingTopic(String boardName, String gradeName, String subjectName, String chapterName, String topicTitle) {
        try {
            // Find board
            Optional<Board> board = boardRepository.findByName(boardName);
            if (!board.isPresent()) return Optional.empty();
            
            // Find grade
            Optional<Grade> grade = gradeRepository.findByName(gradeName);
            if (!grade.isPresent()) return Optional.empty();
            
            // Find subject
            Optional<Subject> subject = subjectRepository.findByName(subjectName);
            if (!subject.isPresent()) return Optional.empty();
            
            // Find chapter
            Optional<Chapter> chapter = chapterRepository.findBySubjectIdAndName(subject.get().getId(), chapterName);
            if (!chapter.isPresent()) return Optional.empty();
            
            // Find topic
            return topicRepository.findByChapterIdAndTitle(chapter.get().getId(), topicTitle);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private Map<String, Object> compareContent(Topic existingTopic, Map<String, String> newRecord) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> changes = new ArrayList<>();
        boolean isDuplicate = true;
        
        // Compare title
        if (!Objects.equals(existingTopic.getTitle(), newRecord.get("topictitle"))) {
            changes.add(Map.of(
                "field", "title",
                "oldValue", existingTopic.getTitle(),
                "newValue", newRecord.get("topictitle")
            ));
            isDuplicate = false;
        }
        
        // Compare description
        String newDescription = newRecord.get("description");
        if (newDescription != null && !newDescription.trim().isEmpty()) {
            if (!Objects.equals(existingTopic.getDescription(), newDescription.trim())) {
                changes.add(Map.of(
                    "field", "description",
                    "oldValue", existingTopic.getDescription(),
                    "newValue", newDescription.trim()
                ));
                isDuplicate = false;
            }
        }
        
        // Compare summary
        String newSummary = newRecord.get("summary");
        if (newSummary != null && !newSummary.trim().isEmpty()) {
            if (!Objects.equals(existingTopic.getSummary(), newSummary.trim())) {
                changes.add(Map.of(
                    "field", "summary",
                    "oldValue", existingTopic.getSummary(),
                    "newValue", newSummary.trim()
                ));
                isDuplicate = false;
            }
        }
        
        // Compare expected time
        String newHours = newRecord.get("hours");
        if (newHours != null && !newHours.trim().isEmpty()) {
            Integer newTimeMins = parseHoursToMinutes(newHours);
            if (newTimeMins != null && !Objects.equals(existingTopic.getExpectedTimeMins(), newTimeMins)) {
                changes.add(Map.of(
                    "field", "expectedTimeMins",
                    "oldValue", existingTopic.getExpectedTimeMins(),
                    "newValue", newTimeMins
                ));
                isDuplicate = false;
            }
        }
        
        // Compare active status
        String newActive = newRecord.get("active");
        if (newActive != null) {
            boolean newActiveValue = Boolean.parseBoolean(newActive);
            if (!Objects.equals(existingTopic.getActive(), newActiveValue)) {
                changes.add(Map.of(
                    "field", "active",
                    "oldValue", existingTopic.getActive(),
                    "newValue", newActiveValue
                ));
                isDuplicate = false;
            }
        }
        
        result.put("isDuplicate", isDuplicate);
        result.put("changes", changes);
        
        return result;
    }

    public org.springframework.data.domain.Page<ImportJob> getAllImportJobs(int page, int size) {
        return importJobRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }
}
