package dev.ifrs;


import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dev.ifrs.model.User;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
public class UsersResource {

    private static final String ISSUER = "users-issuer";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    private String hashPassword(String password) {
        return encoder.encode(password);
    }

    private boolean verifyPassword(String password, String hash) {
        return encoder.matches(password, hash);
    }


    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @WithTransaction
    public Uni<Response> getToken(
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return User.<User>find("email", email).firstResult()
            .onItem().transform(user -> {
                if (user != null && verifyPassword(password, user.getPassword())) {
                    String token = Jwt.issuer(ISSUER)
                        .upn(user.getEmail())
                        .groups("user")
                        .claim(Claims.nickname, user.getName())
                        .claim("id", user.id)
                        .claim(Claims.email, user.getEmail())
                        .expiresIn(3600)  // Token expira em 1 hora (3600 segundos)
                        .sign();
                    return Response.ok(token).build();
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciais inválidas").build();
                }
            });
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @WithTransaction
    public Uni<User> createUser(
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        Log.info("Creating user: " + name + " with email: " + email);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashPassword(password));
        user.setDataCriacao(java.time.LocalDateTime.now().toString());
        return user.persistAndFlush().map(v -> user);
    }

    @PATCH
    @Path("/updateUser")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Response> updateUser(
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).entity("Token necessário").build());
        }

        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).entity("Erro no Token").build());
        }

        Long tokenId;
        if (idClaim instanceof Number) {
            tokenId = ((Number) idClaim).longValue();
        } else {
            try {
                tokenId = Long.parseLong(idClaim.toString());
            } catch (NumberFormatException e) {
                return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Claim id inválido").build());
            }
        }

        return User.<User>findById(tokenId)
            .onItem().ifNotNull().transformToUni(user -> {
                user.setName(name);
                user.setEmail(email);
                user.setPassword(hashPassword(password));
                return user.persistAndFlush()
                    .onItem().transform(updated -> Response.ok(updated).build());
            })
            .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Response> deleteUser() {
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).entity("Token necessário").build());
        }

        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).entity("Erro no Token").build());
        }

        Long tokenId;
        if (idClaim instanceof Number) {
            tokenId = ((Number) idClaim).longValue();
        } else {
            try {
                tokenId = Long.parseLong(idClaim.toString());
            } catch (NumberFormatException e) {
                return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Claim id inválido").build());
            }
        }

        return User.<User>findById(tokenId)
            .onItem().ifNotNull().transformToUni(user ->
                user.delete()
                    .replaceWith(Response.noContent().build())
            )
            .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build());
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class CreateUserRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class UpdateUserRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class DeleteUserRequest {
        public Long id;
    }


}
