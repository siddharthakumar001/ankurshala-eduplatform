package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Policy;
import com.ankurshala.backend.entity.PolicyAcceptance;
import com.ankurshala.backend.entity.PolicyCategory;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.PolicyAcceptanceRepository;
import com.ankurshala.backend.repository.PolicyCategoryRepository;
import com.ankurshala.backend.repository.PolicyRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private PolicyCategoryRepository policyCategoryRepository;
    @Autowired
    private PolicyAcceptanceRepository policyAcceptanceRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Policy> getActivePolicies() {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching active policies - TraceId: {}", traceId);

        return policyRepository.findByActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNull(
                LocalDateTime.now());
    }

    public List<Policy> getPoliciesByCategory(String categoryName) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching policies by category - TraceId: {}, Category: {}", traceId, categoryName);

        PolicyCategory category = policyCategoryRepository.findByNameAndActiveTrue(categoryName)
                .orElseThrow(() -> new BusinessException("Policy category not found", HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"));

        return policyRepository.findByCategoryAndActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNull(
                category, LocalDateTime.now());
    }

    public Policy getLatestPolicyByCategory(String categoryName) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching latest policy by category - TraceId: {}, Category: {}", traceId, categoryName);

        PolicyCategory category = policyCategoryRepository.findByNameAndActiveTrue(categoryName)
                .orElseThrow(() -> new BusinessException("Policy category not found", HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND"));

        return policyRepository.findTopByCategoryAndActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNullOrderByEffectiveDateDesc(
                category, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("No active policy found for category", HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND"));
    }

    public PolicyAcceptance acceptPolicy(Long userId, Long policyId, String ipAddress, String userAgent) {
        String traceId = TraceUtil.getTraceId();
        log.info("User accepting policy - TraceId: {}, UserId: {}, PolicyId: {}", traceId, userId, policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException("Policy not found", HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND"));

        if (!policy.getActive()) {
            throw new BusinessException("Policy is not active", HttpStatus.BAD_REQUEST, "POLICY_INACTIVE");
        }

        if (policy.getEffectiveDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Policy is not yet effective", HttpStatus.BAD_REQUEST, "POLICY_NOT_EFFECTIVE");
        }

        if (policy.getExpiryDate() != null && policy.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Policy has expired", HttpStatus.BAD_REQUEST, "POLICY_EXPIRED");
        }

        // Check if user has already accepted this policy
        Optional<PolicyAcceptance> existingAcceptance = policyAcceptanceRepository.findByUserIdAndPolicyId(userId, policyId);
        if (existingAcceptance.isPresent()) {
            log.info("User has already accepted this policy - TraceId: {}, UserId: {}, PolicyId: {}", traceId, userId, policyId);
            return existingAcceptance.get();
        }

        PolicyAcceptance acceptance = new PolicyAcceptance();
        acceptance.setUser(userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND")));
        acceptance.setPolicy(policyRepository.findById(policyId).orElseThrow(() -> new BusinessException("Policy not found", HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND")));
        acceptance.setIpAddress(ipAddress);
        acceptance.setUserAgent(userAgent);

        PolicyAcceptance savedAcceptance = policyAcceptanceRepository.save(acceptance);
        log.info("Policy acceptance saved - TraceId: {}, AcceptanceId: {}", traceId, savedAcceptance.getId());

        return savedAcceptance;
    }

    public boolean hasUserAcceptedPolicy(Long userId, Long policyId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Checking if user has accepted policy - TraceId: {}, UserId: {}, PolicyId: {}", traceId, userId, policyId);

        return policyAcceptanceRepository.existsByUserIdAndPolicyId(userId, policyId);
    }

    public boolean hasUserAcceptedLatestPolicy(Long userId, String categoryName) {
        String traceId = TraceUtil.getTraceId();
        log.info("Checking if user has accepted latest policy - TraceId: {}, UserId: {}, Category: {}", traceId, userId, categoryName);

        Policy latestPolicy = getLatestPolicyByCategory(categoryName);
        return hasUserAcceptedPolicy(userId, latestPolicy.getId());
    }

    public List<PolicyAcceptance> getUserPolicyAcceptances(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching user policy acceptances - TraceId: {}, UserId: {}", traceId, userId);

        return policyAcceptanceRepository.findByUserIdOrderByAcceptedAtDesc(userId);
    }

    public Policy createPolicy(Policy policy) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating new policy - TraceId: {}, Title: {}", traceId, policy.getTitle());

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy created - TraceId: {}, PolicyId: {}", traceId, savedPolicy.getId());

        return savedPolicy;
    }

    public Policy updatePolicy(Long policyId, Policy policy) {
        String traceId = TraceUtil.getTraceId();
        log.info("Updating policy - TraceId: {}, PolicyId: {}", traceId, policyId);

        Policy existingPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException("Policy not found", HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND"));

        existingPolicy.setTitle(policy.getTitle());
        existingPolicy.setContent(policy.getContent());
        existingPolicy.setVersion(policy.getVersion());
        existingPolicy.setEffectiveDate(policy.getEffectiveDate());
        existingPolicy.setExpiryDate(policy.getExpiryDate());
        existingPolicy.setActive(policy.getActive());

        Policy savedPolicy = policyRepository.save(existingPolicy);
        log.info("Policy updated - TraceId: {}, PolicyId: {}", traceId, savedPolicy.getId());

        return savedPolicy;
    }

    public void deletePolicy(Long policyId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Deleting policy - TraceId: {}, PolicyId: {}", traceId, policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException("Policy not found", HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND"));

        policyRepository.delete(policy);
        log.info("Policy deleted - TraceId: {}, PolicyId: {}", traceId, policyId);
    }
}
