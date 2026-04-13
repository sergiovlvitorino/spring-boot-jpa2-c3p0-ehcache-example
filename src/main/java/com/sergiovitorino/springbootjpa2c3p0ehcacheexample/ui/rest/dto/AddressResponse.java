package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.dto;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Address;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(UUID id, String street, String city, String state, String zipCode,
                               LocalDateTime createdAt) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCreatedAt());
    }
}
