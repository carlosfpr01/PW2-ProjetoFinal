package br.com.gateway.client;

import java.time.LocalDate;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/gastos")
@RegisterRestClient(configKey = "gastos-api")
public interface GastosClient {

    @GET
    @Path("/test-auth")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> testAuth(@HeaderParam("Authorization") String authHeader);

    @POST
    @Path("/despesa/create")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> createDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("amount") Double amount,
        // operation aceita apenas C (Credito) ou D (Debito)
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("date") LocalDate date
    );

    @GET
    @Path("/despesas/list")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getDespesas(
        @HeaderParam("Authorization") String authHeader,
        // operation aceita apenas C (Credito) ou D (Debito)
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("dateStart") String dateStart,
        @QueryParam("dateEnd") String dateEnd
    );

    @PATCH
    @Path("/despesa/update")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> updateDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("id") Long id,
        @QueryParam("amount") Double amount,
        // operation aceita apenas C (Credito) ou D (Debito)
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("date") LocalDate date
    );

    @DELETE
    @Path("/despesa/delete")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> deleteDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("id") Long id
    );

    @GET
    @Path("/por-tags")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getTagSums(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("tag") String tag,
        @QueryParam("dateStart") String dateStart,
        @QueryParam("dateEnd") String dateEnd
    );

    @GET
    @Path("/saldo")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getSaldo(@HeaderParam("Authorization") String authHeader);
}
