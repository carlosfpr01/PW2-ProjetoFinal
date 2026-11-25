package br.com.ifrs.users.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import br.com.ifrs.users.application.port.out.PasswordHasher;
import br.com.ifrs.users.application.port.out.TokenIssuer;
import br.com.ifrs.users.application.port.out.UserRepository;
import br.com.ifrs.users.domain.model.User;
import io.smallrye.mutiny.Uni;

class UserApplicationServiceTest {

    @Test
    void loginIssuesTokenOnlyWhenPasswordMatches() {
        User user = new User(42L, "Ana", "ana@example.test", "stored-hash", "2026-10-12T12:00:00");
        AtomicInteger issuedTokens = new AtomicInteger();
        UserApplicationService service = new UserApplicationService(
            new StubUserRepository(user),
            new PasswordHasher() {
                @Override
                public String hash(String password) {
                    return "stored-hash";
                }

                @Override
                public boolean matches(String password, String hashedPassword) {
                    return "correct".equals(password) && "stored-hash".equals(hashedPassword);
                }
            },
            ignored -> {
                issuedTokens.incrementAndGet();
                return "token";
            }
        );

        assertEquals(Optional.of("token"), service.login("ana@example.test", "correct").await().indefinitely());
        assertEquals(Optional.empty(), service.login("ana@example.test", "wrong").await().indefinitely());
        assertEquals(1, issuedTokens.get());
    }

    private static class StubUserRepository implements UserRepository {

        private final User user;

        StubUserRepository(User user) {
            this.user = user;
        }

        @Override
        public Uni<Optional<User>> findByEmail(String email) {
            return Uni.createFrom().item(
                user.getEmail().equals(email) ? Optional.of(user) : Optional.empty()
            );
        }

        @Override
        public Uni<Optional<User>> findById(Long id) {
            return Uni.createFrom().item(
                user.getId().equals(id) ? Optional.of(user) : Optional.empty()
            );
        }

        @Override
        public Uni<User> save(User user) {
            return Uni.createFrom().item(user);
        }

        @Override
        public Uni<Boolean> deleteById(Long id) {
            return Uni.createFrom().item(user.getId().equals(id));
        }
    }
}
