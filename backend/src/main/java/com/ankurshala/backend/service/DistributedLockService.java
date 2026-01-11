package com.ankurshala.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Distributed Lock Service for high-concurrency booking operations
 * 
 * This implementation uses in-memory locks suitable for single-instance deployment.
 * For multi-instance production deployment, replace with Redis-based distributed locks
 * using Redisson or similar library.
 * 
 * Usage Example:
 * <pre>
 * Booking result = lockService.executeWithLock(
 *     "booking:accept:" + bookingId,
 *     10, // timeout seconds
 *     () -> acceptBooking(bookingId, teacherId)
 * );
 * </pre>
 */
@Slf4j
@Service
public class DistributedLockService {

    @Value("${app.booking.use-redis-locks:false}")
    private boolean useRedisLocks;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    // In-memory lock registry (for single instance / local dev)
    private final ConcurrentHashMap<String, ReentrantLock> inMemoryLocks = new ConcurrentHashMap<>();
    
    // Thread-local lock value for Redis locks (unique per thread/request)
    private static final ThreadLocal<String> lockValue = new ThreadLocal<>();

    // Lua script for atomic lock release (ensures only lock owner can release)
    private static final String RELEASE_LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else return 0 end";

    /**
     * Execute operation with distributed lock
     * Uses Redis-based locks if enabled, otherwise in-memory locks (for local dev)
     * 
     * @param lockKey Unique lock identifier (e.g., "booking:accept:123")
     * @param timeoutSeconds Maximum time to wait for lock acquisition
     * @param operation Operation to execute while holding the lock
     * @return Result from operation
     * @throws LockAcquisitionException if lock cannot be acquired within timeout
     */
    public <T> T executeWithLock(String lockKey, long timeoutSeconds, Supplier<T> operation) {
        if (useRedisLocks && redisTemplate != null) {
            return executeWithRedisLock(lockKey, timeoutSeconds, operation);
        } else {
            return executeWithInMemoryLock(lockKey, timeoutSeconds, operation);
        }
    }

    /**
     * Execute with Redis-based distributed lock (for multi-instance deployment)
     */
    private <T> T executeWithRedisLock(String lockKey, long timeoutSeconds, Supplier<T> operation) {
        String redisKey = "lock:" + lockKey;
        String lockValueStr = UUID.randomUUID().toString();
        lockValue.set(lockValueStr);
        
        boolean acquired = false;
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("[REDIS_LOCK] Attempting to acquire Redis lock: {}", redisKey);
            
            // Try to acquire lock with expiration (prevent deadlock)
            long expireTime = timeoutSeconds + 5; // Lock expires slightly after timeout
            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
                Boolean acquiredLock = redisTemplate.opsForValue().setIfAbsent(
                    redisKey, lockValueStr, expireTime, TimeUnit.SECONDS);
                
                if (Boolean.TRUE.equals(acquiredLock)) {
                    acquired = true;
                    log.debug("[REDIS_LOCK] ✅ Lock acquired: {}", redisKey);
                    break;
                }
                
                // Wait a bit before retrying
                try {
                    Thread.sleep(50); // 50ms retry interval
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new LockAcquisitionException("Lock acquisition interrupted", e);
                }
            }
            
            if (!acquired) {
                log.warn("[REDIS_LOCK] Failed to acquire lock {} within {} seconds", 
                        redisKey, timeoutSeconds);
                throw new LockAcquisitionException(
                    String.format("Could not acquire Redis lock %s within %d seconds", lockKey, timeoutSeconds));
            }
            
            // Execute the operation while holding the lock
            T result = operation.get();
            
