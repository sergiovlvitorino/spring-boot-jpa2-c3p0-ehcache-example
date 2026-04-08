package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveCommand(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String job) {
}
