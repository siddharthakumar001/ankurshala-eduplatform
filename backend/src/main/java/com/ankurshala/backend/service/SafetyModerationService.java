package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for content safety and moderation.
 * Ensures AI responses are age-appropriate and safe for students.
 */
@Service
@Slf4j
public class SafetyModerationService {

    @Autowired
    private AIConfig.AIProperties aiProperties;

    // Patterns for unsafe content detection
    private static final List<Pattern> UNSAFE_PATTERNS = Arrays.asList(
            // Personal information requests
            Pattern.compile("(?i)(your|student'?s?)\\s*(phone|address|home|location|password|credit card|bank)", Pattern.CASE_INSENSITIVE),
            // Medical advice
            Pattern.compile("(?i)(prescribe|diagnose|medical\\s+advice|take\\s+medication|symptoms\\s+of)", Pattern.CASE_INSENSITIVE),
            // Legal advice
            Pattern.compile("(?i)(legal\\s+advice|sue|lawsuit|attorney|lawyer\\s+recommend)", Pattern.CASE_INSENSITIVE),
            // Explicit content
            Pattern.compile("(?i)(porn|explicit|sexual|nude|naked|xxx)", Pattern.CASE_INSENSITIVE),
            // Violence
            Pattern.compile("(?i)(kill|murder|weapon|bomb|attack|hurt\\s+someone)", Pattern.CASE_INSENSITIVE),
            // Self-harm
            Pattern.compile("(?i)(suicide|self[\\s-]?harm|cut\\s+myself|end\\s+my\\s+life)", Pattern.CASE_INSENSITIVE),
            // Drugs/Alcohol
            Pattern.compile("(?i)(buy\\s+drugs|sell\\s+drugs|get\\s+drunk|alcohol\\s+underage)", Pattern.CASE_INSENSITIVE),
            // Cheating/Dishonesty
            Pattern.compile("(?i)(cheat\\s+on\\s+exam|hack\\s+grades|fake\\s+certificate)", Pattern.CASE_INSENSITIVE)
    );

