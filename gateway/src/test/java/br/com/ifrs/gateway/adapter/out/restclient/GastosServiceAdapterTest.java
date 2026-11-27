package br.com.ifrs.gateway.adapter.out.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class GastosServiceAdapterTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 26);

    private GastosRestClient restClient;
    private GastosServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        restClient = mock(GastosRestClient.class);
        adapter = new GastosServiceAdapter();
        adapter.restClient = restClient;
    }

    // Verifica o encaminhamento dos dados para criação de despesa.
    @Test
    void createDespesaForwardsInputAndResponse() {
        when(restClient.createDespesa("Bearer token", 12.5, "D", "mercado", DATE))
            .thenReturn(upstream(201, "created"));

        ForwardedResponse result = adapter.createDespesa("Bearer token", 12.5, "D", "mercado", DATE)
            .await().indefinitely();

        assertForwarded(result, 201, "created");
        verify(restClient).createDespesa("Bearer token", 12.5, "D", "mercado", DATE);
    }

    // Verifica o encaminhamento dos filtros de consulta de despesas.
    @Test
    void getDespesasForwardsFiltersAndResponse() {
        when(restClient.getDespesas("Bearer token", "D", "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(upstream(200, "[]"));

        ForwardedResponse result = adapter.getDespesas(
            "Bearer token",
            "D",
            "mercado",
            "2026-11-01",
            "2026-11-30"
        ).await().indefinitely();

        assertForwarded(result, 200, "[]");
        verify(restClient).getDespesas("Bearer token", "D", "mercado", "2026-11-01", "2026-11-30");
    }

    // Verifica o encaminhamento dos campos para atualização de despesa.
    @Test
    void updateDespesaForwardsInputAndResponse() {
        when(restClient.updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE))
            .thenReturn(upstream(200, "updated"));

        ForwardedResponse result = adapter.updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE)
            .await().indefinitely();

        assertForwarded(result, 200, "updated");
        verify(restClient).updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE);
    }

    // Verifica o encaminhamento do identificador para exclusão de despesa.
    @Test
    void deleteDespesaForwardsIdAndResponse() {
        when(restClient.deleteDespesa("Bearer token", 7L)).thenReturn(upstream(204, null));

        ForwardedResponse result = adapter.deleteDespesa("Bearer token", 7L).await().indefinitely();

        assertForwarded(result, 204, null);
        verify(restClient).deleteDespesa("Bearer token", 7L);
    }

    // Verifica o encaminhamento dos filtros para consulta de totais por tag.
    @Test
    void getTagSumsForwardsFiltersAndResponse() {
        when(restClient.getTagSums("Bearer token", "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(upstream(200, "[{\"tag\":\"mercado\",\"total\":12.5}]"));

        ForwardedResponse result = adapter.getTagSums(
            "Bearer token",
            "mercado",
            "2026-11-01",
            "2026-11-30"
        ).await().indefinitely();

        assertForwarded(result, 200, "[{\"tag\":\"mercado\",\"total\":12.5}]");
        verify(restClient).getTagSums("Bearer token", "mercado", "2026-11-01", "2026-11-30");
    }

    // Verifica o encaminhamento do token para consulta do saldo.
    @Test
    void getSaldoForwardsAuthorizationAndResponse() {
        when(restClient.getSaldo("Bearer token")).thenReturn(upstream(200, "12.5"));

        ForwardedResponse result = adapter.getSaldo("Bearer token").await().indefinitely();

        assertForwarded(result, 200, "12.5");
        verify(restClient).getSaldo("Bearer token");
    }

    private static Uni<Response> upstream(int status, String entity) {
        Response.ResponseBuilder response = Response.status(status);
        if (entity != null) {
            response.entity(entity);
        }
        return Uni.createFrom().item(response.build());
    }

    private static void assertForwarded(ForwardedResponse response, int status, String entity) {
        assertEquals(status, response.status());
        assertEquals(entity, response.entity());
    }
}
