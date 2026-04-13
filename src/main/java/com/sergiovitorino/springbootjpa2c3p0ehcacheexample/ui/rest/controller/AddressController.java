package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.address.CreateAddressCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AddressService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Address;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.dto.AddressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/person/{personId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(@PathVariable UUID personId,
                                                   @RequestBody @Valid CreateAddressCommand command) {
        Address address = Address.builder()
                .street(command.street())
                .city(command.city())
                .state(command.state())
                .zipCode(command.zipCode())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AddressResponse.from(addressService.create(personId, address)));
    }

    @GetMapping
    public ResponseEntity<Page<AddressResponse>> findByPersonId(
            @PathVariable UUID personId,
            @PageableDefault(size = 20, sort = "street") Pageable pageable) {
        return ResponseEntity.ok(addressService.findByPersonId(personId, pageable).map(AddressResponse::from));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable UUID personId, @PathVariable UUID addressId) {
        addressService.delete(personId, addressId);
        return ResponseEntity.noContent().build();
    }
}
