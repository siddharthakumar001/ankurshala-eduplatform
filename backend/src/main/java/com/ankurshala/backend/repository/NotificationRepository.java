package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Notification;
import com.ankurshala.backend.entity.NotificationAudience;
import com.ankurshala.backend.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.id IN :notificationIds")
    int markAsRead(@Param("userId") Long userId, @Param("notificationIds") List<Long> notificationIds);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    int markAllAsRead(@Param("userId") Long userId);
    
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime since);
    
    // Method for compatibility with User entity
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE (:userId IS NULL OR n.userId = :userId) " +
           "AND (:audience IS NULL OR n.audience = :audience) " +
           "AND (:status IS NULL OR n.status = :status)")
    Page<Notification> findFiltered(
            @Param("userId") Long userId,
            @Param("audience") NotificationAudience audience,
            @Param("status") NotificationStatus status,
            Pageable pageable
    );
}
