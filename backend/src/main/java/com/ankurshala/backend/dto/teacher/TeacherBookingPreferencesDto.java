package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class TeacherBookingPreferencesDto {
    private Long id;
    
    @NotNull(message = "Auto accept bookings preference is required")
    private Boolean autoAcceptBookings;
    
    @NotNull(message = "Advance booking days is required")
    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Max(value = 30, message = "Advance booking days cannot exceed 30")
    private Integer advanceBookingDays;
    
    @NotNull(message = "Minimum session duration is required")
    @Min(value = 30, message = "Minimum session duration must be at least 30 minutes")
    @Max(value = 120, message = "Minimum session duration cannot exceed 120 minutes")
    private Integer minimumSessionDuration;
    
    @NotNull(message = "Maximum session duration is required")
    @Min(value = 30, message = "Maximum session duration must be at least 30 minutes")
    @Max(value = 240, message = "Maximum session duration cannot exceed 240 minutes")
    private Integer maximumSessionDuration;
    
    @NotNull(message = "Cancellation policy hours is required")
    @Min(value = 1, message = "Cancellation policy must be at least 1 hour")
    @Max(value = 168, message = "Cancellation policy cannot exceed 168 hours (7 days)")
    private Integer cancellationPolicyHours;
    
    public TeacherBookingPreferencesDto() {}
    
    public TeacherBookingPreferencesDto(Boolean autoAcceptBookings, Integer advanceBookingDays, 
                                      Integer minimumSessionDuration, Integer maximumSessionDuration, 
                                      Integer cancellationPolicyHours) {
        this.autoAcceptBookings = autoAcceptBookings;
        this.advanceBookingDays = advanceBookingDays;
        this.minimumSessionDuration = minimumSessionDuration;
        this.maximumSessionDuration = maximumSessionDuration;
        this.cancellationPolicyHours = cancellationPolicyHours;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boolean getAutoAcceptBookings() { return autoAcceptBookings; }
    public void setAutoAcceptBookings(Boolean autoAcceptBookings) { this.autoAcceptBookings = autoAcceptBookings; }
    
    public Integer getAdvanceBookingDays() { return advanceBookingDays; }
    public void setAdvanceBookingDays(Integer advanceBookingDays) { this.advanceBookingDays = advanceBookingDays; }
    
    public Integer getMinimumSessionDuration() { return minimumSessionDuration; }
    public void setMinimumSessionDuration(Integer minimumSessionDuration) { this.minimumSessionDuration = minimumSessionDuration; }
    
    public Integer getMaximumSessionDuration() { return maximumSessionDuration; }
    public void setMaximumSessionDuration(Integer maximumSessionDuration) { this.maximumSessionDuration = maximumSessionDuration; }
    
    public Integer getCancellationPolicyHours() { return cancellationPolicyHours; }
    public void setCancellationPolicyHours(Integer cancellationPolicyHours) { this.cancellationPolicyHours = cancellationPolicyHours; }
}
