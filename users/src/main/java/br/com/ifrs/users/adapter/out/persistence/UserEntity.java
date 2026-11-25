package br.com.ifrs.users.adapter.out.persistence;

import br.com.ifrs.users.domain.model.User;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "User")
@Table(name = "User")
public class UserEntity extends PanacheEntity {

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, unique = true, length = 150)
    public String email;

    @Column(nullable = false, length = 255)
    public String password;

    @Column(nullable = false, length = 50)
    public String dataCriacao;

    public User toDomain() {
        return new User(id, name, email, password, dataCriacao);
    }

    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId();
        entity.name = user.getName();
        entity.email = user.getEmail();
        entity.password = user.getPassword();
        entity.dataCriacao = user.getDataCriacao();
        return entity;
    }
}
