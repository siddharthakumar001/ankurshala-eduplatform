package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.ClassLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
    Optional<StudentProfile> findByUserId(Long userId);
    long countByUserEnabledTrue();
    long countByUserEnabledFalse();
    long countByUserCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Search and filter methods with COALESCE + LOWER safe version
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(COALESCE(sp.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(sp.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(sp.middleName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(sp.mobileNumber, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(sp.schoolName, '')) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:educationalBoard IS NULL OR sp.educationalBoard = :educationalBoard) AND " +
           "(:classLevel IS NULL OR sp.classLevel = :classLevel)")
    Page<StudentProfile> findStudentsWithFilters(
            @Param("search") String search,
            @Param("enabled") Boolean enabled,
            @Param("educationalBoard") EducationalBoard educationalBoard,
            @Param("classLevel") ClassLevel classLevel,
            Pageable pageable);

    // Simple query for testing
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u")
    Page<StudentProfile> findAllWithUser(Pageable pageable);

    // Count students by educational board
    long countByEducationalBoard(EducationalBoard educationalBoard);

    // Count students by class level
    long countByClassLevel(ClassLevel classLevel);

    // Count students by school
    long countBySchoolName(String schoolName);
}
