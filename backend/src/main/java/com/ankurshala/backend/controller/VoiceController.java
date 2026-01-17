package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.VoiceService;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Voice API endpoints for speech-to-text and text-to-speech functionality.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", 
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/student/ai/voice")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Voice AI", description = "Speech-to-text and text-to-speech APIs")
public class VoiceController {

    @Autowired
    private VoiceService voiceService;

    @Autowired
    private LoggingService loggingService;

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcribe audio to text (Speech-to-Text)")
    public ResponseEntity<ApiResponse<Map<String, String>>> transcribeAudio(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "language", required = false, defaultValue = "en-IN") String language,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("audioSize", audioFile.getSize());
        context.put("language", language);

        loggingService.logBusinessOperationStart("VOICE_TRANSCRIBE", studentId.toString(), context);

        try {
            if (audioFile.isEmpty()) {
                ApiResponse<Map<String, String>> errorResponse = ApiResponse.error("Audio file is empty");
                errorResponse.setTraceId(traceId);
                errorResponse.setRequestId(requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            byte[] audioData = audioFile.getBytes();
            String transcript = voiceService.transcribeAudio(audioData, language);

            Map<String, String> result = new HashMap<>();
            result.put("transcript", transcript);
            result.put("language", language);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("VOICE_TRANSCRIBE", studentId.toString(), true, executionTime);

            ApiResponse<Map<String, String>> apiResponse = ApiResponse.success(result, "Audio transcribed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("VOICE_TRANSCRIBE", studentId.toString(), false, executionTime);
            loggingService.logError("VOICE_TRANSCRIBE", e, context);
            
            ApiResponse<Map<String, String>> errorResponse = ApiResponse.error("Failed to transcribe audio: " + e.getMessage());
            errorResponse.setTraceId(traceId);
            errorResponse.setRequestId(requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/synthesize", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Synthesize text to speech (Text-to-Speech)")
    public ResponseEntity<byte[]> synthesizeSpeech(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        TraceUtil.getTraceId(); // For logging context
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        String text = request.get("text");
        String language = request.getOrDefault("language", "en-IN");
        String voiceName = request.getOrDefault("voice", null);

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("textLength", text != null ? text.length() : 0);
        context.put("language", language);

        loggingService.logBusinessOperationStart("VOICE_SYNTHESIZE", studentId.toString(), context);

        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] audioData = voiceService.synthesizeSpeech(text, language, voiceName);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("VOICE_SYNTHESIZE", studentId.toString(), true, executionTime);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "speech.wav");
            headers.setContentLength(audioData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("VOICE_SYNTHESIZE", studentId.toString(), false, executionTime);
            loggingService.logError("VOICE_SYNTHESIZE", e, context);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

