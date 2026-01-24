package com.ankurshala.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.connection.BitFieldSubCommands;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight in-memory Redis substitute for tests so that we never reach a real Redis server.
 * Supports the small subset of operations our services exercise (get/set/increment/ttl/setIfAbsent).
 */
@Configuration
@Profile("test")
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplateString() {
        return new InMemoryRedisTemplate<>();
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplateObject() {
        return new InMemoryRedisTemplate<>();
    }

    /**
     * Simple in-memory RedisTemplate replacement.
     */
    static class InMemoryRedisTemplate<K, V> extends RedisTemplate<K, V> {
        private final ConcurrentHashMap<K, ExpiringValue<V>> store = new ConcurrentHashMap<>();

        private static class ExpiringValue<V> {
            V value;
            long expiresAt; // 0 = no expiry
        }

        private boolean isExpired(ExpiringValue<V> entry) {
            return entry != null && entry.expiresAt > 0 && System.currentTimeMillis() > entry.expiresAt;
        }

        private V getValue(K key) {
            ExpiringValue<V> entry = store.get(key);
            if (isExpired(entry)) {
                store.remove(key);
                return null;
            }
            return entry == null ? null : entry.value;
        }

        @SuppressWarnings("unchecked")
        private void setValue(K key, Object value, Duration ttl) {
            ExpiringValue<V> entry = new ExpiringValue<>();
            entry.value = (V) value;
            entry.expiresAt = (ttl == null || ttl.isZero() || ttl.isNegative())
                    ? 0
                    : System.currentTimeMillis() + ttl.toMillis();
            store.put(key, entry);
        }

        @Override
        public ValueOperations<K, V> opsForValue() {
            return new InMemoryValueOps();
        }

        @Override
        public Boolean delete(K key) {
            return store.remove(key) != null;
        }

        @Override
        public Long getExpire(K key, TimeUnit timeUnit) {
            ExpiringValue<V> entry = store.get(key);
            if (entry == null || entry.expiresAt == 0) {
                return -1L;
            }
            long remaining = entry.expiresAt - System.currentTimeMillis();
            return remaining <= 0 ? -1L : timeUnit.convert(remaining, TimeUnit.MILLISECONDS);
        }

        private class InMemoryValueOps implements ValueOperations<K, V> {
            @Override
            public void set(K key, V value) {
                setValue(key, value, null);
            }

            @Override
            public void set(K key, V value, long timeout) {
                setValue(key, value, Duration.ofSeconds(timeout));
            }

            @Override
            public void set(K key, V value, long timeout, TimeUnit unit) {
                setValue(key, value, Duration.ofMillis(unit.toMillis(timeout)));
            }

            @Override
            public Boolean setIfAbsent(K key, V value) {
                return setIfAbsent(key, value, 0, TimeUnit.MILLISECONDS);
            }

            @Override
            public Boolean setIfAbsent(K key, V value, long timeout, TimeUnit unit) {
                if (store.containsKey(key) && !isExpired(store.get(key))) {
                    return false;
                }
                setValue(key, value, timeout > 0 ? Duration.ofMillis(unit.toMillis(timeout)) : null);
                return true;
            }

            @Override
            public Boolean setIfPresent(K key, V value) {
                if (getValue(key) == null) {
                    return false;
                }
                setValue(key, value, null);
                return true;
            }

            @Override
            public Boolean setIfPresent(K key, V value, long timeout, TimeUnit unit) {
                if (getValue(key) == null) {
                    return false;
                }
                setValue(key, value, Duration.ofMillis(unit.toMillis(timeout)));
                return true;
            }

            @Override
            public void multiSet(Map<? extends K, ? extends V> m) {
                m.forEach(this::set);
            }

            @Override
            public Boolean multiSetIfAbsent(Map<? extends K, ? extends V> m) {
                boolean nonePresent = m.keySet().stream().noneMatch(k -> getValue(k) != null);
                if (nonePresent) {
                    multiSet(m);
                    return true;
                }
                return false;
            }

            @Override
            public V get(Object key) {
                return getValue((K) key);
            }

            @Override
            public String get(K key, long start, long end) {
                V current = getValue(key);
                if (current == null) return null;
                String s = String.valueOf(current);
                int from = (int) Math.max(0, start);
                int to = (int) Math.min(s.length(), end + 1);
                String slice = from >= to ? "" : s.substring(from, to);
                return slice;
            }

            @Override
            public V getAndDelete(K key) {
                V current = getValue(key);
                delete(key);
                return current;
            }

            @Override
            public V getAndExpire(K key, long timeout, TimeUnit unit) {
                V current = getValue(key);
                if (current != null) {
                    setValue(key, current, Duration.ofMillis(unit.toMillis(timeout)));
                }
                return current;
            }

            @Override
            public V getAndExpire(K key, Duration timeout) {
                V current = getValue(key);
                if (current != null) {
                    setValue(key, current, timeout);
                }
                return current;
            }

            @Override
            public V getAndPersist(K key) {
                V current = getValue(key);
                if (current != null) {
                    setValue(key, current, null);
                }
                return current;
            }

            @Override
            public V getAndSet(K key, V value) {
                V old = getValue(key);
                setValue(key, value, null);
                return old;
            }

            @Override
            public List<V> multiGet(Collection<K> keys) {
                List<V> results = new ArrayList<>(keys.size());
                for (K key : keys) {
                    results.add(getValue(key));
                }
                return results;
            }

            @Override
            public Long increment(K key) {
                return increment(key, 1L);
            }

            @Override
            public Long increment(K key, long delta) {
                V current = getValue(key);
                long value = current == null ? 0L : Long.parseLong(String.valueOf(current));
                value += delta;
                setValue(key, (V) Long.valueOf(value), null);
                return value;
            }

            @Override
            public Double increment(K key, double delta) {
                V current = getValue(key);
                double value = current == null ? 0D : Double.parseDouble(String.valueOf(current));
                value += delta;
                setValue(key, (V) Double.valueOf(value), null);
                return value;
            }

            @Override
            public Long decrement(K key) {
                return increment(key, -1L);
            }

            @Override
            public Long decrement(K key, long delta) {
                return increment(key, -delta);
            }

            @Override
            public Integer append(K key, String value) {
                String current = Optional.ofNullable(getValue(key))
                        .map(String::valueOf)
                        .orElse("");
                String newVal = current + value;
                setValue(key, newVal, null);
                return newVal.length();
            }

            @Override
            public Long size(K key) {
                V val = getValue(key);
                if (val == null) return 0L;
                return (long) String.valueOf(val).length();
            }

            @Override
            public Boolean setBit(K key, long offset, boolean value) {
                // Not used in tests; return false to indicate no previous value
                return false;
            }

            @Override
            public Boolean getBit(K key, long offset) {
                return false;
            }

            @Override
            public List<Long> bitField(K key, BitFieldSubCommands subCommands) {
                return Collections.emptyList();
            }

            @Override
            public RedisOperations<K, V> getOperations() {
                return InMemoryRedisTemplate.this;
            }

            @Override
            public void set(K key, V value, Duration timeout) {
                setValue(key, value, timeout);
            }
        }

        // The following operations are unused in tests; provide safe defaults
        @Override
        public Boolean expire(K key, long timeout, TimeUnit unit) {
            ExpiringValue<V> entry = store.get(key);
            if (entry == null) return false;
            entry.expiresAt = System.currentTimeMillis() + unit.toMillis(timeout);
            return true;
        }

        @Override
        public Boolean expire(K key, Duration timeout) {
            ExpiringValue<V> entry = store.get(key);
            if (entry == null) return false;
            entry.expiresAt = System.currentTimeMillis() + timeout.toMillis();
            return true;
        }

        @Override
        public Boolean persist(K key) {
            ExpiringValue<V> entry = store.get(key);
            if (entry == null) return false;
            entry.expiresAt = 0;
            return true;
        }

        @Override
        public DataType type(K key) {
            return DataType.STRING;
        }

        @Override
        public Long convertAndSend(String channel, Object message) {
            return 0L;
        }

        @Override
        public void afterPropertiesSet() {
            // Skip connection factory checks; this template is fully in-memory for tests.
        }
    }
}
