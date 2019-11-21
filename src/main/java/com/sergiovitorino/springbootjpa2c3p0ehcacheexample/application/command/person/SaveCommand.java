package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SaveCommand {
    @NotNull
    private String name;

    @NotNull
    private String job;
}
