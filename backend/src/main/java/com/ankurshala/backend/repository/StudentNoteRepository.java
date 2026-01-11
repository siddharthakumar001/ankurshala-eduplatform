package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentNote;
import com.ankurshala.backend.entity.StudentNote.NoteFormat;
import com.ankurshala.backend.entity.StudentNote.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentNote entity.
 * Supports filtering by topic, subject, format, and favorites.
 */
@Repository
public interface StudentNoteRepository extends JpaRepository<StudentNote, Long> {

    /**
     * Find all notes for a student with active status
     */
    List<StudentNote> findByStudentIdAndStatusOrderByCreatedAtDesc(Long studentId, NoteStatus status);

    /**
     * Find all notes for a student (paginated)
     */
    Page<StudentNote> findByStudentIdAndStatus(Long studentId, NoteStatus status, Pageable pageable);

    /**
     * Find notes for a specific topic
     */
    List<StudentNote> findByStudentIdAndTopicIdAndStatus(Long studentId, Long topicId, NoteStatus status);

    /**
     * Find notes for a specific subject
     */
    List<StudentNote> findByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, NoteStatus status);

    /**
     * Find notes by format
     */
    List<StudentNote> findByStudentIdAndFormatAndStatus(Long studentId, NoteFormat format, NoteStatus status);

    /**
     * Find favorite notes
     */
    List<StudentNote> findByStudentIdAndIsFavoriteAndStatus(Long studentId, Boolean isFavorite, NoteStatus status);

    /**
     * Find note by ID and student (for ownership validation)
     */
    Optional<StudentNote> findByIdAndStudentId(Long id, Long studentId);

    /**
     * Check if student has a note for a specific topic and format
     */
    boolean existsByStudentIdAndTopicIdAndFormatAndStatus(Long studentId, Long topicId, NoteFormat format, NoteStatus status);

    /**
     * Count notes for a student
     */
    long countByStudentIdAndStatus(Long studentId, NoteStatus status);

    /**
     * Count notes by format for a student
     */
    long countByStudentIdAndFormatAndStatus(Long studentId, NoteFormat format, NoteStatus status);

    /**
     * Find notes with filters (comprehensive query)
     */
    @Query("SELECT n FROM StudentNote n WHERE n.studentId = :studentId " +
           "AND n.status = :status " +
           "AND (:topicId IS NULL OR n.topicId = :topicId) " +
           "AND (:subjectId IS NULL OR n.subjectId = :subjectId) " +
           "AND (:format IS NULL OR n.format = :format) " +
           "AND (:language IS NULL OR n.language = :language) " +
           "ORDER BY n.createdAt DESC")
    List<StudentNote> findWithFilters(
            @Param("studentId") Long studentId,
            @Param("status") NoteStatus status,
            @Param("topicId") Long topicId,
            @Param("subjectId") Long subjectId,
            @Param("format") NoteFormat format,
            @Param("language") String language);

    /**
     * Search notes by title (case-insensitive)
     */
    @Query("SELECT n FROM StudentNote n WHERE n.studentId = :studentId " +
           "AND n.status = :status " +
           "AND LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY n.createdAt DESC")
    List<StudentNote> searchByTitle(
            @Param("studentId") Long studentId,
            @Param("status") NoteStatus status,
            @Param("searchTerm") String searchTerm);

    /**
     * Get recent notes for dashboard
     */
    @Query("SELECT n FROM StudentNote n WHERE n.studentId = :studentId " +
           "AND n.status = 'ACTIVE' " +
           "ORDER BY n.updatedAt DESC")
    List<StudentNote> findRecentNotes(@Param("studentId") Long studentId, Pageable pageable);
}

