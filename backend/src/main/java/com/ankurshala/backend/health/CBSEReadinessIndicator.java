package com.ankurshala.backend.health;

import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.repository.BoardRepository;
import com.ankurshala.backend.repository.ChapterRepository;
import com.ankurshala.backend.repository.ContentChunkRepository;
import com.ankurshala.backend.repository.GradeRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health indicator to validate CBSE board readiness for student experience.
 * Checks:
 * - Board=CBSE exists and is active
 * - Grades 7-12 exist
 * - At least one subject/chapter/topic exists
 * - For sample topics, ACTIVE content chunks exist (RAG grounding)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CBSEReadinessIndicator implements HealthIndicator {

    private final BoardRepository boardRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final ContentChunkRepository contentChunkRepository;

    private static final String CBSE_BOARD_NAME = "CBSE";
    private static final List<String> REQUIRED_GRADES = List.of("7", "8", "9", "10", "11", "12");

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean healthy = true;
        List<String> issues = new ArrayList<>();

        try {
            // Check if CBSE board exists
            var cbseBoard = boardRepository.findByNameIgnoreCase(CBSE_BOARD_NAME);
            if (cbseBoard.isEmpty() || !cbseBoard.get().getActive()) {
                healthy = false;
                issues.add("CBSE board not found or inactive");
                details.put("board", Map.of(
                        "exists", false,
                        "status", "MISSING"
                ));
            } else {
                Long boardId = cbseBoard.get().getId();
                details.put("board", Map.of(
                        "exists", true,
                        "id", boardId,
                        "name", CBSE_BOARD_NAME,
                        "active", cbseBoard.get().getActive()
                ));

                // Check if grades 7-12 exist
                List<String> foundGrades = new ArrayList<>();
                List<String> missingGrades = new ArrayList<>();

                for (String gradeName : REQUIRED_GRADES) {
                    var grade = gradeRepository.findByBoardIdAndName(boardId, gradeName);
                    if (grade.isPresent() && grade.get().getActive() && !grade.get().getSoftDeleted()) {
                        foundGrades.add(gradeName);
                    } else {
                        missingGrades.add(gradeName);
                    }
                }

                if (!missingGrades.isEmpty()) {
                    healthy = false;
                    issues.add("Missing grades: " + String.join(", ", missingGrades));
                }

                details.put("grades", Map.of(
                        "required", REQUIRED_GRADES,
                        "found", foundGrades,
                        "missing", missingGrades,
                        "allPresent", missingGrades.isEmpty()
                ));

                // Check if at least one subject exists for CBSE
                long subjectCount = subjectRepository.countSubjectsByBoardIdAndSoftDeletedFalse(boardId);
                if (subjectCount == 0) {
                    healthy = false;
                    issues.add("No subjects found for CBSE board");
                }
                details.put("subjects", Map.of(
                        "count", subjectCount,
                        "present", subjectCount > 0
                ));

                // Check if at least one chapter exists for CBSE
                long chapterCount = chapterRepository.countChaptersByBoardIdAndSoftDeletedFalse(boardId);
                if (chapterCount == 0) {
                    // Try alternative approach - check if any topics exist (which implies chapters exist)
                    chapterCount = topicRepository.findTopicsWithFilters(
                            boardId, null, null, null, null, null,
                            PageRequest.of(0, 1)
                    ).getTotalElements() > 0 ? 1L : 0L;
                }

                details.put("chapters", Map.of(
                        "count", chapterCount,
                        "present", chapterCount > 0
                ));

                // Check if at least one topic exists for CBSE
                long topicCount = topicRepository.findTopicsWithFilters(
                        boardId, null, null, null, null, true,
                        PageRequest.of(0, 1)
                ).getTotalElements();

                if (topicCount == 0) {
                    healthy = false;
                    issues.add("No topics found for CBSE board");
                }
                details.put("topics", Map.of(
                        "count", topicCount,
                        "present", topicCount > 0
                ));

                // Sample check: verify ACTIVE content chunks exist for sample topics
                if (topicCount > 0) {
                    Pageable samplePage = PageRequest.of(0, 10);
                    var sampleTopics = topicRepository.findTopicsWithFilters(
                            boardId, null, null, null, null, true,
                            samplePage
                    ).getContent();

                    List<Map<String, Object>> topicChunkChecks = new ArrayList<>();
                    int topicsWithChunks = 0;
                    int topicsWithoutChunks = 0;

                    for (var topic : sampleTopics) {
                        List<ContentChunk> chunks = contentChunkRepository.findByTopicIdAndStatus(
                                topic.getId(), ContentChunk.ChunkStatus.ACTIVE
                        );
                        boolean hasChunks = !chunks.isEmpty();

                        if (hasChunks) {
                            topicsWithChunks++;
                        } else {
                            topicsWithoutChunks++;
                        }

                        topicChunkChecks.add(Map.of(
                                "topicId", topic.getId(),
                                "topicTitle", topic.getTitle() != null ? topic.getTitle() : "N/A",
                                "hasActiveChunks", hasChunks,
                                "chunkCount", chunks.size()
                        ));
                    }

                    // If no topics have chunks, that's a critical issue
                    if (topicsWithChunks == 0 && !sampleTopics.isEmpty()) {
                        healthy = false;
                        issues.add("No ACTIVE content chunks found for sampled CBSE topics (RAG grounding unavailable)");
                    }

                    details.put("contentChunks", Map.of(
                            "sampleTopicsChecked", sampleTopics.size(),
                            "topicsWithActiveChunks", topicsWithChunks,
                            "topicsWithoutActiveChunks", topicsWithoutChunks,
                            "ragGroundingAvailable", topicsWithChunks > 0,
                            "sampleChecks", topicChunkChecks
                    ));
                }
            }

            // Summary
            details.put("ready", healthy);
            details.put("issues", issues);
            details.put("summary", issues.isEmpty() 
                    ? "CBSE board is ready for student experience" 
                    : "CBSE board has readiness issues: " + String.join("; ", issues));

        } catch (Exception e) {
            log.error("CBSE readiness check failed", e);
            healthy = false;
            details.put("error", e.getMessage());
            details.put("ready", false);
            details.put("summary", "CBSE readiness check encountered an error: " + e.getMessage());
        }

        if (healthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}

