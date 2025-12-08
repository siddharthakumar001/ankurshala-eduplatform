package com.ankurshala.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    // In-memory lock registry (for single instance)
    // TODO: Replace with Redis-based distributed locks for multi-instance deployment
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Execute operation with distributed lock
     * 
     * @param lockKey Unique lock identifier (e.g., "booking:accept:123")
     * @param timeoutSeconds Maximum time to wait for lock acquisition
     * @param operation Operation to execute while holding the lock
     * @return Result from operation
     * @throws LockAcquisitionException if lock cannot be acquired within timeout
     */
    public <T> T executeWithLock(String lockKey, long timeoutSeconds, Supplier<T> operation) {
        Lock lock = getLock(lockKey);
        
        boolean acquired = false;
        try {
            log.debug("[DISTRIBUTED_LOCK] Attempting to acquire lock: {}", lockKey);
            acquired = lock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!acquired) {
                log.warn("[DISTRIBUTED_LOCK] Failed to acquire lock {} within {} seconds", 
                        lockKey, timeoutSeconds);
                throw new LockAcquisitionException(
                    String.format("Could not acquire lock %s within %d seconds", lockKey, timeoutSeconds));
            }
            
            log.debug("[DISTRIBUTED_LOCK] ✅ Lock acquired: {}", lockKey);
            
            // Execute the operation while holding the lock
            T result = operation.get();
            
            log.debug("[DISTRIBUTED_LOCK] Operation completed successfully with lock: {}", lockKey);
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DISTRIBUTED_LOCK] Lock acquisition interrupted for: {}", lockKey, e);
            throw new LockAcquisitionException("Lock acquisition interrupted", e);
        } finally {
            if (acquired) {
                lock.unlock();
                log.debug("[DISTRIBUTED_LOCK] Lock released: {}", lockKey);
            }
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
        Lock lock = getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * Release a previously acquired lock
     * 
     * @param lockKey Unique lock identifier
     */
    public void unlock(String lockKey) {
        Lock lock = locks.get(lockKey);
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

    /**
     * Get or create lock for given key
     */
    private Lock getLock(String lockKey) {
        return locks.computeIfAbsent(lockKey, k -> new ReentrantLock(true)); // fair lock
    }

    /**
     * Clean up unused locks (for memory management)
     * Call periodically via scheduled task
     */
    public void cleanupUnusedLocks() {
        locks.entrySet().removeIf(entry -> {
            ReentrantLock lock = entry.getValue();
            // Remove if not locked and no threads waiting
            return !lock.isLocked() && !lock.hasQueuedThreads();
        });
        log.info("[DISTRIBUTED_LOCK] Cleaned up unused locks. Current lock count: {}", locks.size());
    }

    /**
     * Get current number of active locks
     */
    public int getActiveLockCount() {
        return (int) locks.values().stream()
                .filter(ReentrantLock::isLocked)
                .count();
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
