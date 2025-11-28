package br.com.gateway.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@RegisterRestClient(configKey = "users-api")
public interface UsersClient {

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Response> login(
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    );

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> createUser(
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    );

    @PATCH
    @Path("/updateUser")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> updateUser(
        @HeaderParam("Authorization") String authHeader,
        @HeaderParam("name") String name,
        @HeaderParam("email") String email,
        @HeaderParam("password") String password
    );

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> deleteUser(@HeaderParam("Authorization") String authHeader);
}
