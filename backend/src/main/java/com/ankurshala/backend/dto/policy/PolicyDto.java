package com.ankurshala.backend.dto.policy;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PolicyDto {
    private Long id;
    private String title;
    private String content;
    private String version;
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
