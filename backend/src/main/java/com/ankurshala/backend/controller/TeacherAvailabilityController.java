package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.teacher.TeacherBookingPreferencesDto;
import com.ankurshala.backend.dto.teacher.TeacherWeeklyAvailabilityDto;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherBookingPreferences;
import com.ankurshala.backend.entity.TeacherWeeklyAvailability;
import com.ankurshala.backend.repository.TeacherBookingPreferencesRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherWeeklyAvailabilityRepository;
import com.ankurshala.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherAvailabilityController {

    private final TeacherWeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final TeacherBookingPreferencesRepository bookingPreferencesRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping("/availability/weekly")
    public ResponseEntity<List<TeacherWeeklyAvailabilityDto>> getWeeklyAvailability(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Teacher teacher = getTeacher(userPrincipal);
        List<TeacherWeeklyAvailabilityDto> availability = weeklyAvailabilityRepository.findByTeacherId(teacher.getId())
            .stream()
            .map(this::toAvailabilityDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(availability);
    }

    @PutMapping("/availability/weekly")
    public ResponseEntity<List<TeacherWeeklyAvailabilityDto>> updateWeeklyAvailability(
            @Valid @RequestBody List<TeacherWeeklyAvailabilityDto> availabilityDtos,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Teacher teacher = getTeacher(userPrincipal);

        for (TeacherWeeklyAvailabilityDto dto : availabilityDtos) {
            LocalTime startTime = dto.getStartTime();
            LocalTime endTime = dto.getEndTime();
            if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
                return ResponseEntity.badRequest().build();
            }
        }

        weeklyAvailabilityRepository.deleteByTeacherId(teacher.getId());

        List<TeacherWeeklyAvailability> saved = weeklyAvailabilityRepository.saveAll(
            availabilityDtos.stream()
                .map(dto -> toAvailabilityEntity(dto, teacher))
                .collect(Collectors.toList())
        );

        List<TeacherWeeklyAvailabilityDto> response = saved.stream()
            .map(this::toAvailabilityDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preferences/booking")
    public ResponseEntity<TeacherBookingPreferencesDto> getBookingPreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Teacher teacher = getTeacher(userPrincipal);
        TeacherBookingPreferences preferences = bookingPreferencesRepository.findByTeacherId(teacher.getId())
            .orElseGet(() -> bookingPreferencesRepository.save(new TeacherBookingPreferences(teacher)));

        return ResponseEntity.ok(toPreferencesDto(preferences));
    }

    @PutMapping("/preferences/booking")
    public ResponseEntity<TeacherBookingPreferencesDto> updateBookingPreferences(
            @Valid @RequestBody TeacherBookingPreferencesDto preferencesDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Teacher teacher = getTeacher(userPrincipal);

        TeacherBookingPreferences preferences = bookingPreferencesRepository.findByTeacherId(teacher.getId())
            .orElseGet(() -> new TeacherBookingPreferences(teacher));

        preferences.setAutoAcceptBookings(preferencesDto.getAutoAcceptBookings());
        preferences.setAdvanceBookingDays(preferencesDto.getAdvanceBookingDays());
        preferences.setMinimumSessionDuration(preferencesDto.getMinimumSessionDuration());
        preferences.setMaximumSessionDuration(preferencesDto.getMaximumSessionDuration());
        preferences.setCancellationPolicyHours(preferencesDto.getCancellationPolicyHours());

        TeacherBookingPreferences saved = bookingPreferencesRepository.save(preferences);
        return ResponseEntity.ok(toPreferencesDto(saved));
    }

    private Teacher getTeacher(UserPrincipal userPrincipal) {
        return teacherRepository.findByUserId(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
    }

    private TeacherWeeklyAvailabilityDto toAvailabilityDto(TeacherWeeklyAvailability availability) {
        TeacherWeeklyAvailabilityDto dto = new TeacherWeeklyAvailabilityDto();
        dto.setId(availability.getId());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setIsAvailable(availability.getIsAvailable());
        return dto;
    }

    private TeacherWeeklyAvailability toAvailabilityEntity(TeacherWeeklyAvailabilityDto dto, Teacher teacher) {
        TeacherWeeklyAvailability availability = new TeacherWeeklyAvailability();
        availability.setTeacher(teacher);
        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setIsAvailable(dto.getIsAvailable());
        return availability;
    }

    private TeacherBookingPreferencesDto toPreferencesDto(TeacherBookingPreferences preferences) {
        TeacherBookingPreferencesDto dto = new TeacherBookingPreferencesDto();
        dto.setId(preferences.getId());
        dto.setAutoAcceptBookings(preferences.getAutoAcceptBookings());
        dto.setAdvanceBookingDays(preferences.getAdvanceBookingDays());
        dto.setMinimumSessionDuration(preferences.getMinimumSessionDuration());
        dto.setMaximumSessionDuration(preferences.getMaximumSessionDuration());
        dto.setCancellationPolicyHours(preferences.getCancellationPolicyHours());
        return dto;
    }
}
