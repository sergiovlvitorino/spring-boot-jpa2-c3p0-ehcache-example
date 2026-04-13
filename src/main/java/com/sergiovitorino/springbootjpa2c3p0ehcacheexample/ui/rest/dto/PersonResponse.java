package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.dto;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;

import java.time.LocalDateTime;
import java.util.UUID;

public record PersonResponse(UUID id, String name, String job, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static PersonResponse from(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getName(),
                person.getJob(),
                person.getCreatedAt(),
                person.getUpdatedAt());
    }
}
