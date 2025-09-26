package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BroadcastNotificationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Audience is required")
    private String audience;

    @NotNull(message = "Delivery method is required")
    private String delivery;
}
