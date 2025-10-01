package com.ankurshala.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/simple")
    public String testSimple() {
        System.out.println("DEBUG: TestController.testSimple called");
        return "Test controller is working";
    }
}


