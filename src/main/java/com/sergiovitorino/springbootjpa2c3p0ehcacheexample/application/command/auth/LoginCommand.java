package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String username,
        @NotBlank String password) {
}
