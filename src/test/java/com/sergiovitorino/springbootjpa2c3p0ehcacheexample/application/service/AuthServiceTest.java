package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.TooManyRequestsException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.JwtUtil;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.LoginRateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginRateLimiter rateLimiter;

    private MeterRegistry meterRegistry;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        authService = new AuthService(jwtUtil, userDetailsService, passwordEncoder, rateLimiter, meterRegistry);
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnToken() {
        // Arrange
        String username = "admin";
        String password = "changeme";
        String clientIp = "127.0.0.1";
        String expectedToken = "mocked-jwt-token";

        UserDetails user = User.withUsername(username)
                .password("encoded-password")
                .roles("USER")
                .build();

        when(rateLimiter.isBlocked(clientIp)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(username)).thenReturn(expectedToken);

        // Act
        String token = authService.authenticate(username, password, clientIp);

        // Assert
        assertEquals(expectedToken, token);
        verify(rateLimiter).isBlocked(clientIp);
        verify(rateLimiter, never()).recordAttempt(anyString());
        verify(jwtUtil).generateToken(username);

        assertEquals(1.0, meterRegistry.counter("auth.login", "result", "success").count());
        assertEquals(0.0, meterRegistry.counter("auth.login", "result", "failure").count());
    }

    @Test
    void authenticate_withInvalidPassword_shouldThrowBadCredentials_andRecordAttempt() {
        // Arrange
        String username = "admin";
        String password = "wrongpassword";
        String clientIp = "192.168.1.1";

        UserDetails user = User.withUsername(username)
                .password("encoded-password")
                .roles("USER")
                .build();

        when(rateLimiter.isBlocked(clientIp)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, "encoded-password")).thenReturn(false);

        // Act & Assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(username, password, clientIp)
        );

        assertEquals("Invalid credentials", ex.getMessage());
        verify(rateLimiter).recordAttempt(clientIp);
        verify(jwtUtil, never()).generateToken(anyString());

        assertEquals(1.0, meterRegistry.counter("auth.login", "result", "failure").count());
        assertEquals(0.0, meterRegistry.counter("auth.login", "result", "success").count());
    }

    @Test
    void authenticate_withUnknownUser_shouldThrowBadCredentials_andRecordAttempt() {
        // Arrange
        String username = "unknown";
        String password = "anypassword";
        String clientIp = "10.0.0.1";

        when(rateLimiter.isBlocked(clientIp)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(new UsernameNotFoundException("User not found: " + username));

        // Act & Assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(username, password, clientIp)
        );

        assertEquals("Invalid credentials", ex.getMessage());
        verify(rateLimiter).recordAttempt(clientIp);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());

        assertEquals(1.0, meterRegistry.counter("auth.login", "result", "failure").count());
    }

    @Test
    void authenticate_whenBlocked_shouldThrowTooManyRequests() {
        // Arrange
        String username = "admin";
        String password = "changeme";
        String clientIp = "10.0.0.5";

        when(rateLimiter.isBlocked(clientIp)).thenReturn(true);

        // Act & Assert
        TooManyRequestsException ex = assertThrows(
                TooManyRequestsException.class,
                () -> authService.authenticate(username, password, clientIp)
        );

        assertTrue(ex.getMessage().contains("Too many login attempts"));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
        verify(rateLimiter, never()).recordAttempt(anyString());

        assertEquals(1.0, meterRegistry.counter("auth.login", "result", "rate_limited").count());
    }
}
