package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.content.*;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.GlobalExceptionHandler;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.service.AdminContentManagementService;
import com.ankurshala.backend.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit tests for AdminContentManagementController
 * Tests all CRUD operations and dropdown endpoints with various scenarios
 */
@ExtendWith(MockitoExtension.class)
class AdminContentManagementControllerTest {

    @Mock
    private AdminContentManagementService contentManagementService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private AdminContentManagementController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Manually inject LoggingService since it uses @Autowired field injection
        try {
            java.lang.reflect.Field loggingServiceField = AdminContentManagementController.class.getDeclaredField("loggingService");
            loggingServiceField.setAccessible(true);
            loggingServiceField.set(controller, loggingService);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        // Inject LoggingService into GlobalExceptionHandler
        try {
            java.lang.reflect.Field globalLoggingServiceField = GlobalExceptionHandler.class.getDeclaredField("loggingService");
            globalLoggingServiceField.setAccessible(true);
            globalLoggingServiceField.set(globalExceptionHandler, loggingService);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ============ BOARD TESTS ============

    @Test
    void testGetBoards_Success() throws Exception {
        // Given
        BoardDto board1 = BoardDto.builder()
                .id(1L)
                .name("CBSE")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        BoardDto board2 = BoardDto.builder()
                .id(2L)
                .name("ICSE")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<BoardDto> boards = Arrays.asList(board1, board2);
        Page<BoardDto> boardPage = new PageImpl<>(boards, PageRequest.of(0, 10), 2);

        when(contentManagementService.getBoards(anyString(), any(Boolean.class), any(Pageable.class)))
                .thenReturn(boardPage);

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "CBSE")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Boards retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("CBSE"))
                .andExpect(jsonPath("$.data.content[1].name").value("ICSE"))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(contentManagementService).getBoards(eq("CBSE"), eq(true), any(Pageable.class));
        verify(loggingService).logBusinessOperationStart(eq("GET_BOARDS"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_BOARDS"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetBoards_EmptyResult() throws Exception {
        // Given
        Page<BoardDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(contentManagementService.getBoards(any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(contentManagementService).getBoards(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetBoards_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getBoards(any(), any(), any(Pageable.class)))
                .thenThrow(new BusinessException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getBoards(isNull(), isNull(), any(Pageable.class));
        verify(loggingService).logError(eq("GET_BOARDS"), any(Exception.class), any());
    }

    @Test
    void testGetBoardById_Success() throws Exception {
        // Given
        BoardDto board = BoardDto.builder()
                .id(1L)
                .name("CBSE")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(contentManagementService.getBoardById(1L)).thenReturn(board);

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Board retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("CBSE"));

        verify(contentManagementService).getBoardById(1L);
        verify(loggingService).logBusinessOperationStart(eq("GET_BOARD_BY_ID"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_BOARD_BY_ID"), eq("1"), eq(true), anyLong());
    }

    @Test
    void testGetBoardById_NotFound() throws Exception {
        // Given
        when(contentManagementService.getBoardById(999L))
                .thenThrow(new ResourceNotFoundException("Board not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards/999"))
                .andExpect(status().isNotFound());

        verify(contentManagementService).getBoardById(999L);
        verify(loggingService).logError(eq("GET_BOARD_BY_ID"), any(Exception.class), any());
    }

    @Test
    void testCreateBoard_Success() throws Exception {
        // Given
        CreateBoardRequest request = new CreateBoardRequest();
        request.setName("New Board");
        request.setActive(true);

        BoardDto createdBoard = BoardDto.builder()
                .id(3L)
                .name("New Board")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(contentManagementService.createBoard(any(CreateBoardRequest.class)))
                .thenReturn(createdBoard);

        // When & Then
        mockMvc.perform(post("/api/admin/content/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Board created successfully"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.name").value("New Board"));

        verify(contentManagementService).createBoard(any(CreateBoardRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("CREATE_BOARD"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("CREATE_BOARD"), eq("3"), eq(true), anyLong());
    }

    @Test
    void testCreateBoard_ValidationError() throws Exception {
        // Given
        CreateBoardRequest request = new CreateBoardRequest();
        request.setName(""); // Invalid: empty name
        request.setActive(true);

        // When & Then
        mockMvc.perform(post("/api/admin/content/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contentManagementService, never()).createBoard(any(CreateBoardRequest.class));
    }

    @Test
    void testCreateBoard_DuplicateName() throws Exception {
        // Given
        CreateBoardRequest request = new CreateBoardRequest();
        request.setName("Existing Board");
        request.setActive(true);

        when(contentManagementService.createBoard(any(CreateBoardRequest.class)))
                .thenThrow(new IllegalArgumentException("Board with name 'Existing Board' already exists"));

        // When & Then
        mockMvc.perform(post("/api/admin/content/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).createBoard(any(CreateBoardRequest.class));
        verify(loggingService).logError(eq("CREATE_BOARD"), any(Exception.class), any());
    }

    @Test
    void testUpdateBoard_Success() throws Exception {
        // Given
        UpdateBoardRequest request = new UpdateBoardRequest();
        request.setName("Updated Board");
        request.setActive(false);

        BoardDto updatedBoard = BoardDto.builder()
                .id(1L)
                .name("Updated Board")
                .active(false)
                .updatedAt(LocalDateTime.now())
                .build();

        when(contentManagementService.updateBoard(eq(1L), any(UpdateBoardRequest.class)))
                .thenReturn(updatedBoard);

        // When & Then
        mockMvc.perform(put("/api/admin/content/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Board updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Updated Board"))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(contentManagementService).updateBoard(eq(1L), any(UpdateBoardRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("UPDATE_BOARD"), eq("1"), any());
        verify(loggingService).logBusinessOperationComplete(eq("UPDATE_BOARD"), eq("1"), eq(true), anyLong());
    }

    @Test
    void testUpdateBoard_NotFound() throws Exception {
        // Given
        UpdateBoardRequest request = new UpdateBoardRequest();
        request.setName("Updated Board");

        when(contentManagementService.updateBoard(eq(999L), any(UpdateBoardRequest.class)))
                .thenThrow(new ResourceNotFoundException("Board not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/admin/content/boards/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(contentManagementService).updateBoard(eq(999L), any(UpdateBoardRequest.class));
        verify(loggingService).logError(eq("UPDATE_BOARD"), any(Exception.class), any());
    }

    @Test
    void testDeleteBoard_Success() throws Exception {
        // Given
        doNothing().when(contentManagementService).deleteBoard(eq(1L), eq(false));

        // When & Then
        mockMvc.perform(delete("/api/admin/content/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Board deleted successfully"));

        verify(contentManagementService).deleteBoard(eq(1L), eq(false));
        verify(loggingService).logBusinessOperationStart(eq("DELETE_BOARD"), eq("1"), any());
        verify(loggingService).logBusinessOperationComplete(eq("DELETE_BOARD"), eq("1"), eq(true), anyLong());
    }

    @Test
    void testDeleteBoard_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Board not found with id: 999"))
                .when(contentManagementService).deleteBoard(eq(999L), eq(false));

        // When & Then
        mockMvc.perform(delete("/api/admin/content/boards/999"))
                .andExpect(status().isNotFound());

        verify(contentManagementService).deleteBoard(eq(999L), eq(false));
        verify(loggingService).logError(eq("DELETE_BOARD"), any(Exception.class), any());
    }

    // ============ GRADE TESTS ============

    @Test
    void testGetGrades_Success() throws Exception {
        // Given
        GradeDto grade1 = GradeDto.builder()
                .id(1L)
                .name("Grade 10")
                .displayName("Class 10")
                .boardId(1L)
                .boardName("CBSE")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<GradeDto> grades = Arrays.asList(grade1);
        Page<GradeDto> gradePage = new PageImpl<>(grades, PageRequest.of(0, 10), 1);

        when(contentManagementService.getGrades(any(), any(), any(), any(Pageable.class)))
                .thenReturn(gradePage);

        // When & Then
        mockMvc.perform(get("/api/admin/content/grades")
                        .param("page", "0")
                        .param("size", "10")
                        .param("boardId", "1")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grades retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].name").value("Grade 10"));

        verify(contentManagementService).getGrades(isNull(), eq(1L), eq(true), any(Pageable.class));
    }

    @Test
    void testCreateGrade_Success() throws Exception {
        // Given
        CreateGradeRequest request = new CreateGradeRequest();
        request.setName("Grade 11");
        request.setDisplayName("Class 11");
        request.setBoardId(1L);
        request.setActive(true);

        GradeDto createdGrade = GradeDto.builder()
                .id(2L)
                .name("Grade 11")
                .displayName("Class 11")
                .boardId(1L)
                .boardName("CBSE")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(contentManagementService.createGrade(any(CreateGradeRequest.class)))
                .thenReturn(createdGrade);

        // When & Then
        mockMvc.perform(post("/api/admin/content/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grade created successfully"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("Grade 11"));

        verify(contentManagementService).createGrade(any(CreateGradeRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("CREATE_GRADE"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("CREATE_GRADE"), eq("2"), eq(true), anyLong());
    }

    @Test
    void testCreateGrade_ValidationError() throws Exception {
        // Given
        CreateGradeRequest request = new CreateGradeRequest();
        request.setName(""); // Invalid: empty name
        request.setDisplayName("Class 11");
        request.setBoardId(1L);

        // When & Then
        mockMvc.perform(post("/api/admin/content/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contentManagementService, never()).createGrade(any(CreateGradeRequest.class));
    }

    @Test
    void testDeleteGrade_Success() throws Exception {
        // Given
        doNothing().when(contentManagementService).deleteGrade(eq(1L), eq(false));

        // When & Then
        mockMvc.perform(delete("/api/admin/content/grades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grade deleted successfully"));

        verify(contentManagementService).deleteGrade(eq(1L), eq(false));
        verify(loggingService).logBusinessOperationStart(eq("DELETE_GRADE"), eq("1"), any());
        verify(loggingService).logBusinessOperationComplete(eq("DELETE_GRADE"), eq("1"), eq(true), anyLong());
    }

    // ============ DROPDOWN TESTS ============

    @Test
    void testGetBoardsDropdown_Success() throws Exception {
        // Given
        BoardDropdownDto board1 = new BoardDropdownDto(1L, "CBSE");
        BoardDropdownDto board2 = new BoardDropdownDto(2L, "ICSE");
        List<BoardDropdownDto> boards = Arrays.asList(board1, board2);

        when(contentManagementService.getBoardsForDropdown()).thenReturn(boards);

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Boards dropdown retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("CBSE"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("ICSE"));

        verify(contentManagementService).getBoardsForDropdown();
        verify(loggingService).logBusinessOperationStart(eq("GET_BOARDS_DROPDOWN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_BOARDS_DROPDOWN"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetBoardsDropdown_EmptyResult() throws Exception {
        // Given
        when(contentManagementService.getBoardsForDropdown()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(contentManagementService).getBoardsForDropdown();
    }

    @Test
    void testGetGradesDropdown_Success() throws Exception {
        // Given
        GradeDropdownDto grade1 = new GradeDropdownDto(1L, "Grade 10", "Class 10", 1L);
        GradeDropdownDto grade2 = new GradeDropdownDto(2L, "Grade 11", "Class 11", 1L);
        List<GradeDropdownDto> grades = Arrays.asList(grade1, grade2);

        when(contentManagementService.getGradesForDropdown(1L)).thenReturn(grades);

        // When & Then
        mockMvc.perform(get("/api/admin/content/grades/dropdown")
                        .param("boardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grades dropdown retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Grade 10"));

        verify(contentManagementService).getGradesForDropdown(1L);
        verify(loggingService).logBusinessOperationStart(eq("GET_GRADES_DROPDOWN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_GRADES_DROPDOWN"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetGradesDropdown_NoBoardId() throws Exception {
        // Given
        GradeDropdownDto grade1 = new GradeDropdownDto(1L, "Grade 10", "Class 10", 1L);
        List<GradeDropdownDto> grades = Arrays.asList(grade1);

        when(contentManagementService.getGradesForDropdown(null)).thenReturn(grades);

        // When & Then
        mockMvc.perform(get("/api/admin/content/grades/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(contentManagementService).getGradesForDropdown(null);
    }

    @Test
    void testGetSubjectsDropdown_Success() throws Exception {
        // Given
        SubjectDropdownDto subject1 = new SubjectDropdownDto(1L, "Mathematics", 1L, 1L);
        SubjectDropdownDto subject2 = new SubjectDropdownDto(2L, "Science", 1L, 1L);
        List<SubjectDropdownDto> subjects = Arrays.asList(subject1, subject2);

        when(contentManagementService.getSubjectsForDropdown(1L)).thenReturn(subjects);

        // When & Then
        mockMvc.perform(get("/api/admin/content/subjects/dropdown")
                        .param("gradeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Subjects dropdown retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Mathematics"));

        verify(contentManagementService).getSubjectsForDropdown(1L);
        verify(loggingService).logBusinessOperationStart(eq("GET_SUBJECTS_DROPDOWN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_SUBJECTS_DROPDOWN"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetChaptersDropdown_Success() throws Exception {
        // Given
        ChapterDropdownDto chapter1 = new ChapterDropdownDto(1L, "Algebra", 1L, 1L, 1L);
        ChapterDropdownDto chapter2 = new ChapterDropdownDto(2L, "Geometry", 1L, 1L, 1L);
        List<ChapterDropdownDto> chapters = Arrays.asList(chapter1, chapter2);

        when(contentManagementService.getChaptersForDropdown(1L)).thenReturn(chapters);

        // When & Then
        mockMvc.perform(get("/api/admin/content/chapters/dropdown")
                        .param("subjectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chapters dropdown retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Algebra"));

        verify(contentManagementService).getChaptersForDropdown(1L);
        verify(loggingService).logBusinessOperationStart(eq("GET_CHAPTERS_DROPDOWN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_CHAPTERS_DROPDOWN"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetTopicsDropdown_Success() throws Exception {
        // Given
        TopicDropdownDto topic1 = new TopicDropdownDto(1L, "Linear Equations", "LE001", 1L, 1L, 1L, 1L);
        TopicDropdownDto topic2 = new TopicDropdownDto(2L, "Quadratic Equations", "QE001", 1L, 1L, 1L, 1L);
        List<TopicDropdownDto> topics = Arrays.asList(topic1, topic2);

        when(contentManagementService.getTopicsForDropdown(1L)).thenReturn(topics);

        // When & Then
        mockMvc.perform(get("/api/admin/content/topics/dropdown")
                        .param("chapterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Topics dropdown retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Linear Equations"));

        verify(contentManagementService).getTopicsForDropdown(1L);
        verify(loggingService).logBusinessOperationStart(eq("GET_TOPICS_DROPDOWN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("GET_TOPICS_DROPDOWN"), isNull(), eq(true), anyLong());
    }

    @Test
    void testGetBoardsDropdown_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getBoardsForDropdown())
                .thenThrow(new BusinessException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/boards/dropdown"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getBoardsForDropdown();
        verify(loggingService).logError(eq("GET_BOARDS_DROPDOWN"), any(Exception.class), any());
    }

    @Test
    void testGetGradesDropdown_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getGradesForDropdown(anyLong()))
                .thenThrow(new BusinessException("Service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/grades/dropdown")
                        .param("boardId", "1"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getGradesForDropdown(1L);
        verify(loggingService).logError(eq("GET_GRADES_DROPDOWN"), any(Exception.class), any());
    }

    @Test
    void testGetSubjectsDropdown_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getSubjectsForDropdown(anyLong()))
                .thenThrow(new BusinessException("Repository error"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/subjects/dropdown")
                        .param("gradeId", "1"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getSubjectsForDropdown(1L);
        verify(loggingService).logError(eq("GET_SUBJECTS_DROPDOWN"), any(Exception.class), any());
    }

    @Test
    void testGetChaptersDropdown_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getChaptersForDropdown(anyLong()))
                .thenThrow(new BusinessException("Data access error"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/chapters/dropdown")
                        .param("subjectId", "1"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getChaptersForDropdown(1L);
        verify(loggingService).logError(eq("GET_CHAPTERS_DROPDOWN"), any(Exception.class), any());
    }

    @Test
    void testGetTopicsDropdown_ServiceException() throws Exception {
        // Given
        when(contentManagementService.getTopicsForDropdown(anyLong()))
                .thenThrow(new BusinessException("Query execution failed"));

        // When & Then
        mockMvc.perform(get("/api/admin/content/topics/dropdown")
                        .param("chapterId", "1"))
                .andExpect(status().isBadRequest());

        verify(contentManagementService).getTopicsForDropdown(1L);
        verify(loggingService).logError(eq("GET_TOPICS_DROPDOWN"), any(Exception.class), any());
    }
}
