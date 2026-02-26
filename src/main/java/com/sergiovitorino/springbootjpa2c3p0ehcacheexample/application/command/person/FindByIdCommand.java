package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FindByIdCommand {

    @NotNull
    private UUID id;

}
