package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressCommand(
        @NotBlank @Size(max = 200) String street,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 50) String state,
        @NotBlank @Size(max = 20) String zipCode) {
}
