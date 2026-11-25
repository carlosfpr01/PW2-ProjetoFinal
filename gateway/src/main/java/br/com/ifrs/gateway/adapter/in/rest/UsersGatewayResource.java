package br.com.ifrs.gateway.adapter.in.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.com.ifrs.gateway.application.port.in.GatewayUseCases;
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


@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gerenciamento de usuários e autenticação")
public class UsersGatewayResource {

    @Inject
    GatewayUseCases gatewayUseCases;

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @APIResponse(responseCode = "200", description = "Login bem-sucedido, retorna JWT token")
    @APIResponse(responseCode = "401", description = "Credenciais inválidas")
    public Uni<Response> login(
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return ForwardedHttpResponse.build(gatewayUseCases.login(email, password));
    }

    @POST
    @Path("/create")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @APIResponse(responseCode = "200", description = "Usuário criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Uni<Response> createUser(
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return ForwardedHttpResponse.build(gatewayUseCases.createUser(name, email, password));
    }

    @PATCH
    @Path("/updateUser")
    @RolesAllowed({"user"})
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados do usuário autenticado")
    @APIResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Uni<Response> updateUser(
        @HeaderParam("Authorization") String authHeader,
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    ) {
        return ForwardedHttpResponse.build(
            gatewayUseCases.updateUser(authHeader, name, email, password)
        );
    }

    @POST
    @Path("/delete")
    @RolesAllowed({"user"})
    public Uni<Response> deleteUser(
        @HeaderParam("Authorization") String authHeader
    ) {
        return ForwardedHttpResponse.build(gatewayUseCases.deleteUser(authHeader));
    }
}
