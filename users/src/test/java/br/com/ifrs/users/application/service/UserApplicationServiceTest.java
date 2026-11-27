package br.com.ifrs.users.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ifrs.users.application.port.out.PasswordHasher;
import br.com.ifrs.users.application.port.out.TokenIssuer;
import br.com.ifrs.users.application.port.out.UserRepository;
import br.com.ifrs.users.domain.model.User;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordHasher passwordHasher;

    @Mock
    TokenIssuer tokenIssuer;

    @InjectMocks
    UserApplicationService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(42L, "Ana", "ana@example.test", "stored-hash", "2026-10-12T12:00:00");
    }

    @Test
    void loginIssuesTokenWhenPasswordMatches() {
        when(userRepository.findByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(Optional.of(user)));
        when(passwordHasher.matches("correct", user.getPassword())).thenReturn(true);
        when(tokenIssuer.issue(user)).thenReturn("token");

        assertEquals(Optional.of("token"), service.login(user.getEmail(), "correct").await().indefinitely());

        verify(tokenIssuer).issue(user);
    }

    @Test
    void loginReturnsEmptyWhenPasswordDoesNotMatchOrUserIsMissing() {
        when(userRepository.findByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(Optional.of(user)));
        when(userRepository.findByEmail("missing@example.test"))
            .thenReturn(Uni.createFrom().item(Optional.empty()));
        when(passwordHasher.matches("wrong", user.getPassword())).thenReturn(false);

        assertEquals(Optional.empty(), service.login(user.getEmail(), "wrong").await().indefinitely());
        assertEquals(Optional.empty(), service.login("missing@example.test", "password").await().indefinitely());

        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void createHashesPasswordAndSavesCreationDate() {
        when(passwordHasher.hash("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
            Uni.createFrom().item(invocation.getArgument(0, User.class)));

        User created = service.create("Ana", "ana@example.test", "plain-password").await().indefinitely();

        assertEquals("hashed-password", created.getPassword());
        assertNotNull(created.getDataCriacao());
        assertFalse(created.getDataCriacao().isBlank());
        LocalDateTime.parse(created.getDataCriacao());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateChangesUserAndHashesNewPassword() {
        when(userRepository.findById(42L)).thenReturn(Uni.createFrom().item(Optional.of(user)));
        when(passwordHasher.hash("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(Uni.createFrom().item(user));

        Optional<User> updated = service.update(42L, "Ana Silva", "ana.silva@example.test", "new-password")
            .await().indefinitely();

        assertTrue(updated.isPresent());
        assertEquals("Ana Silva", user.getName());
        assertEquals("ana.silva@example.test", user.getEmail());
        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updateReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(404L)).thenReturn(Uni.createFrom().item(Optional.empty()));

        assertEquals(Optional.empty(), service.update(404L, "Ana", "ana@example.test", "password")
            .await().indefinitely());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void deleteReturnsRepositoryResult() {
        when(userRepository.deleteById(42L)).thenReturn(Uni.createFrom().item(true));
        when(userRepository.deleteById(404L)).thenReturn(Uni.createFrom().item(false));

        assertTrue(service.delete(42L).await().indefinitely());
        assertFalse(service.delete(404L).await().indefinitely());
    }
}
