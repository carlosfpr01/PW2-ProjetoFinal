package run.gastos;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import run.gastos.model.Despesa;
import run.gastos.model.TagSum;

@Path("/gastos")
public class GastosResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/test-auth")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    public Uni<Response> testAuth() {
        String userId = jwt != null ? jwt.getClaim("id").toString() : "null";
        String upn = jwt != null ? jwt.getName() : "null";
        String groups = jwt != null ? jwt.getGroups().toString() : "null";
        String result = String.format("{\"userId\":\"%s\",\"upn\":\"%s\",\"groups\":\"%s\",\"authenticated\":%b}", 
            userId, upn, groups, !securityIdentity.isAnonymous());
        return Uni.createFrom().item(Response.ok(result).build());
    }

    @POST
    @Path("/despesa/create")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Despesa> createDespesa(
        @QueryParam("amount") Double amount,
        @QueryParam("operation") String operation,
        @QueryParam("tag") String tag,
        @QueryParam("date") LocalDate date
        ) {
        // Validação do token JWT se o token está nulo
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token Vazio", Response.Status.UNAUTHORIZED));
        }

        //Validar se o campo id existe no token
        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.UNAUTHORIZED));
        }

        // Criação do id Long baseado no token JWT (forma simples e resiliente)
        Long tokenId;
        try {
            tokenId = Long.parseLong(idClaim.toString());
        } catch (Exception e) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.BAD_REQUEST));
        }

        // Garantir que amount seja Double para compatibilidade com Despesa
        if (amount == null) {
            return Uni.createFrom().failure(new WebApplicationException("Campo 'amount' é obrigatório", Response.Status.BAD_REQUEST));
        }

        if (operation == null || operation.trim().isEmpty()) {
            return Uni.createFrom().failure(new WebApplicationException("Campo 'operation' é obrigatório", Response.Status.BAD_REQUEST));
        }

        Despesa.operations op;
        try {
            op = Despesa.operations.valueOf(operation.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException("Campo 'operation' inválido (use 'D' ou 'C')", Response.Status.BAD_REQUEST));
        }

        if (date == null) {
            return Uni.createFrom().failure(new WebApplicationException("Campo 'date' é obrigatório", Response.Status.BAD_REQUEST));
        }
        Despesa despesa = new Despesa();
        despesa.setIdUser(tokenId);
        despesa.setAmount(amount);
        despesa.setOperation(op);
        despesa.setDate(date);
        despesa.setTag(tag == null || tag.trim().isEmpty() ? "Outros" : tag);
        return despesa.persistAndFlush();
    }

    @GET
    @Path("/despesas/list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<List<Despesa>> getDespesas(
            @QueryParam("operation") String operation,
            @QueryParam("tag") String tag,
            @QueryParam("dateStart") String dateStart,
            @QueryParam("dateEnd") String dateEnd) {
         // Validação do token JWT se o token está nulo
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token Vazio", Response.Status.UNAUTHORIZED));
        }

        //Validar se o campo id existe no token
        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.UNAUTHORIZED));
        }
        // Criação do id Long baseado no token JWT (forma simples e resiliente)
        Long tokenId;
        try {
            tokenId = Long.parseLong(idClaim.toString());
        } catch (Exception e) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.BAD_REQUEST));
        }

        dateStart = (dateStart == null || dateStart.trim().isEmpty()) ? null : dateStart;
        dateEnd = (dateEnd == null || dateEnd.trim().isEmpty()) ? null : dateEnd;

        return Despesa.getDespesasByFilters(tokenId, operation, tag, dateStart, dateEnd);
    }

    @PATCH
    @Path("/despesa/update")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Despesa> updateDespesa(
            @QueryParam("id") Long id,
            @QueryParam("amount") Double amount,
            @QueryParam("operation") String operation,
            @QueryParam("tag") String tag,
            @QueryParam("date") LocalDate date) {
        return Despesa.<Despesa>findById(id)
            .onItem().ifNotNull()
            .invoke(item -> {
                if (amount != null) {
                    item.setAmount(amount);
                }
                if (operation != null && !operation.trim().isEmpty()) {
                    try {
                        Despesa.operations op = Despesa.operations.valueOf(operation.trim().toUpperCase());
                        item.setOperation(op);
                    } catch (IllegalArgumentException e) {
                        throw new WebApplicationException("Campo 'operation' inválido (use 'D' ou 'C')", Response.Status.BAD_REQUEST);
                    }
                }
                if (tag != null && !tag.trim().isEmpty()) {
                    item.setTag(tag);
                }
                if (date != null) {
                    item.setDate(date);
                }
            })
            .call(item -> item.persistAndFlush());

    }

    @DELETE
    @Path("/despesa/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Despesa> deleteDespesa(@QueryParam("id") Long id) {
        return Despesa.<Despesa>findById(id)
            .onItem().ifNotNull()
            .call(item -> {
                return item.delete();
            });
    }

    @GET
    @Path("/por-tags")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<List<TagSum>> getDebitByTags(
        @QueryParam("tag") String tag,
        @QueryParam("dateStart") String dtStart,
        @QueryParam("dateEnd") String dtEnd
    ) {
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token Vazio", Response.Status.UNAUTHORIZED));
        }

        //Validar se o campo id existe no token
        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.UNAUTHORIZED));
        }

        // Criação do id Long baseado no token JWT (forma simples e resiliente)
        Long tokenId;
        try {
            tokenId = Long.parseLong(idClaim.toString());
        } catch (Exception e) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.BAD_REQUEST));
        }

        return TagSum.getTagsByUserId(tokenId, dtStart,  dtEnd);

    }

    @GET
    @Path("/saldo")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    @WithTransaction
    public Uni<Double> getSaldo() {
        if (securityIdentity == null || securityIdentity.isAnonymous() || jwt == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token Vazio", Response.Status.UNAUTHORIZED));
        }

        //Validar se o campo id existe no token
        Object idClaim = jwt.getClaim("id");
        if (idClaim == null) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.UNAUTHORIZED));
        }

        // Criação do id Long baseado no token JWT (forma simples e resiliente)
        Long tokenId;
        try {
            tokenId = Long.parseLong(idClaim.toString());
        } catch (Exception e) {
            return Uni.createFrom().failure(new WebApplicationException("Token inválido", Response.Status.BAD_REQUEST));
        }

        return Despesa.calculateSaldoByUserId(tokenId);
    }

    public static class UpdateDespesaRequest {
        public Double amount;
        public String operation;
        public String tag;
        public LocalDate date;
    }

    public static class CreateDespesaRequest {
        public Double amount;
        public String operation;
        public String tag;
        public LocalDate date;
    }
    public static class DeleteDespesaRequest {
        public Long id;
    }

}
