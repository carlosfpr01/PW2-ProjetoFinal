package br.com.ifrs.users.adapter.in.rest;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.ifrs.users.application.port.in.UserUseCases;
import br.com.ifrs.users.domain.model.User;
import io.quarkus.security.identity.SecurityIdentity;
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

    @Inject
    UserUseCases userUseCases;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> getToken(
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return userUseCases.login(email, password)
            .map(token -> token
                .map(value -> Response.ok(value).build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Credenciais inválidas")
                    .build()));
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> createUser(
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return userUseCases.create(name, email, password);
    }

    @PATCH
    @Path("/updateUser")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
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

        return userUseCases.update(tokenId, name, email, password)
            .map(user -> user
                .map(updated -> Response.ok(updated).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuário não encontrado")
                    .build()));
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
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

        return userUseCases.delete(tokenId)
            .map(deleted -> deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND)
                    .entity("Usuário não encontrado")
                    .build());
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
