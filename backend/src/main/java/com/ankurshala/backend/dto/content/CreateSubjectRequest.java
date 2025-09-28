package com.ankurshala.backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubjectRequest {
    
    private String name;
    
    private Long boardId;
    
    private Long gradeId;
    
    private Boolean active = true;
}

