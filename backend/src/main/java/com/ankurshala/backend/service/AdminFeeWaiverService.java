package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.CreateFeeWaiverRequest;
import com.ankurshala.backend.dto.admin.FeeWaiverDto;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.FeeWaiver;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.FeeWaiverRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AdminFeeWaiverService {

    @Autowired
    private FeeWaiverRepository feeWaiverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Page<FeeWaiverDto> getFeeWaivers(Long userId, Long bookingId, Pageable pageable) {
        Page<FeeWaiver> waivers = feeWaiverRepository.findAll(pageable);
        return waivers.map(this::convertToDto);
    }

    public FeeWaiverDto createFeeWaiver(CreateFeeWaiverRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FeeWaiver waiver = new FeeWaiver();
        waiver.setUser(user);
        waiver.setReason(request.getReason());
        waiver.setWaiverAmountCents(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        
        // Note: Booking integration not implemented in current FeeWaiver entity

        FeeWaiver savedWaiver = feeWaiverRepository.save(waiver);
        return convertToDto(savedWaiver);
    }

    public Map<String, Object> getFeeWaiverStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = feeWaiverRepository.count();
        stats.put("totalWaivers", totalCount);
        
        // Last 30 days (mock implementation)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("waiversLast30Days", totalCount);
        
        // Total amount waived in last 30 days (mock implementation)
        stats.put("totalAmountLast30Days", 0.0);
        
        // Last 7 days (mock implementation)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        stats.put("waiversLast7Days", totalCount);
        
        stats.put("totalAmountLast7Days", 0.0);

        return stats;
    }

    private FeeWaiverDto convertToDto(FeeWaiver waiver) {
        return new FeeWaiverDto(
                waiver.getId(),
                null, // bookingId not available in current entity
                waiver.getUser().getId(),
                waiver.getUser().getEmail(),
                waiver.getReason(),
                BigDecimal.valueOf(waiver.getWaiverAmountCents()).divide(BigDecimal.valueOf(100)), // Convert cents to amount
                waiver.getCreatedAt()
        );
    }
}
