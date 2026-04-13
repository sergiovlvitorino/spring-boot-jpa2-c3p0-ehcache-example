package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Address;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.AddressRepository;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;

    @Transactional
    public Address create(UUID personId, Address address) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + personId));
        address.setPerson(person);
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Page<Address> findByPersonId(UUID personId, Pageable pageable) {
        if (!personRepository.existsById(personId)) {
            throw new EntityNotFoundException("Person not found with id: " + personId);
        }
        return addressRepository.findByPersonId(personId, pageable);
    }

    @Transactional
    public void delete(UUID personId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        if (!address.getPerson().getId().equals(personId)) {
            throw new EntityNotFoundException("Address not found for person: " + personId);
        }
        addressRepository.delete(address);
    }
}
