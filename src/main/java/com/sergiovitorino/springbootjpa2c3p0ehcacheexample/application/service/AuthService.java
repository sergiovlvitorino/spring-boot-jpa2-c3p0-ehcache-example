package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.TooManyRequestsException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.JwtUtil;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.LoginRateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final MeterRegistry meterRegistry;

    public String authenticate(String username, String password, String clientIp) {
        if (rateLimiter.isBlocked(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            meterRegistry.counter("auth.login", "result", "rate_limited").increment();
            throw new TooManyRequestsException("Too many login attempts. Please try again later.");
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            rateLimiter.recordAttempt(clientIp);
            log.warn("Login failed for username: {}", username);
            meterRegistry.counter("auth.login", "result", "failure").increment();
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            rateLimiter.recordAttempt(clientIp);
            log.warn("Login failed for username: {}", username);
            meterRegistry.counter("auth.login", "result", "failure").increment();
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("Login successful for username: {}", username);
        meterRegistry.counter("auth.login", "result", "success").increment();
        return jwtUtil.generateToken(username);
    }
}
