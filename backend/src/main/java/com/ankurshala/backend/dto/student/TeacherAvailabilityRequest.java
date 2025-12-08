package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityRequest {
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be present or future")
    private LocalDate date;
    
    private Integer durationMinutes; // Optional - for checking if specific duration fits
}
