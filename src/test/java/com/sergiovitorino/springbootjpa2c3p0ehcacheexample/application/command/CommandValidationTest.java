package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth.LoginCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommandValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ---- SaveCommand ----

    @Test
    void saveCommand_withValidData_shouldHaveNoViolations() {
        SaveCommand command = new SaveCommand("Alice", "Engineer");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    void saveCommand_withBlankName_shouldFailValidation() {
        SaveCommand command = new SaveCommand("  ", "Engineer");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void saveCommand_withNullName_shouldFailValidation() {
        SaveCommand command = new SaveCommand(null, "Engineer");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void saveCommand_withBlankJob_shouldFailValidation() {
        SaveCommand command = new SaveCommand("Alice", "");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("job")));
    }

    @Test
    void saveCommand_withNameExceeding100Chars_shouldFailValidation() {
        SaveCommand command = new SaveCommand("A".repeat(101), "Engineer");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void saveCommand_withNameExactly100Chars_shouldHaveNoViolations() {
        SaveCommand command = new SaveCommand("A".repeat(100), "Engineer");

        Set<ConstraintViolation<SaveCommand>> violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    // ---- LoginCommand ----

    @Test
    void loginCommand_withValidData_shouldHaveNoViolations() {
        LoginCommand command = new LoginCommand("admin", "secret");

        Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginCommand_withBlankUsername_shouldFailValidation() {
        LoginCommand command = new LoginCommand("", "secret");

        Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void loginCommand_withBlankPassword_shouldFailValidation() {
        LoginCommand command = new LoginCommand("admin", "  ");

        Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void loginCommand_withNullPassword_shouldFailValidation() {
        LoginCommand command = new LoginCommand("admin", null);

        Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

}
