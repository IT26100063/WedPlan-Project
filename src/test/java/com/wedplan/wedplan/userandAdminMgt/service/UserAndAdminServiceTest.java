package com.wedplan.wedplan.userandAdminMgt.service;

import com.wedplan.wedplan.userandAdminMgt.model.UserAndAdminModel;
import com.wedplan.wedplan.userandAdminMgt.repository.UserAndAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class UserAndAdminServiceTest {

    private UserAndAdminRepository repository;
    private UserAndAdminService service;

    @BeforeEach
    void setUp() {
        repository = (UserAndAdminRepository) Proxy.newProxyInstance(
                UserAndAdminRepository.class.getClassLoader(),
                new Class<?>[]{UserAndAdminRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findByEmail".equals(name)) {
                        String email = (String) args[0];
                        if ("admin@wedplan.com".equals(email)) {
                            return new UserAndAdminModel(1L, "Admin User", "admin@wedplan.com", "secret123", "ADMIN");
                        }
                        return null;
                    }
                    if ("save".equals(name)) {
                        return args[0];
                    }
                    return null;
                }
        );
        service = new UserAndAdminService(repository);
    }

    @Test
    void testAuthenticationSuccess() {
        UserAndAdminModel user = service.authenticate("admin@wedplan.com", "secret123");
        assertNotNull(user);
        assertEquals("Admin User", user.getName());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void testAuthenticationFailure() {
        UserAndAdminModel user = service.authenticate("admin@wedplan.com", "wrongpassword");
        assertNull(user);
    }
}
