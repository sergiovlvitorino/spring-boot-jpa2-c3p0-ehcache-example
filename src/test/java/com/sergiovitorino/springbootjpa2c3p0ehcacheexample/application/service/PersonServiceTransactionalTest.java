package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para garantir que PersonService possui as anotacoes @Transactional corretas.
 * Isso evita regressao da divida tecnica onde operacoes de banco
 * nao estavam dentro de transacoes explicitas.
 */
class PersonServiceTransactionalTest {

    @Test
    void save_shouldHaveTransactionalAnnotation() throws NoSuchMethodException {
        Method saveMethod = PersonService.class.getMethod("save",
                com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person.class);

        Transactional transactional = saveMethod.getAnnotation(Transactional.class);

        assertNotNull(transactional,
                "PersonService.save() deve ter @Transactional");
        assertFalse(transactional.readOnly(),
                "PersonService.save() nao deve ser readOnly");
    }

    @Test
    void findById_shouldHaveTransactionalReadOnly() throws NoSuchMethodException {
        Method findByIdMethod = PersonService.class.getMethod("findById", UUID.class);

        Transactional transactional = findByIdMethod.getAnnotation(Transactional.class);

        assertNotNull(transactional,
                "PersonService.findById() deve ter @Transactional");
        assertTrue(transactional.readOnly(),
                "PersonService.findById() deve ter @Transactional(readOnly = true)");
    }
}
