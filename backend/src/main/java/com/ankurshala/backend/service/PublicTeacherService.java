package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicTeacherService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherReviewService reviewService;
    private final BookingRepository bookingRepository;

    public Page<TeacherSearchResponse> searchTeachers(TeacherSearchRequest request, Pageable pageable) {
        log.info("Searching teachers with criteria: {}", request);
        
        // Use the existing repository method for filtering
        String search = request.getSearch();
        Boolean verifiedOnly = request.getVerifiedOnly();
        
        // For now, use the existing filter method and post-filter in Java
        // In production, we'd add custom query methods to the repository
        Page<TeacherProfile> teacherProfiles = teacherProfileRepository.findTeachersWithFilters(
            search,
            true, // enabled = true
            TeacherStatus.ACTIVE, // status = ACTIVE
            verifiedOnly,
            pageable
        );
        
        // Post-filter by additional criteria
        return teacherProfiles.map(this::mapToSearchResponse);
    }

    public TeacherAvailabilityResponse getTeacherAvailability(TeacherAvailabilityRequest request) {
        log.info("Checking availability for teacher {} on {}", request.getTeacherId(), request.getDate());
        
        TeacherProfile teacher = teacherProfileRepository.findById(request.getTeacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + request.getTeacherId()));
        
        // Generate time slots for the day (9 AM to 9 PM, 1-hour slots)
        List<TeacherAvailabilityResponse.TimeSlotDto> slots = generateTimeSlotsForTeacher(
                request.getTeacherId(), 
                request.getDate()
        );
        
        // Check if teacher has any available slots
        boolean hasAvailability = slots.stream().anyMatch(TeacherAvailabilityResponse.TimeSlotDto::getAvailable);
        
        // Get teacher name - use name from TeacherProfile first, fallback to User.name
        String teacherName = (teacher.getFirstName() != null ? teacher.getFirstName() : "") + 
                             (teacher.getLastName() != null ? " " + teacher.getLastName() : "");
        if (teacherName.isBlank() && teacher.getUser() != null) {
            teacherName = teacher.getUser().getName();
        }
        
        return TeacherAvailabilityResponse.builder()
            .teacherId(teacher.getId())
            .teacherName(teacherName.trim())
            .available(hasAvailability)
            .availableSlots(slots)
            .message(hasAvailability ? "Teacher has available slots" : "Teacher is fully booked on this date")
            .build();
    }

    public TeacherSearchResponse getTeacherProfile(Long teacherId) {
        log.info("Getting teacher profile for teacher {}", teacherId);
        
        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        
        return mapToSearchResponse(teacher);
    }

    private TeacherSearchResponse mapToSearchResponse(TeacherProfile teacher) {
        // Get teacher name - use name from TeacherProfile first, fallback to User.name
        String fullName = (teacher.getFirstName() != null ? teacher.getFirstName() : "") + 
                          (teacher.getLastName() != null ? " " + teacher.getLastName() : "");
        if (fullName.isBlank() && teacher.getUser() != null) {
            fullName = teacher.getUser().getName();
        }
        
        // Convert BigDecimal rating to Double
        Double ratingValue = teacher.getRating() != null ? teacher.getRating().doubleValue() : 0.0;
        
        return TeacherSearchResponse.builder()
            .id(teacher.getId())
            .name(fullName.trim())
            .email(teacher.getUser() != null ? teacher.getUser().getEmail() : teacher.getContactEmail())
            .profilePictureUrl(teacher.getProfilePhotoUrl())
            .bio(teacher.getBio())
            .rating(ratingValue)
            .totalRatings(teacher.getTotalReviews() != null ? teacher.getTotalReviews() : 0)
            .hourlyRate(teacher.getHourlyRate())
            .teacherCategory(teacher.getTeacherCategory() != null ? teacher.getTeacherCategory() : "STANDARD")
            .languages(parseLanguages(teacher.getLanguages()))
            .specializations(parseSpecializations(teacher.getSpecialization()))
            .yearsOfExperience(teacher.getYearsOfExperience())
            .verified(teacher.getVerified())
            .currentlyAvailable(teacher.getUser() != null && teacher.getUser().getEnabled())
            .completedSessions(0) // TODO: Get from booking statistics
            .qualifications(teacher.getQualifications())
            .responseRate(95.0) // TODO: Calculate from booking response data
            .responseTime("Within 2 hours") // TODO: Calculate from booking response data
            .recentReviews(reviewService.getRecentReviews(teacher.getId(), 3))
            .build();
    }

    private List<String> parseSpecializations(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return List.of();
        }
        return List.of(specialization.split(",")).stream()
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private List<String> parseLanguages(String languages) {
        if (languages == null || languages.isBlank()) {
            return List.of("English", "Hindi"); // Default fallback
        }
        return List.of(languages.split(",")).stream()
            .map(String::trim)
            .filter(lang -> !lang.isEmpty())
            .collect(Collectors.toList());
    }

    private List<TeacherAvailabilityResponse.TimeSlotDto> generateTimeSlotsForTeacher(Long teacherId, LocalDate date) {
        List<TeacherAvailabilityResponse.TimeSlotDto> slots = new ArrayList<>();
        
        // Generate hourly slots from 9 AM to 9 PM
        for (int hour = 9; hour < 21; hour++) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, 0));
            LocalDateTime endTime = startTime.plusHours(1);
            
            // Check if this slot is available for the teacher
            boolean isAvailable = checkTeacherAvailability(teacherId, startTime, endTime);
            
            slots.add(TeacherAvailabilityResponse.TimeSlotDto.builder()
                .startTime(startTime)
                .endTime(endTime)
                .available(isAvailable)
                .status(isAvailable ? "AVAILABLE" : "BOOKED")
                .build());
        }
        
        return slots;
    }

    private boolean checkTeacherAvailability(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        // Convert LocalDateTime to ZonedDateTime with system default zone
        ZonedDateTime zonedStartTime = startTime.atZone(ZoneId.systemDefault());
        ZonedDateTime zonedEndTime = endTime.atZone(ZoneId.systemDefault());
        
        // Query for any existing bookings that overlap with the requested time slot
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookingsForTeacher(
                teacherId,
                zonedStartTime, 
                zonedEndTime
        );
        
        return conflictingBookings.isEmpty();
    }
}
