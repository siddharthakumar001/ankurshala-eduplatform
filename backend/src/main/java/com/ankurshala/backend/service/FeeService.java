package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.FeeApplication;
import com.ankurshala.backend.entity.FeeStructure;
import com.ankurshala.backend.entity.FeeWaiver;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.FeeApplicationRepository;
import com.ankurshala.backend.repository.FeeStructureRepository;
import com.ankurshala.backend.repository.FeeWaiverRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class FeeService {

    @Autowired
    private FeeStructureRepository feeStructureRepository;
    @Autowired
    private FeeApplicationRepository feeApplicationRepository;
    @Autowired
    private FeeWaiverRepository feeWaiverRepository;
    @Autowired
    private UserRepository userRepository;

    public FeeApplication calculateFee(String entityType, Long entityId, Long amountCents, String feeCategoryName) {
        String traceId = TraceUtil.getTraceId();
        log.info("Calculating fee - TraceId: {}, EntityType: {}, EntityId: {}, Amount: {}, Category: {}", 
                traceId, entityType, entityId, amountCents, feeCategoryName);

        FeeStructure feeStructure = feeStructureRepository.findActiveByCategoryNameAndEffectiveDate(feeCategoryName, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("No active fee structure found for category", HttpStatus.NOT_FOUND, "FEE_STRUCTURE_NOT_FOUND"));

        Long feeAmountCents = calculateFeeAmount(feeStructure, amountCents);

        FeeApplication feeApplication = new FeeApplication();
        feeApplication.setFeeStructure(feeStructure);
        feeApplication.setEntityType(entityType);
        feeApplication.setEntityId(entityId);
        feeApplication.setAmountCents(amountCents);
        feeApplication.setFeeAmountCents(feeAmountCents);
        feeApplication.setCalculatedAt(LocalDateTime.now());
        feeApplication.setStatus(FeeApplication.FeeApplicationStatus.PENDING);

        FeeApplication savedApplication = feeApplicationRepository.save(feeApplication);
        log.info("Fee application created - TraceId: {}, ApplicationId: {}, FeeAmount: {}", 
                traceId, savedApplication.getId(), feeAmountCents);

        return savedApplication;
    }

    public FeeApplication applyFee(Long feeApplicationId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Applying fee - TraceId: {}, ApplicationId: {}", traceId, feeApplicationId);

        FeeApplication feeApplication = feeApplicationRepository.findById(feeApplicationId)
                .orElseThrow(() -> new BusinessException("Fee application not found", HttpStatus.NOT_FOUND, "FEE_APPLICATION_NOT_FOUND"));

        if (feeApplication.getStatus() != FeeApplication.FeeApplicationStatus.PENDING) {
            throw new BusinessException("Fee application is not in pending status", HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        feeApplication.setStatus(FeeApplication.FeeApplicationStatus.APPLIED);
        feeApplication.setAppliedAt(LocalDateTime.now());

        FeeApplication savedApplication = feeApplicationRepository.save(feeApplication);
        log.info("Fee applied - TraceId: {}, ApplicationId: {}", traceId, savedApplication.getId());

        return savedApplication;
    }

    public FeeWaiver requestFeeWaiver(Long userId, Long feeApplicationId, String reason, FeeWaiver.WaiverType waiverType, Long waiverAmountCents, BigDecimal waiverPercentage) {
        String traceId = TraceUtil.getTraceId();
        log.info("Requesting fee waiver - TraceId: {}, UserId: {}, ApplicationId: {}", traceId, userId, feeApplicationId);

        FeeApplication feeApplication = feeApplicationRepository.findById(feeApplicationId)
                .orElseThrow(() -> new BusinessException("Fee application not found", HttpStatus.NOT_FOUND, "FEE_APPLICATION_NOT_FOUND"));

        if (feeApplication.getStatus() != FeeApplication.FeeApplicationStatus.PENDING) {
            throw new BusinessException("Fee application is not in pending status", HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        FeeWaiver feeWaiver = new FeeWaiver();
        feeWaiver.setUserId(userId);
        feeWaiver.setFeeApplication(feeApplication);
        feeWaiver.setWaiverType(waiverType);
        feeWaiver.setWaiverAmountCents(waiverAmountCents);
        feeWaiver.setWaiverPercentage(waiverPercentage);
        feeWaiver.setReason(reason);
        feeWaiver.setStatus(FeeWaiver.WaiverStatus.PENDING);

        FeeWaiver savedWaiver = feeWaiverRepository.save(feeWaiver);
        log.info("Fee waiver requested - TraceId: {}, WaiverId: {}", traceId, savedWaiver.getId());

        return savedWaiver;
    }

    public FeeWaiver approveFeeWaiver(Long waiverId, Long approvedBy) {
        String traceId = TraceUtil.getTraceId();
        log.info("Approving fee waiver - TraceId: {}, WaiverId: {}, ApprovedBy: {}", traceId, waiverId, approvedBy);

        FeeWaiver feeWaiver = feeWaiverRepository.findById(waiverId)
                .orElseThrow(() -> new BusinessException("Fee waiver not found", HttpStatus.NOT_FOUND, "FEE_WAIVER_NOT_FOUND"));

        if (feeWaiver.getStatus() != FeeWaiver.WaiverStatus.PENDING) {
            throw new BusinessException("Fee waiver is not in pending status", HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        feeWaiver.setStatus(FeeWaiver.WaiverStatus.APPROVED);
        feeWaiver.setApprovedBy(userRepository.findById(approvedBy).orElse(null));
        feeWaiver.setApprovedAt(LocalDateTime.now());

        // Update the fee application status
        FeeApplication feeApplication = feeWaiver.getFeeApplication();
        feeApplication.setStatus(FeeApplication.FeeApplicationStatus.WAIVED);

        feeWaiverRepository.save(feeWaiver);
        feeApplicationRepository.save(feeApplication);

        log.info("Fee waiver approved - TraceId: {}, WaiverId: {}", traceId, waiverId);

        return feeWaiver;
    }

    public FeeWaiver rejectFeeWaiver(Long waiverId, Long rejectedBy) {
        String traceId = TraceUtil.getTraceId();
        log.info("Rejecting fee waiver - TraceId: {}, WaiverId: {}, RejectedBy: {}", traceId, waiverId, rejectedBy);

        FeeWaiver feeWaiver = feeWaiverRepository.findById(waiverId)
                .orElseThrow(() -> new BusinessException("Fee waiver not found", HttpStatus.NOT_FOUND, "FEE_WAIVER_NOT_FOUND"));

        if (feeWaiver.getStatus() != FeeWaiver.WaiverStatus.PENDING) {
            throw new BusinessException("Fee waiver is not in pending status", HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        feeWaiver.setStatus(FeeWaiver.WaiverStatus.REJECTED);
        feeWaiver.setApprovedBy(userRepository.findById(rejectedBy).orElse(null));
        feeWaiver.setApprovedAt(LocalDateTime.now());

        FeeWaiver savedWaiver = feeWaiverRepository.save(feeWaiver);
        log.info("Fee waiver rejected - TraceId: {}, WaiverId: {}", traceId, waiverId);

        return savedWaiver;
    }

    public List<FeeApplication> getFeeApplicationsByEntity(String entityType, Long entityId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching fee applications by entity - TraceId: {}, EntityType: {}, EntityId: {}", traceId, entityType, entityId);

        return feeApplicationRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    public List<FeeWaiver> getFeeWaiversByUser(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching fee waivers by user - TraceId: {}, UserId: {}", traceId, userId);

        return feeWaiverRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public List<FeeWaiver> getPendingFeeWaivers() {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching pending fee waivers - TraceId: {}", traceId);

        return feeWaiverRepository.findByStatusOrderByCreatedAtDesc(FeeWaiver.WaiverStatus.PENDING);
    }

    private Long calculateFeeAmount(FeeStructure feeStructure, Long amountCents) {
        switch (feeStructure.getFeeType()) {
            case FIXED:
                return feeStructure.getBaseAmountCents();
            case PERCENTAGE:
                BigDecimal percentage = feeStructure.getPercentageRate();
                BigDecimal amount = BigDecimal.valueOf(amountCents);
                BigDecimal feeAmount = amount.multiply(percentage);
                return feeAmount.longValue();
            case TIERED:
                // For tiered fees, we would need to implement tier calculation logic
                // This is a simplified version
                return feeStructure.getBaseAmountCents();
            default:
                return 0L;
        }
    }
}
