package br.com.gateway;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.gateway.client.GastosClient;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/gastos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Gastos", description = "Gerenciamento de despesas e receitas")
public class GastosGatewayResource {

    @Inject
    @RestClient
    GastosClient gastosClient;

    @POST
    @Path("/despesa/create")
    @RolesAllowed({"user"})
    @Operation(summary = "Criar despesa", description = "Cria uma nova despesa")
    @APIResponse(responseCode = "200", description = "Despesa criada com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Uni<Response> createDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("amount") Double amount,
        @Parameter(description = "Tipo de operacao. Valores permitidos: C (Credito) ou D (Debito)", schema = @Schema(enumeration = {"C", "D"}))
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("date") LocalDate date) {
        return gastosClient.createDespesa(authHeader, amount, operation, tag, date);
    }

    @GET
    @Path("/despesas/list")
    @RolesAllowed({"user"})
    public Uni<Response> getDespesas(
        @HeaderParam("Authorization") String authHeader,
        @Parameter(description = "Tipo de operacao. Valores permitidos: C (Credito) ou D (Debito)", schema = @Schema(enumeration = {"C", "D"}))
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("dateStart") String dateStart,
        @QueryParam("dateEnd") String dateEnd
    ) {
        return gastosClient.getDespesas(authHeader, operation, tag, dateStart, dateEnd);
    }

    @PATCH
    @Path("/despesa/update")
    @RolesAllowed({"user"})
    public Uni<Response> updateDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("id") Long id,
        @QueryParam("amount") Double amount,
        @Parameter(description = "Tipo de operacao. Valores permitidos: C (Credito) ou D (Debito)", schema = @Schema(enumeration = {"C", "D"}))
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("date") LocalDate date
    ) {
        return gastosClient.updateDespesa(authHeader, id, amount, operation, tag, date);
    }

    @DELETE
    @Path("/despesa/delete")
    @RolesAllowed({"user"})
    public Uni<Response> deleteDespesa(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("id") Long id
    ) {
        return gastosClient.deleteDespesa(authHeader, id);
    }

    @GET
    @Path("/por-tags")
    @RolesAllowed({"user"})
    public Uni<Response> getTagSums(
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("tag") String tag,
        @QueryParam("dateStart") String dateStart,
        @QueryParam("dateEnd") String dateEnd
    ) {
        return gastosClient.getTagSums(authHeader, tag, dateStart, dateEnd);
    }

    @GET
    @Path("/saldo")
    @RolesAllowed({"user"})
    public Uni<Response> getSaldo(@HeaderParam("Authorization") String authHeader) {
        return gastosClient.getSaldo(authHeader);
    }
}
