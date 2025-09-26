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

    @Query("SELECT n FROM Notification n WHERE n.status = 'QUEUED' ORDER BY n.createdAt ASC")
    List<Notification> findQueuedNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    long countByStatus(@Param("status") Notification.NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :from AND n.createdAt <= :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
