package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AvailableSlotResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
