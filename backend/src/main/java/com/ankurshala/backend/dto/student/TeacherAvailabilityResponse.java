package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityResponse {
    
    private Long teacherId;
    
    private String teacherName;
    
    private Boolean available;
    
    private List<TimeSlotDto> availableSlots;
    
    private String message; // E.g., "Teacher is fully booked on this date"
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDto {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean available;
        private String status; // "AVAILABLE", "BOOKED", "BLOCKED"
    }
}
