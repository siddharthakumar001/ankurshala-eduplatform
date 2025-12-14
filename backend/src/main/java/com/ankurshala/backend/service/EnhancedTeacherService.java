package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.TeacherSignupRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Teacher Service
 * Handles comprehensive teacher profile management with mandatory professional fields
 */
@Slf4j
@Service
@Transactional
public class EnhancedTeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TeacherAvailabilitySlotRepository teacherAvailabilitySlotRepository;

    /**
     * Create enhanced teacher profile with all mandatory fields
     */
    public Teacher createTeacherProfile(User user, TeacherSignupRequest signupRequest) {
        log.info("Creating enhanced teacher profile for user ID: {}, email: {}", user.getId(), user.getEmail());
        
        // Create teacher entity
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(signupRequest.getName());
        teacher.setEmail(signupRequest.getEmail());
        teacher.setBio(signupRequest.getBio());
        teacher.setYearsExperience(signupRequest.getYearsExperience());
        teacher.setLanguagesArray(signupRequest.getLanguages().toArray(new String[0]));
        teacher.setCategoriesArray(signupRequest.getCategories().toArray(new String[0]));
        teacher.setHourlyRate(signupRequest.getHourlyRate());
        teacher.setStatus(com.ankurshala.backend.entity.TeacherStatus.PENDING);
        
        // Set optional fields
        if (signupRequest.getPhoneNumber() != null) {
            teacher.setPhoneNumber(signupRequest.getPhoneNumber());
        }
        if (signupRequest.getLinkedinProfile() != null) {
            teacher.setLinkedinProfile(signupRequest.getLinkedinProfile());
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Enhanced teacher created with ID: {} for user ID: {}", savedTeacher.getId(), user.getId());
        
        // Create teacher profile
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        profile.setTeacher(savedTeacher);
        profile.setBio(signupRequest.getBio());
        profile.setYearsExperience(signupRequest.getYearsExperience());
        profile.setHourlyRate(signupRequest.getHourlyRate());
        
        TeacherProfile savedProfile = teacherProfileRepository.save(profile);
        log.info("Enhanced teacher profile created with ID: {} for user ID: {}", savedProfile.getId(), user.getId());
        
        // Create teacher subject expertise mappings
        if (signupRequest.getSubjectExpertise() != null && !signupRequest.getSubjectExpertise().isEmpty()) {
            createSubjectExpertise(savedTeacher, signupRequest.getSubjectExpertise());
        }
        
        // Create teacher availability slots
        if (signupRequest.getAvailability() != null && !signupRequest.getAvailability().isEmpty()) {
            createAvailabilitySlots(user, signupRequest.getAvailability());
        }
        
        return savedTeacher;
    }
    
    /**
     * Create subject expertise mappings for a teacher
     * Handles grade ranges like "Class 1-5" by creating entries for each grade in the range
     */
    private void createSubjectExpertise(Teacher teacher, List<TeacherSignupRequest.SubjectExpertiseDto> expertiseList) {
        for (TeacherSignupRequest.SubjectExpertiseDto expertise : expertiseList) {
            try {
                // Find subject by ID
                Optional<Subject> subjectOpt = subjectRepository.findById(expertise.getSubjectId());
                if (subjectOpt.isEmpty()) {
                    log.warn("Subject not found with ID: {} - skipping expertise", expertise.getSubjectId());
                    continue;
                }
                
                // Find board by name (e.g., "CBSE", "ICSE")
                Board board = null;
                if (expertise.getBoard() != null && !expertise.getBoard().isEmpty()) {
                    Optional<Board> boardOpt = boardRepository.findByNameIgnoreCase(expertise.getBoard());
                    if (boardOpt.isPresent()) {
                        board = boardOpt.get();
                    }
                }
                
                String language = expertise.getLanguage() != null ? expertise.getLanguage() : "English";
                
                // Parse grade range (e.g., "Class 1-5", "Class 9-10")
                List<Grade> matchingGrades = parseGradeRange(expertise.getGrade());
                
                if (matchingGrades.isEmpty()) {
                    // If no grades match, create one entry with null grade
                    createSingleExpertise(teacher, subjectOpt.get(), null, board, language);
                    log.info("Created subject expertise (no grade): teacher={}, subject={}, board={}", 
                        teacher.getId(), expertise.getSubjectId(), expertise.getBoard());
                } else {
                    // Create one entry for each grade in the range
                    for (Grade grade : matchingGrades) {
                        createSingleExpertise(teacher, subjectOpt.get(), grade, board, language);
                    }
                    log.info("Created {} subject expertise entries: teacher={}, subject={}, grades={}, board={}", 
                        matchingGrades.size(), teacher.getId(), expertise.getSubjectId(), expertise.getGrade(), expertise.getBoard());
                }
                
            } catch (Exception e) {
                log.error("Error creating subject expertise: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Create a single expertise entry, handling duplicates gracefully
     */
    private void createSingleExpertise(Teacher teacher, Subject subject, Grade grade, Board board, String language) {
        try {
            TeacherSubjectExpertise tse = new TeacherSubjectExpertise(teacher, subject, grade, board, language);
            teacherSubjectExpertiseRepository.save(tse);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.debug("Expertise already exists for teacher={}, subject={}, grade={}, board={} - skipping", 
                teacher.getId(), subject.getId(), grade != null ? grade.getId() : null, board != null ? board.getId() : null);
        }
    }
    
    /**
     * Parse grade range string into list of matching Grade entities
     * e.g., "Class 1-5" returns grades 1, 2, 3, 4, 5
     * e.g., "Class 9-10" returns grades 9, 10
     */
    private List<Grade> parseGradeRange(String gradeStr) {
        List<Grade> result = new java.util.ArrayList<>();
        if (gradeStr == null || gradeStr.isEmpty()) {
            return result;
        }
        
        // Remove "Class " prefix and parse the range
        String range = gradeStr.replace("Class ", "").trim();
        
        try {
            if (range.contains("-")) {
                String[] parts = range.split("-");
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                
                List<Grade> allGrades = gradeRepository.findAll();
                for (Grade g : allGrades) {
                    try {
                        int gradeNum = Integer.parseInt(g.getName().trim());
                        if (gradeNum >= start && gradeNum <= end) {
                            result.add(g);
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip grades with non-numeric names
                    }
                }
            } else {
                // Single grade
                int gradeNum = Integer.parseInt(range);
                List<Grade> allGrades = gradeRepository.findAll();
                for (Grade g : allGrades) {
                    try {
                        if (Integer.parseInt(g.getName().trim()) == gradeNum) {
                            result.add(g);
                            break; // Found the grade
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip grades with non-numeric names
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse grade range: {}", gradeStr);
        }
        
        return result;
    }
    
    /**
     * Create availability slots for a teacher
     */
    private void createAvailabilitySlots(User user, List<TeacherSignupRequest.AvailabilitySlotDto> availabilityList) {
        for (TeacherSignupRequest.AvailabilitySlotDto slot : availabilityList) {
            try {
                // Parse start and end times
                String[] startParts = slot.getStartTime().split(":");
                String[] endParts = slot.getEndTime().split(":");
                
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                
                // Create availability slot for the specified weekday
                // Use a reference date (Monday of current week) and add days for the weekday
                java.time.LocalDate refDate = java.time.LocalDate.now()
                    .with(java.time.DayOfWeek.MONDAY)
                    .plusDays(slot.getWeekday() - 1); // weekday 1 = Monday
                
                java.time.LocalDateTime startTime = refDate.atTime(startHour, startMinute);
                java.time.LocalDateTime endTime = refDate.atTime(endHour, endMinute);
                
                TeacherAvailabilitySlot availabilitySlot = new TeacherAvailabilitySlot();
                availabilitySlot.setTeacher(user);
                availabilitySlot.setStartTime(startTime);
                availabilitySlot.setEndTime(endTime);
                availabilitySlot.setIsAvailable(true);
                
                teacherAvailabilitySlotRepository.save(availabilitySlot);
                log.info("Created availability slot: teacher={}, weekday={}, start={}, end={}", 
                    user.getId(), slot.getWeekday(), slot.getStartTime(), slot.getEndTime());
                
            } catch (Exception e) {
                log.error("Error creating availability slot: {}", e.getMessage());
            }
        }
    }

    /**
     * Get teacher by user ID
     */
    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for user ID: " + userId));
    }

    /**
     * Get teacher profile by user ID
     */
    public TeacherProfile getTeacherProfileByUserId(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for user ID: " + userId));
    }

    /**
     * Update teacher profile
     */
    public Teacher updateTeacherProfile(Long userId, TeacherSignupRequest updateRequest) {
        Teacher teacher = getTeacherByUserId(userId);
        
        // Update basic fields
        if (updateRequest.getName() != null) {
            teacher.setName(updateRequest.getName());
        }
        if (updateRequest.getBio() != null) {
            teacher.setBio(updateRequest.getBio());
        }
        if (updateRequest.getYearsExperience() != null) {
            teacher.setYearsExperience(updateRequest.getYearsExperience());
        }
        if (updateRequest.getLanguages() != null) {
            teacher.setLanguagesArray(updateRequest.getLanguages().toArray(new String[0]));
        }
        if (updateRequest.getCategories() != null) {
            teacher.setCategoriesArray(updateRequest.getCategories().toArray(new String[0]));
        }
        if (updateRequest.getHourlyRate() != null) {
            teacher.setHourlyRate(updateRequest.getHourlyRate());
        }
        
        // Update optional fields
        if (updateRequest.getPhoneNumber() != null) {
            teacher.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getLinkedinProfile() != null) {
            teacher.setLinkedinProfile(updateRequest.getLinkedinProfile());
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        // Update teacher profile
        TeacherProfile profile = getTeacherProfileByUserId(userId);
        if (updateRequest.getBio() != null) {
            profile.setBio(updateRequest.getBio());
        }
        if (updateRequest.getYearsExperience() != null) {
            profile.setYearsExperience(updateRequest.getYearsExperience());
        }
        if (updateRequest.getHourlyRate() != null) {
            profile.setHourlyRate(updateRequest.getHourlyRate());
        }
        
        teacherProfileRepository.save(profile);
        
        return savedTeacher;
    }

    /**
     * Validate teacher categories
     */
    public boolean isValidCategory(String category) {
        return "STANDARD".equals(category) || "PREMIUM".equals(category);
    }

    /**
     * Validate teacher hourly rate
     */
    public boolean isValidHourlyRate(BigDecimal rate) {
        return rate != null && rate.compareTo(BigDecimal.valueOf(100)) >= 0 && 
               rate.compareTo(BigDecimal.valueOf(10000)) <= 0;
    }
}