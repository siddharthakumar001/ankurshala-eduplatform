package com.ankurshala.backend.dto.admin.content;

/**
 * Simple DTO for dropdown lists
 */
public class DropdownDto {
    private Long id;
    private String name;
    private String displayName;
    private Boolean active;

    public DropdownDto() {}

    public DropdownDto(Long id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = name;
        this.active = true;
    }

    public DropdownDto(Long id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
