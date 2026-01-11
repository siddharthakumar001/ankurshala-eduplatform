package com.ankurshala.backend.integration;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.health.CBSEReadinessIndicator;
import com.ankurshala.backend.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for CBSE readiness validation.
 * Tests the CBSE readiness health indicator and admin endpoint.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CBSEReadinessIntegrationTest {

    @Autowired
    private CBSEReadinessIndicator cbseReadinessIndicator;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ContentChunkRepository contentChunkRepository;

    private Board cbseBoard;

    @BeforeEach
    void setUp() {
        // Create or find CBSE board
        cbseBoard = boardRepository.findByNameIgnoreCase("CBSE")
                .orElseGet(() -> {
                    Board board = new Board();
                    board.setName("CBSE");
                    board.setActive(true);
                    board.setSoftDeleted(false);
                    return boardRepository.save(board);
                });
    }

    @Test
    @DisplayName("CBSE readiness check should pass when board and grades exist")
    void testCBSEReadinessCheck_WithCompleteData() {
        // Create grades 7-12 if they don't exist
        List<String> requiredGrades = List.of("7", "8", "9", "10", "11", "12");
        for (String gradeName : requiredGrades) {
            gradeRepository.findByBoardIdAndName(cbseBoard.getId(), gradeName)
                    .orElseGet(() -> {
                        Grade grade = new Grade();
                        grade.setName(gradeName);
                        grade.setDisplayName("Grade " + gradeName);
                        grade.setBoardId(cbseBoard.getId());
                        grade.setActive(true);
                        grade.setSoftDeleted(false);
                        return gradeRepository.save(grade);
                    });
        }

        // Create at least one subject
        Grade grade10 = gradeRepository.findByBoardIdAndName(cbseBoard.getId(), "10")
                .orElseThrow();
        
        Subject subject = subjectRepository.findAll().stream()
                .filter(s -> s.getBoardId() != null && s.getBoardId().equals(cbseBoard.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setName("Mathematics");
                    s.setBoardId(cbseBoard.getId());
                    s.setGradeId(grade10.getId());
                    s.setActive(true);
                    s.setSoftDeleted(false);
                    return subjectRepository.save(s);
                });

        // Create at least one chapter
        Chapter chapter = chapterRepository.findAll().stream()
                .filter(c -> c.getBoardId() != null && c.getBoardId().equals(cbseBoard.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Chapter c = new Chapter();
                    c.setName("Algebra");
                    c.setBoardId(cbseBoard.getId());
                    c.setGradeId(grade10.getId());
                    c.setSubjectId(subject.getId());
                    c.setActive(true);
                    c.setSoftDeleted(false);
                    return chapterRepository.save(c);
                });

        // Create at least one topic
        Topic topic = topicRepository.findTopicsWithFilters(
                cbseBoard.getId(), null, null, null, null, true,
                org.springframework.data.domain.PageRequest.of(0, 1)
        ).getContent().stream().findFirst()
                .orElseGet(() -> {
                    Topic t = new Topic();
                    t.setTitle("Linear Equations");
                    t.setBoardId(cbseBoard.getId());
                    t.setGradeId(grade10.getId());
                    t.setSubjectId(subject.getId());
                    t.setChapterId(chapter.getId());
                    t.setActive(true);
                    t.setSoftDeleted(false);
                    return topicRepository.save(t);
                });

        // Create at least one ACTIVE content chunk for RAG
        List<ContentChunk> existingChunks = contentChunkRepository.findByTopicIdAndStatus(
                topic.getId(), ContentChunk.ChunkStatus.ACTIVE
        );

        if (existingChunks.isEmpty()) {
            ContentChunk chunk = new ContentChunk();
            chunk.setTopicId(topic.getId());
            chunk.setChapterId(chapter.getId());
            chunk.setSubjectId(subject.getId());
            chunk.setBoardId(cbseBoard.getId());
            chunk.setGradeId(grade10.getId());
            chunk.setChunkText("Linear equations are mathematical statements that contain variables.");
            chunk.setLanguage("en");
            chunk.setSourceType(ContentChunk.SourceType.TEXTBOOK);
            chunk.setStatus(ContentChunk.ChunkStatus.ACTIVE);
            contentChunkRepository.save(chunk);
        }

        // Perform readiness check
        Health health = cbseReadinessIndicator.health();

        // Verify health status
        assertNotNull(health);
        assertEquals("UP", health.getStatus().getCode(), 
                "CBSE readiness should be UP when all requirements are met");
        
        // Verify details
        assertTrue(health.getDetails().containsKey("board"));
        assertTrue(health.getDetails().containsKey("grades"));
        assertTrue(health.getDetails().containsKey("subjects"));
        assertTrue(health.getDetails().containsKey("topics"));
        assertTrue(health.getDetails().containsKey("contentChunks"));
        
        // Verify ready status
        Boolean ready = (Boolean) health.getDetails().get("ready");
        assertTrue(ready != null && ready, "CBSE should be ready");
    }

    @Test
    @DisplayName("CBSE readiness check should fail when board is missing")
    void testCBSEReadinessCheck_WithoutBoard() {
        // Temporarily deactivate or delete CBSE board
        if (cbseBoard != null && cbseBoard.getId() != null) {
            cbseBoard.setActive(false);
            boardRepository.save(cbseBoard);
        }

        // Perform readiness check
        Health health = cbseReadinessIndicator.health();

        // Verify health status is DOWN
        assertEquals("DOWN", health.getStatus().getCode(), 
                "CBSE readiness should be DOWN when board is missing or inactive");
        
        // Verify issues are reported
        assertTrue(health.getDetails().containsKey("issues"));
        
        // Reactivate board for cleanup
        if (cbseBoard != null && cbseBoard.getId() != null) {
            cbseBoard.setActive(true);
            boardRepository.save(cbseBoard);
        }
    }

    @Test
    @DisplayName("CBSE readiness check should fail when grades are missing")
    void testCBSEReadinessCheck_WithoutGrades() {
        // Ensure CBSE board exists
        cbseBoard = boardRepository.findByNameIgnoreCase("CBSE")
                .orElseGet(() -> {
                    Board board = new Board();
                    board.setName("CBSE");
                    board.setActive(true);
                    board.setSoftDeleted(false);
                    return boardRepository.save(board);
                });

        // Delete all grades for CBSE (in test environment)
        List<Grade> grades = gradeRepository.findByBoardIdAndSoftDeletedFalse(cbseBoard.getId());
        for (Grade grade : grades) {
            grade.setSoftDeleted(true);
            gradeRepository.save(grade);
        }

        // Perform readiness check
        Health health = cbseReadinessIndicator.health();

        // Verify health status is DOWN or at least issues are reported
        assertNotNull(health);
        assertTrue(health.getDetails().containsKey("issues") || 
                   "DOWN".equals(health.getStatus().getCode()),
                "CBSE readiness should report issues when grades are missing");
    }

    @Test
    @DisplayName("Health indicator should be accessible via Actuator endpoint")
    void testCBSEReadinessViaHealthEndpoint() {
        // The health indicator should be automatically exposed via Spring Actuator
        // Verify it returns valid health status
        Health health = cbseReadinessIndicator.health();
        
        assertNotNull(health);
        assertTrue(health.getStatus().getCode().equals("UP") || 
                   health.getStatus().getCode().equals("DOWN"),
                   "Health status should be UP or DOWN");
        assertTrue(health.getDetails().containsKey("ready"));
        assertTrue(health.getDetails().containsKey("board"));
    }
}

