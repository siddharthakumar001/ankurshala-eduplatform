package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class BookingQuoteResponse {
    
    @NotNull(message = "Expected minutes is required")
    @Min(value = 15, message = "Expected minutes must be at least 15")
    @Max(value = 180, message = "Expected minutes cannot exceed 180")
    private Integer expectedMinutes;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTimeISO;

    @NotNull(message = "Buffer OK status is required")
    private Boolean bufferOk;

    @NotNull(message = "Price information is required")
    private PriceInfo price;

    // Compatibility methods for tests
    public Integer getDurationMinutes() {
        return expectedMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.expectedMinutes = durationMinutes;
    }

    public ZonedDateTime getEndTime() {
        return endTimeISO;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTimeISO = endTime;
    }

    public Boolean isBufferOk() {
        return bufferOk;
    }

    public void setBufferOk(Boolean bufferOk) {
        this.bufferOk = bufferOk;
    }

    public PriceInfo getPriceDetails() {
        return price;
    }

    public void setPriceDetails(PriceInfo priceDetails) {
        this.price = priceDetails;
    }

    @Data
    public static class PriceInfo {
        @NotBlank(message = "Currency is required")
        private String currency = "INR";

        @NotNull(message = "Minimum price is required")
        @Min(value = 0, message = "Minimum price cannot be negative")
        private Integer min;

        @NotNull(message = "Maximum price is required")
        @Min(value = 0, message = "Maximum price cannot be negative")
        private Integer max;

        @NotNull(message = "Rule ID is required")
        private Long ruleId;

        // Compatibility methods for tests
        public Integer getMinCents() {
            return min;
        }

        public void setMinCents(Integer minCents) {
            this.min = minCents;
        }

        public Integer getMaxCents() {
            return max;
        }

        public void setMaxCents(Integer maxCents) {
            this.max = maxCents;
        }
    }
}
