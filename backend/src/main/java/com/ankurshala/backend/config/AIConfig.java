package com.ankurshala.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AI/LLM integration.
 * Provides a provider-agnostic layer for AI services.
 * 
 * Supports multiple providers: OpenAI, Anthropic, Azure OpenAI, Google Gemini
 * Configuration is determined by environment variables.
 */
@Configuration
@Slf4j
public class AIConfig {

    @Value("${spring.ai.openai.api-key:#{null}}")
    private String openaiApiKey;

    @Value("${spring.ai.openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${spring.ai.openai.embedding-model:text-embedding-ada-002}")
    private String embeddingModel;

    @Value("${app.ai.provider:openai}")
    private String aiProvider;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.dev-mode:${DEMO_ENV:local}}")
    private String devMode;

    @Value("${app.ai.max-tokens:2000}")
    private int maxTokens;

    @Value("${app.ai.temperature:0.7}")
    private double temperature;

    @Value("${app.ai.safety.enabled:true}")
    private boolean safetyEnabled;

    /**
     * AI Properties bean for injection into services
     */
    @Bean
    public AIProperties aiProperties() {
        AIProperties props = new AIProperties();
        props.setEnabled(aiEnabled);
        props.setProvider(aiProvider);
        props.setModel(openaiModel);
        props.setEmbeddingModel(embeddingModel);
        props.setMaxTokens(maxTokens);
        props.setTemperature(temperature);
        props.setSafetyEnabled(safetyEnabled);
        props.setApiKeyConfigured(openaiApiKey != null && !openaiApiKey.isEmpty());
        
        // DEV mode check
        boolean isDevMode = "true".equalsIgnoreCase(devMode) || "local".equalsIgnoreCase(devMode);
        props.setDevMode(isDevMode);

        if (aiEnabled) {
            if (isDevMode) {
                log.info("AI Configuration initialized - DEV MODE (no real API calls), SafetyEnabled: {}", safetyEnabled);
            } else {
                log.info("AI Configuration initialized - Provider: {}, Model: {}, SafetyEnabled: {}", 
                        aiProvider, openaiModel, safetyEnabled);
            }
        } else {
            log.info("AI features are disabled");
        }

        return props;
    }

    /**
     * Properties holder for AI configuration
     */
    public static class AIProperties {
        private boolean enabled;
        private boolean devMode;
        private String provider;
        private String model;
        private String embeddingModel;
        private int maxTokens;
        private double temperature;
        private boolean safetyEnabled;
        private boolean apiKeyConfigured;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isDevMode() { return devMode; }
        public void setDevMode(boolean devMode) { this.devMode = devMode; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public boolean isSafetyEnabled() { return safetyEnabled; }
        public void setSafetyEnabled(boolean safetyEnabled) { this.safetyEnabled = safetyEnabled; }

        public boolean isApiKeyConfigured() { return apiKeyConfigured; }
        public void setApiKeyConfigured(boolean apiKeyConfigured) { this.apiKeyConfigured = apiKeyConfigured; }

        /**
         * Check if AI features are available (enabled and configured)
         */
        public boolean isAvailable() {
            return enabled && apiKeyConfigured;
        }
    }
}