            log.debug("[REDIS_LOCK] Operation completed successfully with lock: {}", redisKey);
            return result;
            
        } finally {
            if (acquired) {
                releaseRedisLock(redisKey, lockValueStr);
                lockValue.remove();
            }
        }
    }

    /**
     * Execute with in-memory lock (for local dev / single instance)
     */
    private <T> T executeWithInMemoryLock(String lockKey, long timeoutSeconds, Supplier<T> operation) {
        Lock lock = getInMemoryLock(lockKey);
        
        boolean acquired = false;
        try {
            log.debug("[IN_MEMORY_LOCK] Attempting to acquire lock: {}", lockKey);
            acquired = lock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!acquired) {
                log.warn("[IN_MEMORY_LOCK] Failed to acquire lock {} within {} seconds", 
                        lockKey, timeoutSeconds);
                throw new LockAcquisitionException(
                    String.format("Could not acquire lock %s within %d seconds", lockKey, timeoutSeconds));
            }
            
            log.debug("[IN_MEMORY_LOCK] ✅ Lock acquired: {}", lockKey);
            
            // Execute the operation while holding the lock
            T result = operation.get();
            
            log.debug("[IN_MEMORY_LOCK] Operation completed successfully with lock: {}", lockKey);
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[IN_MEMORY_LOCK] Lock acquisition interrupted for: {}", lockKey, e);
            throw new LockAcquisitionException("Lock acquisition interrupted", e);
        } finally {
            if (acquired) {
                lock.unlock();
                log.debug("[IN_MEMORY_LOCK] Lock released: {}", lockKey);
            }
        }
    }

    /**
     * Release Redis lock atomically (only if still owned by this thread)
     */
    private void releaseRedisLock(String redisKey, String lockValueStr) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RELEASE_LOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), lockValueStr);
            
            if (Long.valueOf(1).equals(result)) {
                log.debug("[REDIS_LOCK] Lock released: {}", redisKey);
            } else {
                log.warn("[REDIS_LOCK] Lock {} was already released or expired", redisKey);
            }
        } catch (Exception e) {
            log.error("[REDIS_LOCK] Error releasing lock {}: {}", redisKey, e.getMessage());
        }
    }

    /**
     * Execute operation with distributed lock (void return)
     * 
     * @param lockKey Unique lock identifier
     * @param timeoutSeconds Maximum time to wait for lock acquisition
     * @param operation Operation to execute
     * @throws LockAcquisitionException if lock cannot be acquired
     */
    public void executeWithLock(String lockKey, long timeoutSeconds, Runnable operation) {
        executeWithLock(lockKey, timeoutSeconds, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Try to acquire lock without blocking
     * 
     * @param lockKey Unique lock identifier
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String lockKey) {
        if (useRedisLocks && redisTemplate != null) {
            String redisKey = "lock:" + lockKey;
            String lockValueStr = UUID.randomUUID().toString();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                redisKey, lockValueStr, 30, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                lockValue.set(lockValueStr);
            }
            return Boolean.TRUE.equals(acquired);
        } else {
            Lock lock = getInMemoryLock(lockKey);
            return lock.tryLock();
        }
    }

    /**
     * Release a previously acquired lock
     * 
     * @param lockKey Unique lock identifier
     */
    public void unlock(String lockKey) {
        if (useRedisLocks && redisTemplate != null) {
            String redisKey = "lock:" + lockKey;
            String lockValueStr = lockValue.get();
            if (lockValueStr != null) {
                releaseRedisLock(redisKey, lockValueStr);
                lockValue.remove();
            }
        } else {
            Lock lock = inMemoryLocks.get(lockKey);
            if (lock != null) {
                try {
                    lock.unlock();
                    log.debug("[DISTRIBUTED_LOCK] Lock released: {}", lockKey);
                } catch (IllegalMonitorStateException e) {
                    log.warn("[DISTRIBUTED_LOCK] Attempted to unlock a lock not held by current thread: {}", 
                            lockKey);
                }
            }
        }
    }

    /**
     * Get or create in-memory lock for given key
     */
    private Lock getInMemoryLock(String lockKey) {
        return inMemoryLocks.computeIfAbsent(lockKey, k -> new ReentrantLock(true)); // fair lock
    }

    /**
     * Clean up unused locks (for memory management)
     * Only applies to in-memory locks; Redis locks auto-expire
     * Call periodically via scheduled task
     */
    public void cleanupUnusedLocks() {
        if (!useRedisLocks) {
            inMemoryLocks.entrySet().removeIf(entry -> {
                ReentrantLock lock = entry.getValue();
                // Remove if not locked and no threads waiting
                return !lock.isLocked() && !lock.hasQueuedThreads();
            });
            log.info("[DISTRIBUTED_LOCK] Cleaned up unused in-memory locks. Current lock count: {}", 
                    inMemoryLocks.size());
        } else {
            log.debug("[DISTRIBUTED_LOCK] Using Redis locks - no cleanup needed (auto-expire)");
        }
    }

    /**
     * Get current number of active locks
     */
    public int getActiveLockCount() {
        if (!useRedisLocks) {
            return (int) inMemoryLocks.values().stream()
                    .filter(ReentrantLock::isLocked)
                    .count();
        } else {
            // For Redis locks, this would require scanning Redis keys
            // For now, return -1 to indicate "not applicable"
            return -1;
        }
    }

    /**
     * Exception thrown when lock acquisition fails
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
        
        public LockAcquisitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
