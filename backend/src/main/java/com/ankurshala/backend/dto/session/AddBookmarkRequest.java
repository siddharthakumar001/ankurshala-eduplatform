package com.ankurshala.backend.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddBookmarkRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Timestamp in seconds is required")
    @Min(value = 0, message = "Timestamp cannot be negative")
    private Integer tsSeconds;

    @NotBlank(message = "Bookmark note is required")
    private String note;
}
