package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeacherDocumentDto {
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    private String documentType;
    
    @NotBlank
    @Size(max = 500)
    private String documentUrl;
    
    @Size(max = 255)
    private String documentName;

    // Constructors
    public TeacherDocumentDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

}
