package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth.LoginCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.TooManyRequestsException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.JwtUtil;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginCommand command,
                                                     HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (rateLimiter.isBlocked(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new TooManyRequestsException("Too many login attempts. Please try again later.");
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(command.username());
        } catch (UsernameNotFoundException e) {
            rateLimiter.recordAttempt(clientIp);
            log.warn("Login failed for username: {}", command.username());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            rateLimiter.recordAttempt(clientIp);
            log.warn("Login failed for username: {}", command.username());
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("Login successful for username: {}", command.username());
        String token = jwtUtil.generateToken(command.username());
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
    }

}
