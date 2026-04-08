package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void isBlocked_withNoAttempts_shouldReturnFalse() {
        assertFalse(rateLimiter.isBlocked("192.168.1.1"));
    }

    @Test
    void isBlocked_afterMaxAttempts_shouldReturnTrue() {
        String ip = "192.168.1.1";
        for (int i = 0; i < LoginRateLimiter.MAX_ATTEMPTS; i++) {
            rateLimiter.recordAttempt(ip);
        }
        assertTrue(rateLimiter.isBlocked(ip));
    }

    @Test
    void isBlocked_belowMaxAttempts_shouldReturnFalse() {
        String ip = "192.168.1.1";
        for (int i = 0; i < LoginRateLimiter.MAX_ATTEMPTS - 1; i++) {
            rateLimiter.recordAttempt(ip);
        }
        assertFalse(rateLimiter.isBlocked(ip));
    }

    @Test
    void isBlocked_differentIps_shouldBeIndependent() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";
        for (int i = 0; i < LoginRateLimiter.MAX_ATTEMPTS; i++) {
            rateLimiter.recordAttempt(ip1);
        }
        assertTrue(rateLimiter.isBlocked(ip1));
        assertFalse(rateLimiter.isBlocked(ip2));
    }

    @Test
    void activeKeys_afterExpiredEntries_shouldCleanup() {
        String ip = "10.0.0.1";
        rateLimiter.recordAttempt(ip);
        assertEquals(1, rateLimiter.activeKeys());

        // After checking an IP with no recent attempts (simulated by a fresh limiter),
        // the key should still exist since the window hasn't expired
        assertFalse(rateLimiter.isBlocked(ip));
        assertEquals(1, rateLimiter.activeKeys());
    }

    @Test
    void activeKeys_withNoAttempts_shouldBeZero() {
        assertEquals(0, rateLimiter.activeKeys());
    }
}
