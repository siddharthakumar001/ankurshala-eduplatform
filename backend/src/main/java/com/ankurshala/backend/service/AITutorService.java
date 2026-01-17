package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.entity.AIInteraction;
import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.repository.AIInteractionRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * AI Tutor Service for personalized student interactions.
 * Uses RAG (Retrieval-Augmented Generation) to ground responses in curriculum content.
 */
@Service
@Slf4j
public class AITutorService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private ContentChunkService contentChunkService;

    // Reserved for future direct embedding operations (e.g., query expansion)
    @SuppressWarnings("unused")
    @Autowired(required = false)
    private EmbeddingService embeddingService;

    @Autowired
    private SafetyModerationService safetyService;

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @Autowired
    private AIInteractionRepository aiInteractionRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired(required = false)
    private DevAIProvider devAIProvider;

    private static final int MAX_CONTEXT_CHUNKS = 5;
    // Threshold for minimum acceptable similarity in semantic search (for future use)
    @SuppressWarnings("unused")
    private static final double MIN_SIMILARITY_THRESHOLD = 0.7;

    // Base system prompt for the AI Tutor (will be personalized with student profile)
    private static final String BASE_SYSTEM_PROMPT = """
        You are Ankur, a friendly and patient AI tutor for Indian school students.
        
        Your role:
        - Help students understand concepts from their curriculum
        - Explain in simple terms appropriate for the student's grade level
        - Use examples relevant to Indian context when possible
        - Encourage learning and curiosity
        - Support multiple Indian languages (Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi, and English)
        - Respond in the language the student prefers
        
        Important guidelines:
        1. ONLY answer questions related to academics and school subjects
        2. Base your answers ONLY on the provided context when available
        3. If you're not sure about something, admit it and suggest asking a teacher
        4. Never provide medical, legal, or financial advice
        5. Keep responses concise but thorough - students have limited attention spans
        6. Include step-by-step explanations for math and science problems
        7. Always cite which chapter or topic the information comes from when possible
        8. Be encouraging - use phrases like "Great question!" and "You're on the right track!"
        
        If the question is outside your knowledge or the provided context, respond with:
        "I don't have specific information about this in my curriculum materials. 
        I'd recommend asking your teacher for the most accurate answer. 
        Is there something else about [subject] I can help you with?"
        """;

    /**
     * Process a chat request with RAG
     */
    @Transactional
    public ChatDTO.ChatResponse chat(ChatDTO.ChatRequest request, Long studentId) {
        String traceId = TraceUtil.getTraceId();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        log.info("Processing chat request - TraceId: {}, StudentId: {}, SessionId: {}, DevMode: {}", 
                traceId, studentId, sessionId, aiProperties.isDevMode());

        // DEV MODE: Use deterministic DevAIProvider
        if (aiProperties.isDevMode() && devAIProvider != null) {
            log.debug("Using DevAIProvider for deterministic testing");
            ChatDTO.ChatResponse response = devAIProvider.generateChatResponse(request);
            long latencyMs = System.currentTimeMillis() - startTime;
            logInteraction(studentId, sessionId, request, response, null, latencyMs, null);
            return response;
        }

        // 1. Safety check on user input
        ChatDTO.SafetyCheckResult inputSafetyCheck = safetyService.checkUserInput(request.getMessage());
        if (!inputSafetyCheck.getIsSafe()) {
            log.warn("Unsafe input detected - TraceId: {}, Category: {}", traceId, inputSafetyCheck.getCategory());
            return buildSafetyResponse(sessionId, inputSafetyCheck, startTime);
        }

        // 2. Check if AI is available
        if (!aiProperties.isAvailable() || chatModel == null) {
            log.warn("AI service not available - TraceId: {}", traceId);
            return buildUnavailableResponse(sessionId, startTime);
        }

        // 3. Get student profile for personalization
        StudentProfile studentProfile = studentProfileRepository.findByUserId(studentId).orElse(null);

        // 4. Retrieve relevant context using RAG (with student profile context)
        ContentChunkService.RetrievalResult retrievalResult = retrieveContext(
                request.getMessage(),
                request.getTopicId(),
                request.getSubjectId(),
                studentProfile != null && studentProfile.getLanguage() != null 
                    ? studentProfile.getLanguage() 
                    : request.getLanguage(),
                studentProfile
        );

        // 5. Build the prompt with context and personalization
        List<Message> messages = buildMessages(request, retrievalResult, studentProfile);

        try {
            // 5. Call the AI model
            Prompt prompt = new Prompt(messages);
            ChatResponse aiResponse = chatModel.call(prompt);

            String responseContent = aiResponse.getResult().getOutput().getContent();

            // 6. Safety check on AI response
            ChatDTO.SafetyCheckResult outputSafetyCheck = safetyService.checkAIResponse(responseContent);
            if (!outputSafetyCheck.getIsSafe()) {
                log.warn("Unsafe AI response detected - TraceId: {}", traceId);
                responseContent = outputSafetyCheck.getSuggestedResponse();
            }

            // 7. Build response with citations
            long latency = System.currentTimeMillis() - startTime;
            ChatDTO.ChatResponse response = buildSuccessResponse(
                    sessionId,
                    responseContent,
                    retrievalResult,
                    outputSafetyCheck,
                    latency,
                    aiResponse.getMetadata() != null ? 
                            (Integer) aiResponse.getMetadata().getOrDefault("total_tokens", 0) : 0
            );

            // 8. Log the interaction
            logInteraction(studentId, sessionId, request, response, retrievalResult, latency, null);

            return response;

        } catch (Exception e) {
            log.error("AI chat failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            long latency = System.currentTimeMillis() - startTime;
            logInteraction(studentId, sessionId, request, null, retrievalResult, latency, e.getMessage());
            return buildErrorResponse(sessionId, latency);
        }
    }

    /**
     * Stream chat response for real-time feel
     */
    public Flux<ChatDTO.StreamChunk> streamChat(ChatDTO.ChatRequest request, Long studentId) {
        String traceId = TraceUtil.getTraceId();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        log.info("Starting streaming chat - TraceId: {}, StudentId: {}", traceId, studentId);

        // Safety check
        ChatDTO.SafetyCheckResult inputSafetyCheck = safetyService.checkUserInput(request.getMessage());
        if (!inputSafetyCheck.getIsSafe()) {
            return Flux.just(ChatDTO.StreamChunk.builder()
                    .sessionId(sessionId)
                    .content(inputSafetyCheck.getSuggestedResponse())
                    .isComplete(true)
                    .chunkIndex(0)
                    .build());
        }

        if (!aiProperties.isAvailable() || chatModel == null) {
            return Flux.just(ChatDTO.StreamChunk.builder()
                    .sessionId(sessionId)
                    .content("I'm temporarily unavailable. Please try again later!")
                    .isComplete(true)
                    .chunkIndex(0)
                    .build());
        }

        // Get student profile for personalization
        StudentProfile studentProfile = studentProfileRepository.findByUserId(studentId).orElse(null);

        // Retrieve context (with student profile context)
        ContentChunkService.RetrievalResult retrievalResult = retrieveContext(
                request.getMessage(),
                request.getTopicId(),
                request.getSubjectId(),
                studentProfile != null && studentProfile.getLanguage() != null 
                    ? studentProfile.getLanguage() 
                    : request.getLanguage(),
                studentProfile
        );

        List<Message> messages = buildMessages(request, retrievalResult, studentProfile);
        Prompt prompt = new Prompt(messages);

        try {
            // Fallback to non-streaming response wrapped in Flux
            // Note: Full streaming support requires StreamingChatModel which depends on Spring AI version
            ChatResponse aiResponse = chatModel.call(prompt);
            String responseContent = aiResponse.getResult().getOutput().getContent();
            
            // Simulate streaming by splitting response into chunks
            List<String> chunks = splitIntoChunks(responseContent, 50);
            final int totalChunks = chunks.size();
            
            return Flux.fromIterable(chunks)
                    .index()
                    .map(tuple -> ChatDTO.StreamChunk.builder()
                            .sessionId(sessionId)
                            .content(tuple.getT2())
                            .isComplete(tuple.getT1() == totalChunks - 1)
                            .chunkIndex(tuple.getT1().intValue())
                            .build())
                    .delayElements(java.time.Duration.ofMillis(20)); // Small delay for streaming effect
        } catch (Exception e) {
            log.error("Streaming chat failed - TraceId: {}", traceId, e);
            return Flux.just(ChatDTO.StreamChunk.builder()
                    .sessionId(sessionId)
                    .content("Sorry, I encountered an error. Please try again!")
                    .isComplete(true)
                    .chunkIndex(0)
                    .build());
        }
    }

    /**
     * Retrieve relevant context chunks for RAG with student profile personalization
     */
    private ContentChunkService.RetrievalResult retrieveContext(
            String query, Long topicId, Long subjectId, String language, StudentProfile studentProfile) {
        
        String traceId = TraceUtil.getTraceId();
        log.debug("Retrieving RAG context - TraceId: {}, TopicId: {}, StudentProfile: {}", 
                traceId, topicId, studentProfile != null ? "present" : "null");

        // Use student's preferred language if available
        String effectiveLanguage = language;
        if (studentProfile != null && studentProfile.getLanguage() != null && !studentProfile.getLanguage().isEmpty()) {
            effectiveLanguage = studentProfile.getLanguage();
        }

        // Extract grade and board from student profile for personalized content retrieval
        Long gradeId = null;
        Long boardId = null;
        if (studentProfile != null) {
            // Try to get grade ID from profile
            if (studentProfile.getGradeId() != null) {
                gradeId = studentProfile.getGradeId();
            }
            // Try to get board ID from profile
            if (studentProfile.getBoardId() != null) {
                boardId = studentProfile.getBoardId();
            }
        }

        // Use semantic search with student profile context for better personalization
        return contentChunkService.retrieveForRAGWithProfile(
                query,
                topicId,
                subjectId,
                gradeId,
                boardId,
                effectiveLanguage,
                MAX_CONTEXT_CHUNKS
        );
    }

    /**
     * Build the message list for the AI model with student profile personalization
     */
    private List<Message> buildMessages(ChatDTO.ChatRequest request, 
                                        ContentChunkService.RetrievalResult retrievalResult,
                                        StudentProfile studentProfile) {
        List<Message> messages = new ArrayList<>();

        // Build personalized system prompt
        String personalizedPrompt = buildPersonalizedSystemPrompt(studentProfile);
        messages.add(new SystemMessage(personalizedPrompt));

        // Add context from RAG if available
        if (retrievalResult.getRetrievalCount() > 0) {
            String contextMessage = buildContextMessage(retrievalResult);
            messages.add(new SystemMessage(contextMessage));
        }

        // Add conversation history if provided
        if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
            log.debug("Adding conversation history - {} messages", request.getConversationHistory().size());
            for (ChatDTO.ChatMessage histMsg : request.getConversationHistory()) {
                if ("USER".equals(histMsg.getRole())) {
                    messages.add(new UserMessage(histMsg.getContent()));
                } else if ("ASSISTANT".equals(histMsg.getRole())) {
                    messages.add(new AssistantMessage(histMsg.getContent()));
                }
            }
        }

        // Add current user message
        messages.add(new UserMessage(request.getMessage()));

        return messages;
    }

    /**
     * Build personalized system prompt based on student profile
     */
    private String buildPersonalizedSystemPrompt(StudentProfile studentProfile) {
        StringBuilder prompt = new StringBuilder(BASE_SYSTEM_PROMPT);
        
        if (studentProfile != null) {
            prompt.append("\n\nSTUDENT CONTEXT:\n");
            
            // Add grade/class level
            if (studentProfile.getClassLevel() != null) {
                String gradeLevel = studentProfile.getClassLevel().name().replace("GRADE_", "Grade ");
                prompt.append("- Student is in ").append(gradeLevel).append("\n");
            } else if (studentProfile.getGradeLevel() != null) {
                prompt.append("- Student is in ").append(studentProfile.getGradeLevel()).append("\n");
            }
            
            // Add educational board
            if (studentProfile.getEducationalBoard() != null) {
                prompt.append("- Student follows ").append(studentProfile.getEducationalBoard().name()).append(" curriculum\n");
            }
            
            // Add preferred language
            if (studentProfile.getLanguage() != null && !studentProfile.getLanguage().isEmpty()) {
                prompt.append("- Student prefers ").append(studentProfile.getLanguage()).append(" language\n");
            }
            
            // Add learning goals if available
            if (studentProfile.getGoals() != null && !studentProfile.getGoals().isEmpty()) {
                prompt.append("- Student's learning goals: ").append(studentProfile.getGoals()).append("\n");
            }
            
            prompt.append("\nUse this context to personalize your explanations. ");
            prompt.append("Adjust the complexity and examples based on the student's grade level and curriculum.\n");
        }
        
        return prompt.toString();
    }

    /**
     * Build context message from retrieved chunks
     */
    private String buildContextMessage(ContentChunkService.RetrievalResult retrievalResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("Use the following curriculum content to answer the student's question. ");
        sb.append("Always cite the source when using this information.\n\n");
        sb.append("CURRICULUM CONTEXT:\n");
        sb.append(safetyService.sanitizeRetrievedContent(retrievalResult.getContextString()));
        sb.append("\n--- END OF CONTEXT ---\n");
        return sb.toString();
    }

    /**
     * Build citations from retrieval results
     */
    private List<ChatDTO.ContentReference> buildCitations(ContentChunkService.RetrievalResult retrievalResult) {
        List<ChatDTO.ContentReference> citations = new ArrayList<>();

        for (ContentChunkService.ChunkWithScore chunkWithScore : retrievalResult.getChunks()) {
            ContentChunk chunk = chunkWithScore.getChunk();
            citations.add(ChatDTO.ContentReference.builder()
                    .topicTitle(chunk.getTopic() != null ? chunk.getTopic().getTitle() : null)
                    .chapterName(chunk.getChapter() != null ? chunk.getChapter().getName() : null)
                    .subjectName(chunk.getSubject() != null ? chunk.getSubject().getName() : null)
                    .sourceType(chunk.getSourceType() != null ? chunk.getSourceType().name() : null)
                    .sourceRef(chunk.getSourceRef())
                    .relevanceScore(chunkWithScore.getScore())
                    .build());
        }

        return citations;
    }

    /**
     * Build success response
     */
    private ChatDTO.ChatResponse buildSuccessResponse(
            String sessionId,
            String responseContent,
            ContentChunkService.RetrievalResult retrievalResult,
            ChatDTO.SafetyCheckResult outputSafetyCheck,
            long latency,
            int tokensUsed) {

        return ChatDTO.ChatResponse.builder()
                .sessionId(sessionId)
                .message(responseContent)
                .wasGrounded(retrievalResult.getRetrievalCount() > 0)
                .references(buildCitations(retrievalResult))
                .suggestedActions(buildSuggestedActions(retrievalResult))
                .safetyFiltered(!outputSafetyCheck.getIsSafe())
                .filterReason(outputSafetyCheck.getIsSafe() ? null : outputSafetyCheck.getReason())
                .tokensUsed(tokensUsed)
                .latencyMs((int) latency)
                .build();
    }

    /**
     * Build safety response when input is flagged
     */
    private ChatDTO.ChatResponse buildSafetyResponse(String sessionId, 
                                                      ChatDTO.SafetyCheckResult safetyCheck, 
                                                      long startTime) {
        return ChatDTO.ChatResponse.builder()
                .sessionId(sessionId)
                .message(safetyCheck.getSuggestedResponse())
                .wasGrounded(false)
                .references(Collections.emptyList())
                .suggestedActions(Collections.emptyList())
                .safetyFiltered(true)
                .filterReason(safetyCheck.getReason())
                .tokensUsed(0)
                .latencyMs((int) (System.currentTimeMillis() - startTime))
                .build();
    }

    /**
     * Build response when AI is unavailable
     */
    private ChatDTO.ChatResponse buildUnavailableResponse(String sessionId, long startTime) {
        return ChatDTO.ChatResponse.builder()
                .sessionId(sessionId)
                .message("I'm temporarily unavailable. Please try again later, or ask your teacher for help with this question!")
                .wasGrounded(false)
                .references(Collections.emptyList())
                .suggestedActions(Collections.emptyList())
                .safetyFiltered(false)
                .tokensUsed(0)
                .latencyMs((int) (System.currentTimeMillis() - startTime))
                .build();
    }

    /**
     * Build error response
     */
    private ChatDTO.ChatResponse buildErrorResponse(String sessionId, long latency) {
        return ChatDTO.ChatResponse.builder()
                .sessionId(sessionId)
                .message("I encountered an error processing your question. Please try again, or ask your teacher if this keeps happening!")
                .wasGrounded(false)
                .references(Collections.emptyList())
                .suggestedActions(Collections.emptyList())
                .safetyFiltered(false)
                .tokensUsed(0)
                .latencyMs((int) latency)
                .build();
    }

    /**
     * Build suggested follow-up actions
     */
    private List<ChatDTO.SuggestedAction> buildSuggestedActions(ContentChunkService.RetrievalResult retrievalResult) {
        List<ChatDTO.SuggestedAction> actions = new ArrayList<>();

        if (retrievalResult.getRetrievalCount() > 0) {
            ContentChunk firstChunk = retrievalResult.getChunks().get(0).getChunk();

            if (firstChunk.getTopicId() != null) {
                actions.add(ChatDTO.SuggestedAction.builder()
                        .actionType("TAKE_QUIZ")
                        .actionLabel("Practice with a quiz on this topic")
                        .actionData(Map.of("topicId", firstChunk.getTopicId()))
                        .build());

                actions.add(ChatDTO.SuggestedAction.builder()
                        .actionType("VIEW_TOPIC")
                        .actionLabel("View full topic details")
                        .actionData(Map.of("topicId", firstChunk.getTopicId()))
                        .build());
            }
        }

        actions.add(ChatDTO.SuggestedAction.builder()
                .actionType("ASK_TEACHER")
                .actionLabel("Ask your teacher for more help")
                .actionData(Map.of())
                .build());

        return actions;
    }

    /**
     * Log AI interaction for analytics and debugging
     */
    private void logInteraction(Long studentId, String sessionId, 
                                ChatDTO.ChatRequest request, ChatDTO.ChatResponse response,
                                ContentChunkService.RetrievalResult retrievalResult,
                                long latencyMs, String errorMessage) {
        try {
            AIInteraction interaction = new AIInteraction(AIInteraction.InteractionType.CHAT, studentId)
                    .withSessionId(sessionId)
                    .withTopic(request.getTopicId())
                    .withSubject(request.getSubjectId())
                    .withModel(aiProperties.getProvider(), aiProperties.getModel(), null)
                    .withLatency((int) latencyMs);

            if (response != null) {
                interaction.withTokens(0, response.getTokensUsed() != null ? response.getTokensUsed() : 0);
                if (Boolean.TRUE.equals(response.getSafetyFiltered())) {
                    interaction.markRefused(response.getFilterReason());
                }
            }

            if (retrievalResult != null && retrievalResult.getRetrievalCount() > 0) {
                interaction.withRetrieval(
                        retrievalResult.getRetrievalCount(),
                        retrievalResult.getAverageScore()
                );
            }

            if (errorMessage != null) {
                interaction.markError("AI_ERROR", errorMessage);
            }

            aiInteractionRepository.save(interaction);
        } catch (Exception e) {
            log.error("Failed to log AI interaction", e);
        }
    }

    /**
     * Split text into chunks for simulated streaming
     */
    private List<String> splitIntoChunks(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return Collections.singletonList("");
        }
        
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            // Try to break at word boundary
            if (end < text.length() && text.charAt(end) != ' ') {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace + 1;
                }
            }
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }
}

