package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.util.IndianLanguageSupport;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Voice Service for speech-to-text and text-to-speech functionality.
 * Supports Azure Speech Service and fallback to browser APIs.
 */
@Service
@Slf4j
public class VoiceService {

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @Value("${app.ai.azure.speech.key:#{null}}")
    private String azureSpeechKey;

    @Value("${app.ai.azure.speech.region:#{null}}")
    private String azureSpeechRegion;

    @Value("${app.ai.azure.speech.language:en-IN}")
    private String defaultLanguage;

    @Autowired(required = false)
    private DevAIProvider devAIProvider;

    /**
     * Transcribe audio to text using Azure Speech Service or fallback
     */
    public String transcribeAudio(byte[] audioData, String language) {
        String traceId = com.ankurshala.backend.util.TraceUtil.getTraceId();
        log.info("Transcribing audio - TraceId: {}, AudioSize: {} bytes, Language: {}", 
                traceId, audioData.length, language);

        // DEV MODE: Use DevAIProvider
        if (aiProperties.isDevMode() && devAIProvider != null) {
            log.debug("Using DevAIProvider for audio transcription");
            return devAIProvider.transcribeAudio(audioData);
        }

        // Check if Azure Speech is configured
        if (azureSpeechKey == null || azureSpeechKey.isEmpty() || 
            azureSpeechRegion == null || azureSpeechRegion.isEmpty()) {
            log.warn("Azure Speech Service not configured - returning placeholder");
            return "Audio transcription requires Azure Speech Service configuration. " +
                   "Please configure AZURE_SPEECH_KEY and AZURE_SPEECH_REGION environment variables.";
        }

        try {
            // Use Azure Speech SDK for transcription
            return transcribeWithAzure(audioData, language != null ? language : defaultLanguage);
        } catch (Exception e) {
            log.error("Audio transcription failed - TraceId: {}", traceId, e);
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }

    /**
     * Synthesize text to speech using Azure Speech Service or fallback
     */
    public byte[] synthesizeSpeech(String text, String language, String voiceName) {
        String traceId = com.ankurshala.backend.util.TraceUtil.getTraceId();
        log.info("Synthesizing speech - TraceId: {}, TextLength: {}, Language: {}, Voice: {}", 
                traceId, text.length(), language, voiceName);

        // DEV MODE: Use DevAIProvider
        if (aiProperties.isDevMode() && devAIProvider != null) {
            log.debug("Using DevAIProvider for speech synthesis");
            return devAIProvider.synthesizeSpeech(text);
        }

        // Check if Azure Speech is configured
        if (azureSpeechKey == null || azureSpeechKey.isEmpty() || 
            azureSpeechRegion == null || azureSpeechRegion.isEmpty()) {
            log.warn("Azure Speech Service not configured - returning empty audio");
            return new byte[0];
        }

        try {
            // Use Azure Speech SDK for synthesis
            return synthesizeWithAzure(text, language != null ? language : defaultLanguage, voiceName);
        } catch (Exception e) {
            log.error("Speech synthesis failed - TraceId: {}", traceId, e);
            throw new RuntimeException("Failed to synthesize speech: " + e.getMessage(), e);
        }
    }

    /**
     * Transcribe audio using Azure Speech Service
     */
    private String transcribeWithAzure(byte[] audioData, String language) throws Exception {
        try {
            // Get locale code for the language
            String localeCode = IndianLanguageSupport.getLanguageInfo(language != null ? language.split("-")[0] : "en").getLocaleCode();
            
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureSpeechKey, azureSpeechRegion);
            speechConfig.setSpeechRecognitionLanguage(localeCode);
            
            // Create audio stream from byte array
            PushAudioInputStream pushStream = AudioInputStream.createPushStream();
            pushStream.write(audioData);
            pushStream.close();
            
            AudioConfig audioConfig = AudioConfig.fromStreamInput(pushStream);
            SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);
            
            Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
            SpeechRecognitionResult result = task.get();
            
            recognizer.close();
            audioConfig.close();
            speechConfig.close();
            
            if (result.getReason() == ResultReason.RecognizedSpeech) {
                return result.getText();
            } else if (result.getReason() == ResultReason.NoMatch) {
                log.warn("No speech could be recognized");
                return "";
            } else {
                log.error("Speech recognition failed: {}", result.getReason());
                throw new RuntimeException("Speech recognition failed: " + result.getReason());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during speech recognition", e);
            throw new RuntimeException("Speech recognition error: " + e.getMessage(), e);
        }
    }

    /**
     * Synthesize speech using Azure Speech Service
     */
    private byte[] synthesizeWithAzure(String text, String language, String voiceName) throws Exception {
        try {
            // Get locale code and default voice for the language
            String langCode = language != null ? language.split("-")[0] : "en";
            IndianLanguageSupport.LanguageInfo langInfo = IndianLanguageSupport.getLanguageInfo(langCode);
            String voice = voiceName != null ? voiceName : langInfo.getFemaleVoice();
            
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureSpeechKey, azureSpeechRegion);
            speechConfig.setSpeechSynthesisVoiceName(voice);
            
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);
            
            Future<SpeechSynthesisResult> task = synthesizer.SpeakTextAsync(text);
            SpeechSynthesisResult result = task.get();
            
            synthesizer.close();
            speechConfig.close();
            
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                return result.getAudioData();
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = 
                    SpeechSynthesisCancellationDetails.fromResult(result);
                log.error("Speech synthesis canceled: {}", cancellation.getReason());
                throw new RuntimeException("Speech synthesis canceled: " + cancellation.getReason());
            } else {
                log.error("Speech synthesis failed: {}", result.getReason());
                throw new RuntimeException("Speech synthesis failed: " + result.getReason());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during speech synthesis", e);
            throw new RuntimeException("Speech synthesis error: " + e.getMessage(), e);
        }
    }

    /**
     * Convert audio bytes to WAV format (if needed)
     */
    public byte[] convertToWav(byte[] audioData, int sampleRate, int channels, int bitsPerSample) {
        try {
            ByteArrayOutputStream wavStream = new ByteArrayOutputStream();
            
            // WAV header
            int dataSize = audioData.length;
            int fileSize = 36 + dataSize;
            
            // Write WAV header
            wavStream.write("RIFF".getBytes());
            wavStream.write(intToByteArray(fileSize));
            wavStream.write("WAVE".getBytes());
            wavStream.write("fmt ".getBytes());
            wavStream.write(intToByteArray(16)); // fmt chunk size
            wavStream.write(shortToByteArray((short) 1)); // audio format (PCM)
            wavStream.write(shortToByteArray((short) channels));
            wavStream.write(intToByteArray(sampleRate));
            wavStream.write(intToByteArray(sampleRate * channels * bitsPerSample / 8)); // byte rate
            wavStream.write(shortToByteArray((short) (channels * bitsPerSample / 8))); // block align
            wavStream.write(shortToByteArray((short) bitsPerSample));
            wavStream.write("data".getBytes());
            wavStream.write(intToByteArray(dataSize));
            wavStream.write(audioData);
            
            return wavStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to convert audio to WAV", e);
            return audioData; // Return original if conversion fails
        }
    }

    private byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    private byte[] shortToByteArray(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }
}

