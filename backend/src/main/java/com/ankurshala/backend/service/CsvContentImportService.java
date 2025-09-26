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
        boardRepository.findByName(boardName)
                .orElseGet(() -> boardRepository.save(new Board(boardName)));
        
        // Find or create Grade
        gradeRepository.findByName(gradeName)
                .orElseGet(() -> {
                    String displayName = "Grade " + gradeName;
                    return gradeRepository.save(new Grade(gradeName, displayName));
                });
        
        // Find or create Subject
        Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> subjectRepository.save(new Subject(subjectName)));
        
        // Find or create Chapter
        Chapter chapter = chapterRepository.findBySubjectIdAndName(subject.getId(), chapterName)
                .orElseGet(() -> chapterRepository.save(new Chapter(subject, chapterName)));
        
        // Find or create Topic (by code if provided, else by chapter and title)
        Topic topic;
        if (topicCode != null && !topicCode.trim().isEmpty()) {
            topic = topicRepository.findByCode(topicCode)
                    .orElseGet(() -> createNewTopic(chapter, record));
        } else {
            topic = topicRepository.findByChapterIdAndTitle(chapter.getId(), topicTitle)
                    .orElseGet(() -> createNewTopic(chapter, record));
        }
        
        // Update topic with new data
        updateTopicFromRecord(topic, record);
        topicRepository.save(topic);
    }

    private Topic createNewTopic(Chapter chapter, Map<String, String> record) {
        Topic topic = new Topic(chapter, record.get("topictitle"));
        updateTopicFromRecord(topic, record);
        return topic;
    }

    private void updateTopicFromRecord(Topic topic, Map<String, String> record) {
        topic.setTitle(record.get("topictitle"));
        
        String topicCode = record.get("topiccode");
        if (topicCode != null && !topicCode.trim().isEmpty()) {
            topic.setCode(topicCode.trim());
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
        return "Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary,TopicCode,Prerequisites,RelatedTopics,Active\n" +
               "CBSE,9,Physics,Motion,Introduction to Motion,1.5,Basic concepts of motion,Understanding motion and its types,PHY901,Basic Mathematics,Force and Energy,true\n" +
               "CBSE,9,Physics,Motion,Types of Motion,2.0,Different types of motion,Linear rotational and oscillatory motion,PHY902,Introduction to Motion,,true\n" +
               "CBSE,9,Chemistry,Atoms and Molecules,Atomic Structure,1.0,Structure of atoms,Protons neutrons and electrons,CHEM901,Basic Chemistry,,true\n" +
               "CBSE,10,Mathematics,Algebra,Quadratic Equations,2.5,Solving quadratic equations,Methods to solve quadratic equations,MATH1001,Linear Equations,Polynomials,true";
    }

    public org.springframework.data.domain.Page<ImportJob> getAllImportJobs(int page, int size) {
        return importJobRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }
}
