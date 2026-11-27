package br.com.ifrs.users.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.com.ifrs.users.domain.model.User;

class UserEntityTest {

    // Verifica a cópia de todos os campos do domínio para a entidade.
    @Test
    void fromDomainCopiesAllUserFields() {
        User user = new User(42L, "Ana", "ana@example.test", "hashed", "2026-11-26");

        UserEntity entity = UserEntity.fromDomain(user);

        assertAll(
            () -> assertEquals(42L, entity.id),
            () -> assertEquals("Ana", entity.name),
            () -> assertEquals("ana@example.test", entity.email),
            () -> assertEquals("hashed", entity.password),
            () -> assertEquals("2026-11-26", entity.dataCriacao)
        );
    }

    // Verifica a cópia de todos os campos da entidade para o domínio.
    @Test
    void toDomainCopiesAllUserFields() {
        UserEntity entity = new UserEntity();
        entity.id = 42L;
        entity.name = "Ana";
        entity.email = "ana@example.test";
        entity.password = "hashed";
        entity.dataCriacao = "2026-11-26";

        User user = entity.toDomain();

        assertAll(
            () -> assertEquals(42L, user.getId()),
            () -> assertEquals("Ana", user.getName()),
            () -> assertEquals("ana@example.test", user.getEmail()),
            () -> assertEquals("hashed", user.getPassword()),
            () -> assertEquals("2026-11-26", user.getDataCriacao())
        );
    }
}
