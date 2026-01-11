package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.dto.ai.ContentIngestionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Caching service for AI responses and RAG results.
 * Uses Redis for distributed caching with configurable TTLs.
 */
@Service
@Slf4j
public class AICacheService {

    private static final String CACHE_PREFIX = "ai:cache:";
    private static final String RAG_CACHE_PREFIX = "ai:rag:";
    private static final String EMBEDDING_CACHE_PREFIX = "ai:emb:";

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.ai.cache.chat-ttl-seconds:300}")  // 5 minutes default
    private int chatCacheTtl;

    @Value("${app.ai.cache.rag-ttl-seconds:3600}")  // 1 hour default
    private int ragCacheTtl;

    @Value("${app.ai.cache.embedding-ttl-seconds:86400}")  // 24 hours default
    private int embeddingCacheTtl;

    @Value("${app.ai.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Get cached chat response if available
     */
    public ChatDTO.ChatResponse getCachedChatResponse(String message, Long topicId, String language) {
        if (!cacheEnabled || redisTemplate == null) return null;

        try {
            String key = buildChatCacheKey(message, topicId, language);
            String cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                log.debug("Cache hit for chat response - Key: {}", key);
                return objectMapper.readValue(cached, ChatDTO.ChatResponse.class);
            }

        } catch (Exception e) {
            log.warn("Failed to get cached chat response: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Cache a chat response
     */
    public void cacheChatResponse(String message, Long topicId, String language, ChatDTO.ChatResponse response) {
        if (!cacheEnabled || redisTemplate == null) return;

        // Don't cache safety-filtered or error responses
        if (Boolean.TRUE.equals(response.getSafetyFiltered()) || response.getMessage().contains("error")) {
            return;
        }

        try {
            String key = buildChatCacheKey(message, topicId, language);
            String value = objectMapper.writeValueAsString(response);
            
            redisTemplate.opsForValue().set(key, value, chatCacheTtl, TimeUnit.SECONDS);
            log.debug("Cached chat response - Key: {}, TTL: {}s", key, chatCacheTtl);

        } catch (Exception e) {
            log.warn("Failed to cache chat response: {}", e.getMessage());
        }
    }

    /**
     * Get cached RAG search results
     */
    public ContentIngestionDTO.SemanticSearchResponse getCachedRagResults(String query, Long topicId, Long subjectId, String language) {
        if (!cacheEnabled || redisTemplate == null) return null;

        try {
            String key = buildRagCacheKey(query, topicId, subjectId, language);
            String cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                log.debug("Cache hit for RAG results - Key: {}", key);
                return objectMapper.readValue(cached, ContentIngestionDTO.SemanticSearchResponse.class);
            }

        } catch (Exception e) {
            log.warn("Failed to get cached RAG results: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Cache RAG search results
     */
    public void cacheRagResults(String query, Long topicId, Long subjectId, String language, 
                                ContentIngestionDTO.SemanticSearchResponse response) {
        if (!cacheEnabled || redisTemplate == null) return;

        try {
            String key = buildRagCacheKey(query, topicId, subjectId, language);
            String value = objectMapper.writeValueAsString(response);
            
            redisTemplate.opsForValue().set(key, value, ragCacheTtl, TimeUnit.SECONDS);
            log.debug("Cached RAG results - Key: {}, TTL: {}s, Results: {}", key, ragCacheTtl, response.getTotalResults());

        } catch (Exception e) {
            log.warn("Failed to cache RAG results: {}", e.getMessage());
        }
    }

    /**
     * Get cached embedding for text
     */
    public float[] getCachedEmbedding(String text) {
        if (!cacheEnabled || redisTemplate == null) return null;

        try {
            String key = buildEmbeddingCacheKey(text);
            String cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                log.debug("Cache hit for embedding");
                return objectMapper.readValue(cached, float[].class);
            }

        } catch (Exception e) {
            log.warn("Failed to get cached embedding: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Cache embedding for text
     */
    public void cacheEmbedding(String text, float[] embedding) {
        if (!cacheEnabled || redisTemplate == null) return;

        try {
            String key = buildEmbeddingCacheKey(text);
            String value = objectMapper.writeValueAsString(embedding);
            
            redisTemplate.opsForValue().set(key, value, embeddingCacheTtl, TimeUnit.SECONDS);
            log.debug("Cached embedding - Key length: {}, TTL: {}s", key.length(), embeddingCacheTtl);

        } catch (Exception e) {
            log.warn("Failed to cache embedding: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all caches for a topic (after content update)
     */
    public void invalidateTopicCache(Long topicId) {
        if (redisTemplate == null) return;

        try {
            String pattern = RAG_CACHE_PREFIX + "*:topic:" + topicId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} cache entries for topic {}", keys.size(), topicId);
            }

        } catch (Exception e) {
            log.warn("Failed to invalidate topic cache: {}", e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        if (redisTemplate == null) {
            return stats;
        }

        try {
            // Count keys by type
            var chatKeys = redisTemplate.keys(CACHE_PREFIX + "*");
            var ragKeys = redisTemplate.keys(RAG_CACHE_PREFIX + "*");
            var embeddingKeys = redisTemplate.keys(EMBEDDING_CACHE_PREFIX + "*");

            stats.setChatCacheSize(chatKeys != null ? chatKeys.size() : 0);
            stats.setRagCacheSize(ragKeys != null ? ragKeys.size() : 0);
            stats.setEmbeddingCacheSize(embeddingKeys != null ? embeddingKeys.size() : 0);
            stats.setCacheEnabled(cacheEnabled);

        } catch (Exception e) {
            log.warn("Failed to get cache stats: {}", e.getMessage());
        }

        return stats;
    }

    /**
     * Clear all AI caches (admin action)
     */
    public void clearAllCaches() {
        if (redisTemplate == null) return;

        try {
            var chatKeys = redisTemplate.keys(CACHE_PREFIX + "*");
            var ragKeys = redisTemplate.keys(RAG_CACHE_PREFIX + "*");
            var embeddingKeys = redisTemplate.keys(EMBEDDING_CACHE_PREFIX + "*");

            int total = 0;
            if (chatKeys != null) { redisTemplate.delete(chatKeys); total += chatKeys.size(); }
            if (ragKeys != null) { redisTemplate.delete(ragKeys); total += ragKeys.size(); }
            if (embeddingKeys != null) { redisTemplate.delete(embeddingKeys); total += embeddingKeys.size(); }

            log.info("Cleared {} AI cache entries", total);

        } catch (Exception e) {
            log.error("Failed to clear AI caches: {}", e.getMessage());
        }
    }

    // Key building methods

    private String buildChatCacheKey(String message, Long topicId, String language) {
        String normalized = normalizeForCache(message);
        String hash = hashString(normalized);
        return CACHE_PREFIX + "chat:" + (language != null ? language : "en") + ":" + 
               (topicId != null ? "topic:" + topicId + ":" : "") + hash;
    }

    private String buildRagCacheKey(String query, Long topicId, Long subjectId, String language) {
        String normalized = normalizeForCache(query);
        String hash = hashString(normalized);
        StringBuilder key = new StringBuilder(RAG_CACHE_PREFIX);
        key.append(language != null ? language : "en").append(":");
        if (topicId != null) key.append("topic:").append(topicId).append(":");
        if (subjectId != null) key.append("subject:").append(subjectId).append(":");
        key.append(hash);
        return key.toString();
    }

    private String buildEmbeddingCacheKey(String text) {
        String hash = hashString(text);
        return EMBEDDING_CACHE_PREFIX + hash;
    }

    private String normalizeForCache(String text) {
        if (text == null) return "";
        return text.toLowerCase().trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z0-9\\s]", "");
    }

    private String hashString(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16);  // Use first 16 chars
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(text.hashCode());
        }
    }

    // Stats class

    public static class CacheStats {
        private int chatCacheSize;
        private int ragCacheSize;
        private int embeddingCacheSize;
        private boolean cacheEnabled;

        public int getChatCacheSize() { return chatCacheSize; }
        public void setChatCacheSize(int size) { this.chatCacheSize = size; }
        public int getRagCacheSize() { return ragCacheSize; }
        public void setRagCacheSize(int size) { this.ragCacheSize = size; }
        public int getEmbeddingCacheSize() { return embeddingCacheSize; }
        public void setEmbeddingCacheSize(int size) { this.embeddingCacheSize = size; }
        public boolean isCacheEnabled() { return cacheEnabled; }
        public void setCacheEnabled(boolean enabled) { this.cacheEnabled = enabled; }
        public int getTotalSize() { return chatCacheSize + ragCacheSize + embeddingCacheSize; }
    }
}

