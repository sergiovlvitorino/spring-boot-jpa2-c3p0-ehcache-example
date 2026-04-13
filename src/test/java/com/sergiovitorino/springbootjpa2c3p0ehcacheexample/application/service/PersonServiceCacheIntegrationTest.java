package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "app.admin.username=admin",
        "app.admin.password=changeme",
        "app.jwt.secret=test-secret-key-must-be-at-least-32-chars!",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=caffeine",
        "spring.cache.cache-names=personCache",
        "spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m"
})
class PersonServiceCacheIntegrationTest {

    @Autowired
    private PersonService personService;

    @MockitoSpyBean
    private PersonRepository personRepository;

    @Test
    void findById_secondCall_shouldReturnFromCacheWithoutQueryingRepository() {
        // Save directly via repository to bypass @CachePut (start with cold cache)
        Person person = Person.builder().name("Cache Test").job("Tester").build();
        Person saved = personRepository.save(person);
        UUID id = saved.getId();
        assertNotNull(id);

        Mockito.clearInvocations(personRepository);

        // First call — cache miss, hits repository
        Person firstCall = personService.findById(id);

        // Second call — cache hit, should NOT hit repository
        Person secondCall = personService.findById(id);

        assertEquals(firstCall.getId(), secondCall.getId());
        assertEquals("Cache Test", secondCall.getName());
        // Repository.findById should have been called only once (cache hit on second)
        verify(personRepository, times(1)).findById(id);
    }

    @Test
    void save_shouldPutInCache_andFindByIdShouldReturnCachedValue() {
        // Save via service — @CachePut populates cache
        Person person = Person.builder().name("Cached Save").job("Developer").build();
        Person saved = personService.save(person);
        UUID id = saved.getId();
        assertNotNull(id);

        Mockito.clearInvocations(personRepository);

        // findById should return from cache without calling repository
        Person found = personService.findById(id);

        assertEquals(id, found.getId());
        assertEquals("Cached Save", found.getName());
        verify(personRepository, times(0)).findById(id);
    }
}
