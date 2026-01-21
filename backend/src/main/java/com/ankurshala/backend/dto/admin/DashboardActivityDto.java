package com.ankurshala.backend.dto.admin;

import lombok.Data;

@Data
public class DashboardActivityDto {
    private String id;
    private String type;
    private String title;
    private String description;
    private String timestamp;
    private String user;
}
