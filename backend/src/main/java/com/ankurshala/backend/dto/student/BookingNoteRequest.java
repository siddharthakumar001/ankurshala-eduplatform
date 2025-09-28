package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingNoteRequest {
    
    @NotBlank
    private String content;
    
    private String noteType = "GENERAL"; // GENERAL, FEEDBACK, REMINDER
}
