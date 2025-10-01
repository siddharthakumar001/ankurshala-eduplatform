package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.CreatePricingRuleRequest;
import com.ankurshala.backend.dto.admin.PricingRuleDto;
import com.ankurshala.backend.dto.admin.UpdatePricingRuleRequest;
import com.ankurshala.backend.service.AdminPricingService;
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
@RequestMapping("/admin/pricing")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingController {

    @Autowired
    private AdminPricingService pricingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PricingRuleDto>> getPricingRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Boolean active) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PricingRuleDto> rules = pricingService.getPricingRules(
                boardId, gradeId, subjectId, chapterId, topicId, active, pageable);
        
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingRuleDto> createPricingRule(@Valid @RequestBody CreatePricingRuleRequest request) {
        PricingRuleDto rule = pricingService.createPricingRule(request);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingRuleDto> updatePricingRule(
            @PathVariable Long id, 
            @Valid @RequestBody UpdatePricingRuleRequest request) {
        PricingRuleDto rule = pricingService.updatePricingRule(id, request);
        return ResponseEntity.ok(rule);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> togglePricingRuleStatus(@PathVariable Long id) {
        boolean newStatus = pricingService.togglePricingRuleStatus(id);
        return ResponseEntity.ok(Map.of(
            "id", id,
            "active", newStatus,
            "message", "Pricing rule status updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePricingRule(@PathVariable Long id) {
        pricingService.deletePricingRule(id);
        return ResponseEntity.ok(Map.of(
            "message", "Pricing rule deleted successfully"
        ));
    }

    @GetMapping("/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resolvePricingRule(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long topicId) {
        
        PricingRuleDto rule = pricingService.resolvePricingRule(boardId, gradeId, subjectId, chapterId, topicId);
        return ResponseEntity.ok(Map.of(
            "rule", rule,
            "message", rule != null ? "Pricing rule found" : "No matching pricing rule found"
        ));
    }
}
