package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.ContentChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentChunkRepository extends JpaRepository<ContentChunk, Long> {

    List<ContentChunk> findByTopicIdAndStatus(Long topicId, ContentChunk.ChunkStatus status);

    Page<ContentChunk> findByTopicId(Long topicId, Pageable pageable);

    List<ContentChunk> findByChapterIdAndStatus(Long chapterId, ContentChunk.ChunkStatus status);

    List<ContentChunk> findBySubjectIdAndStatus(Long subjectId, ContentChunk.ChunkStatus status);

    Page<ContentChunk> findByStatus(ContentChunk.ChunkStatus status, Pageable pageable);

    /**
     * Find chunks by topic and language
     */
    List<ContentChunk> findByTopicIdAndLanguageAndStatus(
            Long topicId,
            String language,
            ContentChunk.ChunkStatus status);

    /**
     * Find chunks by source type
     */
    List<ContentChunk> findBySourceTypeAndStatus(
            ContentChunk.SourceType sourceType,
            ContentChunk.ChunkStatus status);

    /**
     * Find chunks for RAG by filters
     */
    @Query("SELECT cc FROM ContentChunk cc WHERE cc.status = 'ACTIVE' " +
           "AND (:topicId IS NULL OR cc.topicId = :topicId) " +
           "AND (:subjectId IS NULL OR cc.subjectId = :subjectId) " +
           "AND (:gradeId IS NULL OR cc.gradeId = :gradeId) " +
           "AND (:boardId IS NULL OR cc.boardId = :boardId) " +
           "AND (:language IS NULL OR cc.language = :language)")
    List<ContentChunk> findByFilters(
            @Param("topicId") Long topicId,
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId,
            @Param("language") String language);

    /**
     * Semantic search using pgvector cosine similarity
     * Note: This requires the embedding to be passed as a properly formatted vector string
     */
    @Query(value = "SELECT * FROM content_chunks cc " +
           "WHERE cc.status = 'ACTIVE' " +
           "AND cc.embedding IS NOT NULL " +
           "AND (:topicId IS NULL OR cc.topic_id = :topicId) " +
           "AND (:subjectId IS NULL OR cc.subject_id = :subjectId) " +
           "AND (:gradeId IS NULL OR cc.grade_id = :gradeId) " +
           "AND (:boardId IS NULL OR cc.board_id = :boardId) " +
           "AND (:language IS NULL OR cc.language = :language) " +
           "ORDER BY cc.embedding <=> CAST(:queryEmbedding AS vector) " +
           "LIMIT :limit",
           nativeQuery = true)
    List<ContentChunk> findSimilarChunks(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topicId") Long topicId,
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId,
            @Param("language") String language,
            @Param("limit") int limit);

    /**
     * Semantic search with similarity score
     */
    @Query(value = "SELECT cc.*, 1 - (cc.embedding <=> CAST(:queryEmbedding AS vector)) as similarity " +
           "FROM content_chunks cc " +
           "WHERE cc.status = 'ACTIVE' " +
           "AND cc.embedding IS NOT NULL " +
           "AND (:topicId IS NULL OR cc.topic_id = :topicId) " +
           "ORDER BY cc.embedding <=> CAST(:queryEmbedding AS vector) " +
           "LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findSimilarChunksWithScore(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topicId") Long topicId,
            @Param("limit") int limit);

    /**
     * Update embedding for a chunk
     */
    @Modifying
    @Query(value = "UPDATE content_chunks SET embedding = CAST(:embedding AS vector) " +
           "WHERE id = :chunkId",
           nativeQuery = true)
    void updateEmbedding(
            @Param("chunkId") Long chunkId,
            @Param("embedding") String embedding);

    /**
     * Count chunks by topic
     */
    Long countByTopicIdAndStatus(Long topicId, ContentChunk.ChunkStatus status);

    /**
     * Find unverified chunks for review
     */
    @Query("SELECT cc FROM ContentChunk cc " +
           "WHERE cc.status = 'PENDING_REVIEW' OR cc.isVerified = false " +
           "ORDER BY cc.createdAt ASC")
    Page<ContentChunk> findUnverifiedChunks(Pageable pageable);

    /**
     * Count chunks without embeddings
     */
    @Query(value = "SELECT COUNT(*) FROM content_chunks WHERE embedding IS NULL AND status = 'ACTIVE'",
           nativeQuery = true)
    Long countChunksWithoutEmbeddings();
}

