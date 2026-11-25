package br.com.ifrs.users.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.ifrs.users.application.port.out.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String password) {
        return encoder.encode(password);
    }

    @Override
    public boolean matches(String password, String hashedPassword) {
        return encoder.matches(password, hashedPassword);
    }
}
