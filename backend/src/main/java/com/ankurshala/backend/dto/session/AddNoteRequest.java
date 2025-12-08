package com.ankurshala.backend.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddNoteRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Note text is required")
    private String text;

    private String url; // Optional URL attachment
}
