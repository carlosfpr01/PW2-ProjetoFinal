package br.com.ifrs.users.application.port.in;

import java.util.Optional;

import br.com.ifrs.users.domain.model.User;
import io.smallrye.mutiny.Uni;

public interface UserUseCases {

    Uni<Optional<String>> login(String email, String password);

    Uni<User> create(String name, String email, String password);

    Uni<Optional<User>> update(Long id, String name, String email, String password);

    Uni<Boolean> delete(Long id);
}
