package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonEqualsTest {

    @Test
    void equals_sameId_shouldBeEqual() {
        UUID id = UUID.randomUUID();
        Person p1 = Person.builder().id(id).name("Alice").job("Engineer").build();
        Person p2 = Person.builder().id(id).name("Bob").job("Designer").build();

        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    @Test
    void equals_differentId_shouldNotBeEqual() {
        Person p1 = Person.builder().id(UUID.randomUUID()).name("Alice").job("Engineer").build();
        Person p2 = Person.builder().id(UUID.randomUUID()).name("Alice").job("Engineer").build();

        assertFalse(p1.equals(p2));
    }

    @Test
    void equals_nullId_shouldNotBeEqual() {
        Person withId = Person.builder().id(UUID.randomUUID()).name("Alice").job("Engineer").build();
        Person withoutId = Person.builder().name("Alice").job("Engineer").build();

        assertFalse(withoutId.equals(withId));
        assertFalse(withId.equals(withoutId));
    }

    @Test
    void equals_sameReference_shouldBeEqual() {
        Person p = Person.builder().id(UUID.randomUUID()).name("Alice").job("Engineer").build();

        assertTrue(p.equals(p));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void equals_null_shouldNotBeEqual() {
        Person p = Person.builder().id(UUID.randomUUID()).name("Alice").job("Engineer").build();

        assertFalse(p.equals(null));
    }

    @Test
    void hashCode_sameId_shouldBeEqual() {
        UUID id = UUID.randomUUID();
        Person p1 = Person.builder().id(id).name("Alice").job("Engineer").build();
        Person p2 = Person.builder().id(id).name("Bob").job("Designer").build();

        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
