package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.CreateFeeWaiverRequest;
import com.ankurshala.backend.dto.admin.FeeWaiverDto;
import com.ankurshala.backend.service.AdminFeeWaiverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/fees")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeeWaiverController {

    @Autowired
    private AdminFeeWaiverService feeWaiverService;

    @GetMapping("/waivers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FeeWaiverDto>> getFeeWaivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookingId) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeeWaiverDto> waivers = feeWaiverService.getFeeWaivers(userId, bookingId, pageable);
        
        return ResponseEntity.ok(waivers);
    }

    @PostMapping("/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createFeeWaiver(@Valid @RequestBody CreateFeeWaiverRequest request) {
        FeeWaiverDto waiver = feeWaiverService.createFeeWaiver(request);
        return ResponseEntity.ok(Map.of(
            "waiver", waiver,
            "message", "Fee waiver created successfully"
        ));
    }

    @GetMapping("/waivers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFeeWaiverStats() {
        Map<String, Object> stats = feeWaiverService.getFeeWaiverStats();
        return ResponseEntity.ok(stats);
    }
}
