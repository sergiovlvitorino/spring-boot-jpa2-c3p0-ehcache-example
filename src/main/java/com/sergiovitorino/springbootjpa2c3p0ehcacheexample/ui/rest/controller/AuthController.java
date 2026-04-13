package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth.LoginCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginCommand command,
                                                     HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        String token = authService.authenticate(command.username(), command.password(), clientIp);
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
