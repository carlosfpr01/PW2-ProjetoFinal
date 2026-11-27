package br.com.ifrs.gateway.adapter.out.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class UsersServiceAdapterTest {

    private UsersRestClient restClient;
    private UsersServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        restClient = mock(UsersRestClient.class);
        adapter = new UsersServiceAdapter();
        adapter.restClient = restClient;
    }

    // Verifica o encaminhamento do login e da resposta do serviço de usuários.
    @Test
    void loginForwardsCredentialsAndResponse() {
        when(restClient.login("ana@example.test", "secret")).thenReturn(upstream(200, "token"));

        ForwardedResponse result = adapter.login("ana@example.test", "secret").await().indefinitely();

        assertForwarded(result, 200, "token");
        verify(restClient).login("ana@example.test", "secret");
    }

    // Verifica o encaminhamento dos dados para criação de usuário.
    @Test
    void createUserForwardsInputAndResponse() {
        when(restClient.createUser("Ana", "ana@example.test", "secret"))
            .thenReturn(upstream(201, "created"));

        ForwardedResponse result = adapter.createUser("Ana", "ana@example.test", "secret")
            .await().indefinitely();

        assertForwarded(result, 201, "created");
        verify(restClient).createUser("Ana", "ana@example.test", "secret");
    }

    // Verifica o encaminhamento do token e dos campos para atualização do usuário.
    @Test
    void updateUserForwardsAuthorizationAndInput() {
        when(restClient.updateUser("Bearer token", "Ana Silva", null, null))
            .thenReturn(upstream(200, "updated"));

        ForwardedResponse result = adapter.updateUser("Bearer token", "Ana Silva", null, null)
            .await().indefinitely();

        assertForwarded(result, 200, "updated");
        verify(restClient).updateUser("Bearer token", "Ana Silva", null, null);
    }

    // Verifica o encaminhamento do token para exclusão do usuário.
    @Test
    void deleteUserForwardsAuthorization() {
        when(restClient.deleteUser("Bearer token")).thenReturn(upstream(204, null));

        ForwardedResponse result = adapter.deleteUser("Bearer token").await().indefinitely();

        assertForwarded(result, 204, null);
        verify(restClient).deleteUser("Bearer token");
    }

    // Verifica que respostas de erro HTTP do cliente são convertidas em respostas encaminhadas.
    @Test
    void recoversClientErrorResponse() {
        Response error = Response.status(401).entity("invalid credentials").build();
        ClientWebApplicationException exception = new ClientWebApplicationException(error);
        when(restClient.login("ana@example.test", "wrong"))
            .thenReturn(Uni.createFrom().failure(exception));

        ForwardedResponse result = adapter.login("ana@example.test", "wrong").await().indefinitely();

        assertForwarded(result, 401, "invalid credentials");
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
