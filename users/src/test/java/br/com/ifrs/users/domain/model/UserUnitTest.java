package br.com.ifrs.users.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserUnitTest {

    @Test
    void constructorAndAccessorsPreserveUserData() {
        User user = new User("Ana Silva", "ana@example.test", "hashed-password");
        user.setDataCriacao("2026-10-12T12:00:00");

        assertEquals("Ana Silva", user.getName());
        assertEquals("ana@example.test", user.getEmail());
        assertEquals("hashed-password", user.getPassword());
        assertEquals("2026-10-12T12:00:00", user.getDataCriacao());
    }
}
