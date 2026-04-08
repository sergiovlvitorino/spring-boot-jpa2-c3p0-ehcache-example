package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth.LoginCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.AuthController;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.PersonController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de arquitetura para garantir que decisoes tecnicas nao regridam:
 * - Constructor injection em todos os componentes (sem @Autowired em campos)
 * - Commands como Java records (imutabilidade)
 */
class SecurityArchitectureTest {

    @Test
    void securityConfig_shouldNotHaveAutowiredFields() {
        assertNoAutowiredFields(SecurityConfig.class);
    }

    @Test
    void jwtUtil_shouldNotHaveAutowiredFields() {
        assertNoAutowiredFields(JwtUtil.class);
    }

    @Test
    void authController_shouldNotHaveAutowiredFields() {
        assertNoAutowiredFields(AuthController.class);
    }

    @Test
    void personService_shouldNotHaveAutowiredFields() {
        assertNoAutowiredFields(PersonService.class);
    }

    @Test
    void personController_shouldNotHaveAutowiredFields() {
        assertNoAutowiredFields(PersonController.class);
    }

    @Test
    void saveCommand_shouldBeRecord() {
        assertTrue(SaveCommand.class.isRecord(),
                "SaveCommand deve ser um Java record para garantir imutabilidade");
    }

    @Test
    void loginCommand_shouldBeRecord() {
        assertTrue(LoginCommand.class.isRecord(),
                "LoginCommand deve ser um Java record para garantir imutabilidade");
    }

    /**
     * Verifica que nenhum campo da classe possui @Autowired.
     * Constructor injection e o padrao obrigatorio do projeto.
     */
    private void assertNoAutowiredFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            boolean hasAutowired = Arrays.stream(field.getAnnotations())
                    .anyMatch(a -> a.annotationType().equals(Autowired.class));
            assertFalse(hasAutowired,
                    String.format("Campo '%s' em %s nao deve ter @Autowired. Use constructor injection.",
                            field.getName(), clazz.getSimpleName()));
        }
    }
}
