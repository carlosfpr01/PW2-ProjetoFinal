package br.com.ifrs.users.adapter.out.persistence;

import java.util.Optional;

import br.com.ifrs.users.application.port.out.UserRepository;
import br.com.ifrs.users.domain.model.User;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PanacheUserRepository implements UserRepository {

    @Override
    public Uni<Optional<User>> findByEmail(String email) {
        return UserEntity.<UserEntity>find("email", email).firstResult()
            .map(entity -> Optional.ofNullable(entity).map(UserEntity::toDomain));
    }

    @Override
    public Uni<Optional<User>> findById(Long id) {
        return UserEntity.<UserEntity>findById(id)
            .map(entity -> Optional.ofNullable(entity).map(UserEntity::toDomain));
    }

    @Override
    public Uni<User> save(User user) {
        if (user.getId() == null) {
            UserEntity entity = UserEntity.fromDomain(user);
            return entity.persistAndFlush().map(ignored -> entity.toDomain());
        }

        return UserEntity.<UserEntity>findById(user.getId())
            .onItem().ifNull().failWith(() -> new IllegalStateException("User not found: " + user.getId()))
            .invoke(entity -> {
                entity.name = user.getName();
                entity.email = user.getEmail();
                entity.password = user.getPassword();
                entity.dataCriacao = user.getDataCriacao();
            })
            .call(UserEntity::persistAndFlush)
            .map(UserEntity::toDomain);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return UserEntity.<UserEntity>findById(id)
            .onItem().ifNotNull().transformToUni(entity -> entity.delete().replaceWith(true))
            .onItem().ifNull().continueWith(false);
    }
}
