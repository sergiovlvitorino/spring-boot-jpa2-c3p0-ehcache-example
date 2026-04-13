package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    Page<Address> findByPersonId(UUID personId, Pageable pageable);
}
