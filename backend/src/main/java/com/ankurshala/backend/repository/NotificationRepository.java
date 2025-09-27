package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n")
    Page<Notification> findAllNotifications(Pageable pageable);

    List<Notification> findByStatusOrderByCreatedAtAsc(Notification.NotificationStatus status);

    long countByStatus(Notification.NotificationStatus status);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
