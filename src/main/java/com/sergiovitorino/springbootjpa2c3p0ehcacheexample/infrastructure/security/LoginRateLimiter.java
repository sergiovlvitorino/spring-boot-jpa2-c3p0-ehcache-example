package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Deque<Instant> deque = attempts.get(key);
        if (deque == null) return false;
        cleanOldEntries(deque);
        if (deque.isEmpty()) {
            attempts.remove(key);
            return false;
        }
        return deque.size() >= MAX_ATTEMPTS;
    }

    public void recordAttempt(String key) {
        Deque<Instant> deque = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        cleanOldEntries(deque);
        deque.add(Instant.now());
    }

    int activeKeys() {
        return attempts.size();
    }

    private void cleanOldEntries(Deque<Instant> deque) {
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
            deque.pollFirst();
        }
    }
}
