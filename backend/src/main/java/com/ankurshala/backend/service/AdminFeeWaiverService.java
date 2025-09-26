package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.CreateFeeWaiverRequest;
import com.ankurshala.backend.dto.admin.FeeWaiverDto;
import com.ankurshala.backend.entity.FeeWaiver;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.FeeWaiverRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<FeeWaiverDto> getFeeWaivers(Long userId, Long bookingId, Pageable pageable) {
        Page<FeeWaiver> waivers = feeWaiverRepository.findFeeWaiversWithFilters(userId, bookingId, pageable);
        return waivers.map(this::convertToDto);
    }

    public FeeWaiverDto createFeeWaiver(CreateFeeWaiverRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FeeWaiver waiver = new FeeWaiver(
                request.getBookingId(),
                user,
                request.getReason(),
                request.getAmount()
        );

        FeeWaiver savedWaiver = feeWaiverRepository.save(waiver);
        return convertToDto(savedWaiver);
    }

    public Map<String, Object> getFeeWaiverStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalWaivers", feeWaiverRepository.count());
        
        // Last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("waiversLast30Days", feeWaiverRepository.countByCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now()));
        
        // Total amount waived in last 30 days
        Double totalAmountLast30Days = feeWaiverRepository.sumAmountByCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());
        stats.put("totalAmountLast30Days", totalAmountLast30Days != null ? totalAmountLast30Days : 0.0);
        
        // Last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        stats.put("waiversLast7Days", feeWaiverRepository.countByCreatedAtBetween(sevenDaysAgo, LocalDateTime.now()));
        
        Double totalAmountLast7Days = feeWaiverRepository.sumAmountByCreatedAtBetween(sevenDaysAgo, LocalDateTime.now());
        stats.put("totalAmountLast7Days", totalAmountLast7Days != null ? totalAmountLast7Days : 0.0);

        return stats;
    }

    private FeeWaiverDto convertToDto(FeeWaiver waiver) {
        return new FeeWaiverDto(
                waiver.getId(),
                waiver.getBookingId(),
                waiver.getUser().getId(),
                waiver.getUser().getEmail(),
                waiver.getReason(),
                waiver.getAmount(),
                waiver.getCreatedAt()
        );
    }
}
