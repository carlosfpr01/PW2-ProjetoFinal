package br.com.ifrs.gateway.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import br.com.ifrs.gateway.application.port.in.GatewayUseCases;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class GatewayResourcesUnitTest {

    private static final String AUTHORIZATION = "Bearer token";
    private static final ForwardedResponse FORWARDED = new ForwardedResponse(
        202,
        "upstream response",
        "text/plain",
        Map.of("X-Request-Id", List.of("request-42"))
    );

    private GatewayUseCases useCases;
    private UsersGatewayResource usersResource;
    private GastosGatewayResource gastosResource;

    @BeforeEach
    void setUp() {
        useCases = mock(GatewayUseCases.class);
        usersResource = new UsersGatewayResource();
        usersResource.gatewayUseCases = useCases;
        gastosResource = new GastosGatewayResource();
        gastosResource.gatewayUseCases = useCases;
    }

    // Verifica o encaminhamento das credenciais e da resposta no endpoint de login.
    @Test
    void loginDelegatesCredentialsAndBuildsHttpResponse() {
        when(useCases.login("ana@example.test", "secret")).thenReturn(forwarded());

        Response response = usersResource.login("ana@example.test", "secret").await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).login("ana@example.test", "secret");
    }

    // Verifica o encaminhamento dos dados e da resposta no endpoint de criação de usuário.
    @Test
    void createUserDelegatesInputAndBuildsHttpResponse() {
        when(useCases.createUser("Ana", "ana@example.test", "secret")).thenReturn(forwarded());

        Response response = usersResource.createUser("Ana", "ana@example.test", "secret")
            .await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).createUser("Ana", "ana@example.test", "secret");
    }

    // Verifica o encaminhamento do token e dos dados no endpoint de atualização de usuário.
    @Test
    void updateUserDelegatesAuthorizationAndInput() {
        when(useCases.updateUser(AUTHORIZATION, "Ana Silva", null, null)).thenReturn(forwarded());

        Response response = usersResource.updateUser(AUTHORIZATION, "Ana Silva", null, null)
            .await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).updateUser(AUTHORIZATION, "Ana Silva", null, null);
    }

    // Verifica o encaminhamento do token no endpoint de exclusão de usuário.
    @Test
    void deleteUserDelegatesAuthorization() {
        when(useCases.deleteUser(AUTHORIZATION)).thenReturn(forwarded());

        Response response = usersResource.deleteUser(AUTHORIZATION).await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).deleteUser(AUTHORIZATION);
    }

    // Verifica o encaminhamento dos dados no endpoint de criação de despesa.
    @Test
    void createDespesaDelegatesInput() {
        LocalDate date = LocalDate.of(2026, 11, 26);
        when(useCases.createDespesa(AUTHORIZATION, 12.5, "D", "mercado", date)).thenReturn(forwarded());

        Response response = gastosResource.createDespesa(AUTHORIZATION, 12.5, "D", "mercado", date)
            .await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).createDespesa(AUTHORIZATION, 12.5, "D", "mercado", date);
    }

    // Verifica o encaminhamento dos filtros no endpoint de listagem de despesas.
    @Test
    void getDespesasDelegatesFilters() {
        when(useCases.getDespesas(AUTHORIZATION, "D", "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(forwarded());

        Response response = gastosResource.getDespesas(
            AUTHORIZATION,
            "D",
            "mercado",
            "2026-11-01",
            "2026-11-30"
        ).await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).getDespesas(AUTHORIZATION, "D", "mercado", "2026-11-01", "2026-11-30");
    }

    // Verifica o encaminhamento dos campos no endpoint de atualização de despesa.
    @Test
    void updateDespesaDelegatesInput() {
        LocalDate date = LocalDate.of(2026, 11, 26);
        when(useCases.updateDespesa(AUTHORIZATION, 7L, 15.0, "C", "salario", date))
            .thenReturn(forwarded());

        Response response = gastosResource.updateDespesa(AUTHORIZATION, 7L, 15.0, "C", "salario", date)
            .await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).updateDespesa(AUTHORIZATION, 7L, 15.0, "C", "salario", date);
    }

    // Verifica o encaminhamento do identificador no endpoint de exclusão de despesa.
    @Test
    void deleteDespesaDelegatesId() {
        when(useCases.deleteDespesa(AUTHORIZATION, 7L)).thenReturn(forwarded());

        Response response = gastosResource.deleteDespesa(AUTHORIZATION, 7L).await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).deleteDespesa(AUTHORIZATION, 7L);
    }

    // Verifica o encaminhamento dos filtros no endpoint de totais por tag.
    @Test
    void getTagSumsDelegatesFilters() {
        when(useCases.getTagSums(AUTHORIZATION, "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(forwarded());

        Response response = gastosResource.getTagSums(
            AUTHORIZATION,
            "mercado",
            "2026-11-01",
            "2026-11-30"
        ).await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).getTagSums(AUTHORIZATION, "mercado", "2026-11-01", "2026-11-30");
    }

    // Verifica o encaminhamento do token no endpoint de consulta do saldo.
    @Test
    void getSaldoDelegatesAuthorization() {
        when(useCases.getSaldo(AUTHORIZATION)).thenReturn(forwarded());

        Response response = gastosResource.getSaldo(AUTHORIZATION).await().indefinitely();

        assertForwardedResponse(response);
        verify(useCases).getSaldo(AUTHORIZATION);
    }

    private static Uni<ForwardedResponse> forwarded() {
        return Uni.createFrom().item(FORWARDED);
    }

    private static void assertForwardedResponse(Response response) {
        assertEquals(202, response.getStatus());
        assertEquals("upstream response", response.getEntity());
        assertEquals("text/plain", response.getMediaType().toString());
        assertEquals("request-42", response.getHeaderString("X-Request-Id"));
        response.close();
    }
}
