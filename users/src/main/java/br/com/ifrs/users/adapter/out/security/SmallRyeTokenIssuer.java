package br.com.ifrs.users.adapter.out.security;

import org.eclipse.microprofile.jwt.Claims;

import br.com.ifrs.users.application.port.out.TokenIssuer;
import br.com.ifrs.users.domain.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SmallRyeTokenIssuer implements TokenIssuer {

    private static final String ISSUER = "users-issuer";

    @Override
    public String issue(User user) {
        return Jwt.issuer(ISSUER)
            .upn(user.getEmail())
            .groups("user")
            .claim(Claims.nickname, user.getName())
            .claim("id", user.getId())
            .claim(Claims.email, user.getEmail())
            .expiresIn(3600)
            .sign();
    }
}
