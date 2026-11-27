package br.com.ifrs.users.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import br.com.ifrs.users.domain.model.User;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;

class PanacheUserRepositoryTest {

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "ana@example.test";

    private final PanacheUserRepository repository = new PanacheUserRepository();

    // Verifica a busca por e-mail quando o usuário existe.
    @Test
    void findByEmailReturnsMappedUserWhenPresent() {
        User expected = user();
        UserEntity entity = entity();
        PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().item(entity));

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>find("email", EMAIL)).thenReturn(query);

            Optional<User> result = repository.findByEmail(EMAIL).await().indefinitely();

            assertUser(expected, result.orElseThrow());
        }
    }

    // Verifica que a busca por e-mail ausente retorna Optional vazio.
    @Test
    void findByEmailReturnsEmptyWhenUserDoesNotExist() {
        PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        when(query.firstResult()).thenReturn(Uni.createFrom().nullItem());

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>find("email", EMAIL)).thenReturn(query);

            Optional<User> result = repository.findByEmail(EMAIL).await().indefinitely();

            assertEquals(Optional.empty(), result);
        }
    }

    // Verifica a busca por identificador e a conversão para o domínio.
    @Test
    void findByIdReturnsMappedUserWhenPresent() {
        User expected = user();

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().item(entity()));

            Optional<User> result = repository.findById(USER_ID).await().indefinitely();

            assertUser(expected, result.orElseThrow());
        }
    }

    // Verifica que a busca por identificador ausente retorna Optional vazio.
    @Test
    void findByIdReturnsEmptyWhenUserDoesNotExist() {
        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().nullItem());

            Optional<User> result = repository.findById(USER_ID).await().indefinitely();

            assertEquals(Optional.empty(), result);
        }
    }

    // Verifica a persistência de um usuário novo e o retorno convertido ao domínio.
    @Test
    void savePersistsNewUserAndReturnsDomainModel() {
        User input = new User("Ana", EMAIL, "hashed");
        User expected = user();

        try (MockedConstruction<UserEntity> construction = mockConstruction(
            UserEntity.class,
            (entity, context) -> {
                when(entity.persistAndFlush()).thenReturn(Uni.createFrom().item(entity));
                when(entity.toDomain()).thenReturn(expected);
            }
        )) {
            User saved = repository.save(input).await().indefinitely();
            UserEntity persisted = construction.constructed().get(0);

            assertAll(
                () -> assertSame(expected, saved),
                () -> verify(persisted).persistAndFlush(),
                () -> assertEquals("Ana", persisted.name),
                () -> assertEquals(EMAIL, persisted.email),
                () -> assertEquals("hashed", persisted.password)
            );
        }
    }

    // Verifica a atualização dos dados de um usuário existente.
    @Test
    void saveUpdatesExistingUser() {
        User input = new User(USER_ID, "Ana Silva", "ana.silva@example.test", "new-hash", "2026-11-27");
        User expected = new User(USER_ID, input.getName(), input.getEmail(), input.getPassword(), input.getDataCriacao());
        UserEntity entity = mock(UserEntity.class);
        when(entity.persistAndFlush()).thenReturn(Uni.createFrom().item(entity));
        when(entity.toDomain()).thenReturn(expected);

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().item(entity));

            User saved = repository.save(input).await().indefinitely();

            assertAll(
                () -> assertSame(expected, saved),
                () -> assertEquals("Ana Silva", entity.name),
                () -> assertEquals("ana.silva@example.test", entity.email),
                () -> assertEquals("new-hash", entity.password),
                () -> assertEquals("2026-11-27", entity.dataCriacao),
                () -> verify(entity).persistAndFlush()
            );
        }
    }

    // Verifica que salvar uma atualização falha quando o usuário não existe.
    @Test
    void saveFailsWhenExistingUserCannotBeFound() {
        User input = user();

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().nullItem());

            assertThrows(
                IllegalStateException.class,
                () -> repository.save(input).await().indefinitely()
            );
        }
    }

    // Verifica a exclusão e o retorno verdadeiro quando o usuário existe.
    @Test
    void deleteByIdReturnsTrueWhenUserExists() {
        UserEntity entity = mock(UserEntity.class);
        when(entity.delete()).thenReturn(Uni.createFrom().voidItem());

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().item(entity));

            Boolean deleted = repository.deleteById(USER_ID).await().indefinitely();

            assertEquals(true, deleted);
            verify(entity).delete();
        }
    }

    // Verifica que excluir um usuário inexistente retorna falso.
    @Test
    void deleteByIdReturnsFalseWhenUserDoesNotExist() {
        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> UserEntity.<UserEntity>findById(USER_ID))
                .thenReturn(Uni.createFrom().nullItem());

            Boolean deleted = repository.deleteById(USER_ID).await().indefinitely();

            assertEquals(false, deleted);
        }
    }

    private static User user() {
        return new User(USER_ID, "Ana", EMAIL, "hashed", "2026-11-26");
    }

    private static UserEntity entity() {
        UserEntity entity = new UserEntity();
        entity.id = USER_ID;
        entity.name = "Ana";
        entity.email = EMAIL;
        entity.password = "hashed";
        entity.dataCriacao = "2026-11-26";
        return entity;
    }

    private static void assertUser(User expected, User actual) {
        assertAll(
            () -> assertEquals(expected.getId(), actual.getId()),
            () -> assertEquals(expected.getName(), actual.getName()),
            () -> assertEquals(expected.getEmail(), actual.getEmail()),
            () -> assertEquals(expected.getPassword(), actual.getPassword()),
            () -> assertEquals(expected.getDataCriacao(), actual.getDataCriacao())
        );
    }
}
