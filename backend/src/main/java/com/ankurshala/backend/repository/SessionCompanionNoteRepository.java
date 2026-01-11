package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.SessionCompanionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionCompanionNoteRepository extends JpaRepository<SessionCompanionNote, Long> {

    /**
     * Find all notes for a companion
     */
    List<SessionCompanionNote> findByCompanionIdOrderByCreatedAtAsc(Long companionId);

    /**
     * Find notes by type
     */
    List<SessionCompanionNote> findByCompanionIdAndNoteTypeOrderByCreatedAtAsc(
            Long companionId, SessionCompanionNote.NoteType noteType);

    /**
     * Find unresolved questions
     */
    List<SessionCompanionNote> findByCompanionIdAndNoteTypeAndIsResolvedFalse(
            Long companionId, SessionCompanionNote.NoteType noteType);

    /**
     * Count notes by companion
     */
    long countByCompanionId(Long companionId);
}

