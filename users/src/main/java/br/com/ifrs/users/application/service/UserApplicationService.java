package br.com.ifrs.users.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import br.com.ifrs.users.application.port.in.UserUseCases;
import br.com.ifrs.users.application.port.out.PasswordHasher;
import br.com.ifrs.users.application.port.out.TokenIssuer;
import br.com.ifrs.users.application.port.out.UserRepository;
import br.com.ifrs.users.domain.model.User;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserApplicationService implements UserUseCases {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Inject
    public UserApplicationService(
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        TokenIssuer tokenIssuer
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @WithTransaction
    public Uni<Optional<String>> login(String email, String password) {
        return userRepository.findByEmail(email)
            .map(user -> user
                .filter(value -> passwordHasher.matches(password, value.getPassword()))
                .map(tokenIssuer::issue));
    }

    @Override
    @WithTransaction
    public Uni<User> create(String name, String email, String password) {
        User user = new User(name, email, passwordHasher.hash(password));
        user.setDataCriacao(LocalDateTime.now().toString());
        return userRepository.save(user);
    }

    @Override
    @WithTransaction
    public Uni<Optional<User>> update(Long id, String name, String email, String password) {
        return userRepository.findById(id)
            .flatMap(user -> {
                if (user.isEmpty()) {
                    return Uni.createFrom().item(Optional.empty());
                }

                User updated = user.get();
                updated.setName(name);
                updated.setEmail(email);
                updated.setPassword(passwordHasher.hash(password));
                return userRepository.save(updated).map(Optional::of);
            });
    }

    @Override
    @WithTransaction
    public Uni<Boolean> delete(Long id) {
        return userRepository.deleteById(id);
    }
}
