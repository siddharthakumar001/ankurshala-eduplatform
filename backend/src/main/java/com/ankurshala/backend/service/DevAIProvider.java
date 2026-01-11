package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.repository.ContentChunkRepository;
import com.ankurshala.backend.repository.TopicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DEV AI Provider - Returns deterministic, grounded mock responses for local E2E testing.
 * 
 * This provider:
 * 1. Uses real retrieval if content chunks exist for the topic
 * 2. Returns safe, educational fallback responses if no content available
 * 3. Always includes suggested actions for testing UI integration
 * 4. Provides deterministic responses for predictable E2E tests
 * 
 * Enabled when: app.ai.dev-mode=true (default in local environment)
 */
@Service
@Slf4j
public class DevAIProvider {

    @Autowired
    private ContentChunkRepository contentChunkRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired(required = false)
    private EmbeddingService embeddingService;

    /**
     * Generate a chat response in DEV mode
     */
    public ChatDTO.ChatResponse generateChatResponse(ChatDTO.ChatRequest request) {
        log.debug("[DEV AI] Generating response for: {}", request.getMessage());

        ChatDTO.ChatResponse response = new ChatDTO.ChatResponse();
        response.setSessionId(request.getSessionId() != null ? request.getSessionId() : generateSessionId());

        // Try to ground response using RAG if topic is specified
        boolean wasGrounded = false;
        String aiResponse;
        List<ChatDTO.ContentReference> references = new ArrayList<>();

        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId()).orElse(null);
            if (topic != null) {
                // Try to retrieve content chunks
                List<ContentChunk> chunks = contentChunkRepository.findByTopicIdAndStatus(
                    request.getTopicId(),
                    ContentChunk.ChunkStatus.ACTIVE
                );

                if (!chunks.isEmpty()) {
                    // Grounded response using actual content
                    aiResponse = generateGroundedResponse(request.getMessage(), chunks, topic);
                    references = buildReferences(chunks, topic);
                    wasGrounded = true;
                    log.debug("[DEV AI] Generated grounded response using {} chunks", chunks.size());
                } else {
                    // Fallback to generic topic-aware response
                    aiResponse = generateTopicFallbackResponse(request.getMessage(), topic);
                    log.debug("[DEV AI] No content chunks found, using topic fallback");
                }
            } else {
                aiResponse = generateGenericFallbackResponse(request.getMessage());
            }
        } else {
            aiResponse = generateGenericFallbackResponse(request.getMessage());
        }

        response.setMessage(aiResponse);
        response.setWasGrounded(wasGrounded);
        response.setReferences(references);
        response.setSuggestedActions(generateSuggestedActions(request.getTopicId()));
        response.setSafetyFiltered(false);
        response.setTokensUsed(Integer.valueOf(aiResponse.split("\\s+").length * 2)); // Mock token count
        response.setLatencyMs(150); // Mock latency

        return response;
    }

    /**
     * Generate a grounded response using retrieved content chunks
     */
    private String generateGroundedResponse(String question, List<ContentChunk> chunks, Topic topic) {
        // Use first 2-3 chunks for context
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < Math.min(3, chunks.size()); i++) {
            context.append(chunks.get(i).getChunkText()).append("\n\n");
        }

        // Generate response based on question type
        String lowercaseQuestion = question.toLowerCase();
        
        if (lowercaseQuestion.contains("what is") || lowercaseQuestion.contains("define")) {
            return String.format("Based on the curriculum content for %s:\n\n%s\n\nThis concept is fundamental to understanding %s. " +
                "Would you like me to explain any specific aspect in more detail?",
                topic.getTitle(),
                chunks.get(0).getChunkText(),
                topic.getTitle());
        } else if (lowercaseQuestion.contains("how") || lowercaseQuestion.contains("explain")) {
            return String.format("Let me explain %s step by step:\n\n%s\n\nKey points to remember:\n" +
                "• This builds upon previous concepts\n" +
                "• Practice is essential for mastery\n" +
                "• Real-world applications make this more intuitive\n\n" +
                "Would you like me to provide some practice problems?",
                topic.getTitle(),
                chunks.get(0).getChunkText());
        } else if (lowercaseQuestion.contains("example") || lowercaseQuestion.contains("demonstrate")) {
            return String.format("Here's an example related to %s:\n\n%s\n\n" +
                "This example demonstrates the core principle. Let me know if you'd like more examples or want to try solving similar problems!",
                topic.getTitle(),
                chunks.get(0).getChunkText());
        } else {
            // Generic grounded response
            return String.format("Regarding your question about %s:\n\n%s\n\n" +
                "I hope this helps! Feel free to ask if you need clarification on any part.",
                topic.getTitle(),
                chunks.get(0).getChunkText());
        }
    }

    /**
     * Generate a fallback response when no content chunks are available but topic is known
     */
    private String generateTopicFallbackResponse(String question, Topic topic) {
        return String.format("I'd be happy to help you with %s!\n\n" +
            "While I don't have detailed curriculum content loaded for this topic yet, " +
            "I can still guide you through the key concepts. %s is an important topic that builds upon " +
            "your previous knowledge.\n\n" +
            "To get the most accurate and detailed information, I recommend:\n" +
            "• Reviewing your textbook chapter on this topic\n" +
            "• Trying the practice problems available\n" +
            "• Booking a session with a teacher for personalized guidance\n\n" +
            "Is there a specific aspect of %s you'd like to explore?",
            topic.getTitle(),
            topic.getTitle(),
            topic.getTitle());
    }

    /**
     * Generate a generic fallback response when no topic context is available
     */
    private String generateGenericFallbackResponse(String question) {
        if (question.toLowerCase().contains("hello") || question.toLowerCase().contains("hi")) {
            return "Hello! I'm your AI tutor. I'm here to help you learn and understand various topics. " +
                "To get started, you can:\n" +
                "• Ask me questions about specific topics you're studying\n" +
                "• Request explanations or examples\n" +
                "• Get help with practice problems\n\n" +
                "What would you like to learn about today?";
        }

        return "I understand your question! To provide you with the most accurate and helpful answer, " +
            "it would be great if you could specify which topic you're studying. " +
            "You can select a topic from your curriculum, and I'll be able to give you detailed, " +
            "curriculum-aligned explanations.\n\n" +
            "In the meantime, feel free to browse available topics or book a session with a teacher " +
            "for personalized guidance!";
    }

    /**
     * Build reference citations from content chunks
     */
    private List<ChatDTO.ContentReference> buildReferences(List<ContentChunk> chunks, Topic topic) {
        List<ChatDTO.ContentReference> references = new ArrayList<>();
        
        for (int i = 0; i < Math.min(3, chunks.size()); i++) {
            ContentChunk chunk = chunks.get(i);
            references.add(ChatDTO.ContentReference.builder()
                .topicTitle(chunk.getTopic().getTitle())
                .chapterName(chunk.getTopic().getChapter().getName())
                .subjectName(chunk.getTopic().getChapter().getSubject().getName())
                .sourceType("TEXTBOOK")
                .sourceRef("Chapter " + (i + 1))
                .relevanceScore(0.85 - (i * 0.1))
                .build());
        }
        
        return references;
    }

    /**
     * Generate suggested actions for the UI
     */
    private List<ChatDTO.SuggestedAction> generateSuggestedActions(Long topicId) {
        List<ChatDTO.SuggestedAction> actions = new ArrayList<>();

        if (topicId != null) {
            // Generate Notes action
            ChatDTO.SuggestedAction generateNotes = new ChatDTO.SuggestedAction();
            generateNotes.setActionType("GENERATE_NOTES");
            generateNotes.setActionLabel("Generate notes for this topic");
            Map<String, Object> notesData = new HashMap<>();
            notesData.put("topicId", topicId);
            generateNotes.setActionData(notesData);
            actions.add(generateNotes);

            // Start Practice action
            ChatDTO.SuggestedAction startPractice = new ChatDTO.SuggestedAction();
            startPractice.setActionType("START_PRACTICE");
            startPractice.setActionLabel("Practice with quiz questions");
            Map<String, Object> practiceData = new HashMap<>();
            practiceData.put("topicId", topicId);
            startPractice.setActionData(practiceData);
            actions.add(startPractice);

            // Start Focus action
            ChatDTO.SuggestedAction startFocus = new ChatDTO.SuggestedAction();
            startFocus.setActionType("START_FOCUS");
            startFocus.setActionLabel("Start a focused study sprint");
            Map<String, Object> focusData = new HashMap<>();
            focusData.put("topicId", topicId);
            startFocus.setActionData(focusData);
            actions.add(startFocus);

            // View Topic Details action
            ChatDTO.SuggestedAction viewTopic = new ChatDTO.SuggestedAction();
            viewTopic.setActionType("VIEW_TOPIC");
            viewTopic.setActionLabel("View full topic details");
            Map<String, Object> viewData = new HashMap<>();
            viewData.put("topicId", topicId);
            viewTopic.setActionData(viewData);
            actions.add(viewTopic);
        } else {
            // Browse Topics action
            ChatDTO.SuggestedAction browsTopics = new ChatDTO.SuggestedAction();
            browsTopics.setActionType("BROWSE_TOPICS");
            browsTopics.setActionLabel("Browse available topics");
            browsTopics.setActionData(new HashMap<>());
            actions.add(browsTopics);
        }

        return actions;
    }

    /**
     * Generate notes for a topic in DEV mode
     */
    public String generateNotes(Long topicId, String format, String language) {
        log.debug("[DEV AI] Generating {} notes for topic {}", format, topicId);

        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return "# Topic Not Found\n\nUnable to generate notes for the specified topic.";
        }

        // Try to use content chunks if available
        List<ContentChunk> chunks = contentChunkRepository.findByTopicIdAndStatus(
            topicId,
            ContentChunk.ChunkStatus.ACTIVE
        );

        if (!chunks.isEmpty()) {
            return generateNotesFromContent(topic, chunks, format);
        } else {
            return generateFallbackNotes(topic, format);
        }
    }

    private String generateNotesFromContent(Topic topic, List<ContentChunk> chunks, String format) {
        StringBuilder notes = new StringBuilder();
        
        if ("SHORT".equals(format)) {
            notes.append("# ").append(topic.getTitle()).append(" - Quick Notes\n\n");
            notes.append("## Key Concepts\n\n");
            notes.append(chunks.get(0).getChunkText()).append("\n\n");
            notes.append("## Remember\n\n");
            notes.append("• Practice regularly\n");
            notes.append("• Review examples\n");
            notes.append("• Ask questions when stuck\n");
        } else if ("LONG".equals(format)) {
            notes.append("# ").append(topic.getTitle()).append(" - Comprehensive Notes\n\n");
            notes.append("## Introduction\n\n");
            notes.append(chunks.get(0).getChunkText()).append("\n\n");
            
            if (chunks.size() > 1) {
                notes.append("## Detailed Explanation\n\n");
                notes.append(chunks.get(1).getChunkText()).append("\n\n");
            }
            
            if (chunks.size() > 2) {
                notes.append("## Additional Concepts\n\n");
                notes.append(chunks.get(2).getChunkText()).append("\n\n");
            }
            
            notes.append("## Summary\n\n");
            notes.append("Understanding ").append(topic.getTitle());
            notes.append(" is crucial for your mathematics foundation. ");
            notes.append("Make sure to practice regularly and seek help when needed.\n\n");
            
            notes.append("## Practice Tips\n\n");
            notes.append("1. Review the key concepts daily\n");
            notes.append("2. Work through example problems\n");
            notes.append("3. Create your own practice questions\n");
            notes.append("4. Discuss with peers or teachers\n");
        } else { // REVISION_SHEET
            notes.append("# ").append(topic.getTitle()).append(" - Revision Sheet\n\n");
            notes.append("## Quick Review\n\n");
            notes.append(chunks.get(0).getChunkText()).append("\n\n");
            notes.append("## Key Formulas & Rules\n\n");
            notes.append("• Review your textbook for specific formulas\n");
            notes.append("• Practice applying concepts to problems\n\n");
            notes.append("## Common Mistakes to Avoid\n\n");
            notes.append("• Not reading questions carefully\n");
            notes.append("• Skipping steps in calculations\n");
            notes.append("• Not checking your answers\n");
        }
        
        return notes.toString();
    }

    private String generateFallbackNotes(Topic topic, String format) {
        return String.format("# %s - %s\n\n" +
            "## Topic Overview\n\n" +
            "%s\n\n" +
            "## What to Study\n\n" +
            "• Review your textbook chapter on this topic\n" +
            "• Work through practice problems\n" +
            "• Create flashcards for key concepts\n" +
            "• Book a session with a teacher for detailed explanation\n\n" +
            "## Next Steps\n\n" +
            "To get detailed notes with curriculum content, make sure content chunks " +
            "are loaded for this topic. Contact your administrator if content is missing.",
            topic.getTitle(),
            format.equals("SHORT") ? "Quick Notes" : "Comprehensive Notes",
            topic.getDescription() != null ? topic.getDescription() : "Important mathematical concepts");
    }

    /**
     * Generate quiz questions in DEV mode
     */
    public String generateQuizQuestions(Long topicId, int questionCount, String difficulty) {
        log.debug("[DEV AI] Generating {} {} quiz questions for topic {}", questionCount, difficulty, topicId);

        // Return deterministic quiz questions in JSON format
        return "{\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"question\": \"What is 2 + 2?\",\n" +
            "      \"options\": [\"3\", \"4\", \"5\", \"6\"],\n" +
            "      \"correctAnswer\": \"4\",\n" +
            "      \"explanation\": \"Basic addition: 2 + 2 = 4\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"question\": \"What is 5 × 3?\",\n" +
            "      \"options\": [\"8\", \"12\", \"15\", \"18\"],\n" +
            "      \"correctAnswer\": \"15\",\n" +
            "      \"explanation\": \"Multiplication: 5 × 3 = 15\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }

    private String generateSessionId() {
        return "dev-session-" + System.currentTimeMillis();
    }

    /**
     * Simulate voice transcription (STT) in DEV mode
     */
    public String transcribeAudio(byte[] audioData) {
        log.debug("[DEV AI] Simulating audio transcription");
        return "This is a simulated transcription of the audio input for E2E testing.";
    }

    /**
     * Simulate text-to-speech (TTS) in DEV mode
     */
    public byte[] synthesizeSpeech(String text) {
        log.debug("[DEV AI] Simulating speech synthesis for text: {}", text.substring(0, Math.min(50, text.length())));
        // Return empty byte array as mock audio
        return new byte[0];
    }
}
