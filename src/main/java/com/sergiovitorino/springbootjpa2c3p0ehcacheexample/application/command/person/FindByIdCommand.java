package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class FindByIdCommand {
    @NotNull
    private UUID id;
}
