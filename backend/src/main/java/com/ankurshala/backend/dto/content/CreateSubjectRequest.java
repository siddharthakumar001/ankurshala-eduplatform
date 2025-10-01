package com.ankurshala.backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubjectRequest {
    
    private String name;
    
    @JsonProperty("boardId")
    private Long boardId;
    
    @JsonProperty("gradeId")
    private Long gradeId;
    
    private Boolean active = true;
}

