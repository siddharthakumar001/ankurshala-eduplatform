package com.ankurshala.backend.dto.admin;

import lombok.Data;

@Data
public class SystemStatusDto {
    private String name;
    private String status;
    private String latency;
    private String uptime;
}
