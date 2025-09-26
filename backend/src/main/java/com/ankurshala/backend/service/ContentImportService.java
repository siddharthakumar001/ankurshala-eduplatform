package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.CourseContentRepository;
import com.ankurshala.backend.repository.ImportJobRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ContentImportService {

    @Autowired
    private CourseContentRepository courseContentRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ImportJob createImportJob(String fileName, String fileType, Long fileSize, Long userId) {
        ImportJob importJob = new ImportJob(fileName, fileType, fileSize);
        importJob.setStatus(ImportJobStatus.PENDING);
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            importJob.setCreatedBy(user);
        }
        
        return importJobRepository.save(importJob);
    }

    @Async
    public CompletableFuture<ImportJob> processFileAsync(ImportJob importJob, MultipartFile file) {
        try {
            importJob.setStatus(ImportJobStatus.RUNNING);
            importJob.setStartedAt(LocalDateTime.now());
            importJobRepository.save(importJob);

            List<CourseContent> courseContents = new ArrayList<>();
            
            if (file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                courseContents = parseCsvFile(file, importJob);
            } else if (file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                courseContents = parseXlsxFile(file, importJob);
            } else {
                throw new IllegalArgumentException("Unsupported file type. Only CSV and XLSX files are supported.");
            }

            // Only save if we have valid content
            if (!courseContents.isEmpty()) {
                courseContentRepository.saveAll(courseContents);
            }
            
            importJob.setStatus(ImportJobStatus.SUCCEEDED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJob.setSuccessRows(courseContents.size());
            importJob.setTotalRows(courseContents.size());
            
        } catch (Exception e) {
            importJob.setStatus(ImportJobStatus.FAILED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJob.setErrorMessage(e.getMessage());
            System.err.println("Import failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return CompletableFuture.completedFuture(importJobRepository.save(importJob));
    }

    private List<CourseContent> parseCsvFile(MultipartFile file, ImportJob importJob) throws IOException {
        List<CourseContent> courseContents = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<CSVRecord> records = parser.getRecords();
            importJob.setTotalRows(records.size());
            
            for (CSVRecord record : records) {
                try {
                    CourseContent content = parseCsvRecord(record);
                    content.setImportJob(importJob);
                    courseContents.add(content);
                    // Count will be updated at the end
                } catch (Exception e) {
                    importJob.setErrorRows((importJob.getErrorRows() != null ? importJob.getErrorRows() : 0) + 1);
                    // Log error but continue processing
                    System.err.println("Error parsing record: " + e.getMessage());
                }
            }
        }
        
        return courseContents;
    }

    private List<CourseContent> parseXlsxFile(MultipartFile file, ImportJob importJob) throws IOException {
        List<CourseContent> courseContents = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int totalRows = sheet.getLastRowNum();
            int validRows = 0;
            
            // First pass: count valid rows
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row != null && hasRequiredFields(row)) {
                    validRows++;
                }
            }
            
            importJob.setTotalRows(validRows);
            
            // Second pass: process valid rows
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row != null && hasRequiredFields(row)) {
                    try {
                        CourseContent content = parseXlsxRow(row);
                        content.setImportJob(importJob);
                        courseContents.add(content);
                        // Count will be updated at the end
                    } catch (Exception e) {
                        importJob.setErrorRows((importJob.getErrorRows() != null ? importJob.getErrorRows() : 0) + 1);
                        // Log error but continue processing
                        System.err.println("Error parsing row " + i + ": " + e.getMessage());
                    }
                }
            }
        }
        
        return courseContents;
    }
    
    private boolean hasRequiredFields(Row row) {
        if (row == null) return false;
        
        String classLevelValue = getCellStringValue(row, 0);
        String subjectValue = getCellStringValue(row, 1);
        
        // Row is valid if BOTH class level AND subject are present and not empty
        return (classLevelValue != null && !classLevelValue.trim().isEmpty()) &&
               (subjectValue != null && !subjectValue.trim().isEmpty());
    }
    

    private CourseContent parseCsvRecord(CSVRecord record) {
        CourseContent content = new CourseContent();
        
        // Map CSV columns to entity fields
        String classLevelStr = getStringValue(record, "Class Level");
        String subjectStr = getStringValue(record, "Subject");
        
        // Ensure required fields are not null
        if (classLevelStr == null || classLevelStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Class Level is required but was empty");
        }
        if (subjectStr == null || subjectStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required but was empty");
        }
        
        content.setClassLevel(parseClassLevel(classLevelStr));
        content.setSubject(subjectStr.trim());
        content.setChapter(getStringValue(record, "Chapter"));
        content.setTopic(getStringValue(record, "Topic"));
        content.setBriefDescription(getStringValue(record, "Brief Description"));
        content.setSummary(getStringValue(record, "Summary"));
        content.setSuggestedTopics(getStringValue(record, "Suggested Topics"));
        content.setResourceUrl(getStringValue(record, "Resource URL"));
        content.setExpectedTimeMinutes(parseInteger(getStringValue(record, "Expected Time Minutes")));
        
        return content;
    }

    private CourseContent parseXlsxRow(Row row) {
        CourseContent content = new CourseContent();
        
        // Map XLSX columns to entity fields (assuming same order as CSV)
        String classLevelStr = getCellStringValue(row, 0);
        String subjectStr = getCellStringValue(row, 1);
        
        // Ensure required fields are not null
        if (classLevelStr == null || classLevelStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Class Level is required but was empty");
        }
        if (subjectStr == null || subjectStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required but was empty");
        }
        
        content.setClassLevel(parseClassLevel(classLevelStr));
        content.setSubject(subjectStr.trim());
        content.setChapter(getCellStringValue(row, 2));
        content.setTopic(getCellStringValue(row, 3));
        content.setBriefDescription(getCellStringValue(row, 4));
        content.setSummary(getCellStringValue(row, 5));
        content.setSuggestedTopics(getCellStringValue(row, 6));
        content.setResourceUrl(getCellStringValue(row, 7));
        content.setExpectedTimeMinutes(parseInteger(getCellStringValue(row, 8)));
        
        return content;
    }

    private String getStringValue(CSVRecord record, String columnName) {
        try {
            return record.get(columnName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getCellStringValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private ClassLevel parseClassLevel(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Class Level cannot be null or empty");
        }
        
        try {
            return ClassLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to map common variations
            String normalized = value.trim().toUpperCase().replace(" ", "_");
            try {
                return ClassLevel.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                // If still can't parse, default to OTHER instead of returning null
                return ClassLevel.OTHER;
            }
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        
        try {
            // Remove common suffixes like "MIN", "MINT", etc.
            String cleaned = value.trim().replaceAll("\\s*(MIN|MINT|MINUTES?)\\s*$", "");
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<ImportJob> getImportJobs() {
        return importJobRepository.findAll();
    }

    public ImportJob getImportJob(Long id) {
        return importJobRepository.findById(id).orElse(null);
    }

    public List<ImportJob> getActiveImportJobs() {
        return importJobRepository.findActiveJobs();
    }
}
