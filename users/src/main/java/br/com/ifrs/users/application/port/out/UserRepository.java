package br.com.ifrs.users.application.port.out;

import java.util.Optional;

import br.com.ifrs.users.domain.model.User;
import io.smallrye.mutiny.Uni;

public interface UserRepository {

    Uni<Optional<User>> findByEmail(String email);

    Uni<Optional<User>> findById(Long id);

    Uni<User> save(User user);

    Uni<Boolean> deleteById(Long id);
}
