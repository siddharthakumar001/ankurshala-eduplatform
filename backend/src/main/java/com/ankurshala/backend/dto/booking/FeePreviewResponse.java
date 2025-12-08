package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeePreviewResponse {
    
    @NotBlank(message = "Currency is required")
    private String currency = "INR";

    @NotNull(message = "Fee is required")
    @Min(value = 0, message = "Fee cannot be negative")
    private Integer fee;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Waived status is required")
    private Boolean waived = false;
}
