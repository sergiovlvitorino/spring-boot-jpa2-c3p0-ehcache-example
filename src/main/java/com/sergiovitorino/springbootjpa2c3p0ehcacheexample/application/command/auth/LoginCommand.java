package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginCommand {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