    // Off-topic patterns (not educational)
    private static final List<Pattern> OFF_TOPIC_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)(celebrity\\s+gossip|movie\\s+spoilers|dating\\s+advice|relationship\\s+problems)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(stock\\s+tips|invest\\s+money|cryptocurrency|gambling)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(political\\s+opinion|vote\\s+for|religion\\s+better)", Pattern.CASE_INSENSITIVE)
    );

    // Prompt injection patterns
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)(ignore\\s+(previous|all|above)\\s+instructions)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(you\\s+are\\s+now|pretend\\s+to\\s+be|act\\s+as\\s+if)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(system\\s*:?\\s*prompt|\\[system\\]|<\\|im_start\\|>)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(bypass|override|disable)\\s+.*?(safety|filter|restriction)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)jailbreak|DAN\\s+mode", Pattern.CASE_INSENSITIVE)
    );

    // Safe, canned responses for different categories
    private static final Map<String, String> SAFE_RESPONSES = Map.of(
            "HARMFUL", "I can't help with that request. Let's focus on your studies! Is there a topic you'd like to learn about?",
            "MEDICAL", "For medical questions, please consult a doctor or healthcare professional. I can help you understand biology and health science concepts from your curriculum instead.",
            "LEGAL", "For legal matters, please consult a qualified lawyer or legal professional. I can help you learn about civics and law concepts from your textbook.",
            "PERSONAL_DATA", "I don't collect or need personal information. Let's keep our conversation focused on your learning!",
            "OFF_TOPIC", "That's outside my area of expertise. I'm here to help with your schoolwork. What subject can I assist you with?",
            "SELF_HARM", "I'm concerned about what you shared. Please talk to a trusted adult, parent, teacher, or call a helpline. You're not alone, and help is available.",
            "INJECTION", "I noticed something unusual in your message. Let's stick to educational topics! What would you like to learn about?",
            "DEFAULT", "I'm not sure I can help with that. I'm best at helping with your school subjects like Math, Science, English, and Social Studies. What would you like to learn?"
    );

    /**
     * Check if user input is safe to process
     */
    public ChatDTO.SafetyCheckResult checkUserInput(String input) {
        String traceId = TraceUtil.getTraceId();
        
        if (!aiProperties.isSafetyEnabled()) {
            return ChatDTO.SafetyCheckResult.builder()
                    .isSafe(true)
                    .category("SAFE")
                    .build();
        }

        log.debug("Checking user input safety - TraceId: {}", traceId);

        // Check for prompt injection first
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("Prompt injection detected - TraceId: {}", traceId);
                return ChatDTO.SafetyCheckResult.builder()
                        .isSafe(false)
                        .category("INJECTION")
                        .reason("Potential prompt injection attempt detected")
                        .suggestedResponse(SAFE_RESPONSES.get("INJECTION"))
                        .build();
            }
        }

        // Check for harmful content
        for (Pattern pattern : UNSAFE_PATTERNS) {
            if (pattern.matcher(input).find()) {
                String category = categorizeUnsafeContent(input);
                log.warn("Unsafe content detected - TraceId: {}, Category: {}", traceId, category);
                return ChatDTO.SafetyCheckResult.builder()
                        .isSafe(false)
                        .category(category)
                        .reason("Content flagged as potentially inappropriate for student platform")
                        .suggestedResponse(SAFE_RESPONSES.getOrDefault(category, SAFE_RESPONSES.get("DEFAULT")))
                        .build();
            }
        }

        // Check for off-topic content
        for (Pattern pattern : OFF_TOPIC_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.info("Off-topic content detected - TraceId: {}", traceId);
                return ChatDTO.SafetyCheckResult.builder()
                        .isSafe(false)
                        .category("OFF_TOPIC")
                        .reason("Question is outside educational scope")
                        .suggestedResponse(SAFE_RESPONSES.get("OFF_TOPIC"))
                        .build();
            }
        }

        return ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();
    }

    /**
     * Check if AI response is safe to send to student
     */
    public ChatDTO.SafetyCheckResult checkAIResponse(String response) {
        String traceId = TraceUtil.getTraceId();
        
        if (!aiProperties.isSafetyEnabled()) {
            return ChatDTO.SafetyCheckResult.builder()
                    .isSafe(true)
                    .category("SAFE")
                    .build();
        }

        // Check for harmful content in response
        for (Pattern pattern : UNSAFE_PATTERNS) {
            if (pattern.matcher(response).find()) {
                log.warn("Unsafe AI response detected - TraceId: {}", traceId);
                return ChatDTO.SafetyCheckResult.builder()
                        .isSafe(false)
                        .category("HARMFUL_RESPONSE")
                        .reason("AI response contains potentially harmful content")
                        .suggestedResponse(SAFE_RESPONSES.get("DEFAULT"))
                        .build();
            }
        }

        return ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();
    }

    /**
     * Sanitize retrieved content to remove any injected instructions
     */
    public String sanitizeRetrievedContent(String content) {
        if (content == null) return "";
        
        String sanitized = content;
        
        // Remove potential system prompt injections
        for (Pattern pattern : INJECTION_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("[REDACTED]");
        }
        
        return sanitized;
    }

    /**
     * Build safety flags map for logging
     */
    public Map<String, Object> buildSafetyFlags(ChatDTO.SafetyCheckResult inputCheck, ChatDTO.SafetyCheckResult outputCheck) {
        Map<String, Object> flags = new HashMap<>();
        flags.put("inputSafe", inputCheck.getIsSafe());
        flags.put("inputCategory", inputCheck.getCategory());
        if (!inputCheck.getIsSafe()) {
            flags.put("inputReason", inputCheck.getReason());
        }
        if (outputCheck != null) {
            flags.put("outputSafe", outputCheck.getIsSafe());
            flags.put("outputCategory", outputCheck.getCategory());
        }
        return flags;
    }

    /**
     * Categorize unsafe content into specific types
     */
    private String categorizeUnsafeContent(String input) {
        String lowered = input.toLowerCase();
        
        if (lowered.contains("suicide") || lowered.contains("self-harm") || lowered.contains("hurt myself")) {
            return "SELF_HARM";
        }
        if (lowered.contains("medical") || lowered.contains("prescribe") || lowered.contains("diagnose") || lowered.contains("medication")) {
            return "MEDICAL";
        }
        if (lowered.contains("legal") || lowered.contains("lawyer") || lowered.contains("sue")) {
            return "LEGAL";
        }
        if (lowered.contains("password") || lowered.contains("address") || lowered.contains("phone number") || lowered.contains("credit card")) {
            return "PERSONAL_DATA";
        }
        
        return "HARMFUL";
    }

    /**
     * Get student-friendly message for safety refusal
     */
    public String getRefusalMessage(String category) {
        return SAFE_RESPONSES.getOrDefault(category, SAFE_RESPONSES.get("DEFAULT"));
    }

    /**
     * Check if AI features should be available based on safety status
     */
    public boolean isSafetyEnabled() {
        return aiProperties.isSafetyEnabled();
    }
}

