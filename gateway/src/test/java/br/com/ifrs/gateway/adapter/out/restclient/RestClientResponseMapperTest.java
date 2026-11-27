package br.com.ifrs.gateway.adapter.out.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class RestClientResponseMapperTest {

    // Verifica o mapeamento do status, corpo, tipo de mídia e cabeçalhos.
    @Test
    void mapsResponseStatusEntityMediaTypeAndHeaders() {
        Response response = Response.status(Response.Status.CREATED)
            .type("application/json")
            .header("X-Request-Id", "request-123")
            .entity("{\"id\":7}")
            .build();

        ForwardedResponse mapped = RestClientResponseMapper.map(response);

        assertEquals(201, mapped.status());
        assertEquals("{\"id\":7}", mapped.entity());
        assertEquals("application/json", mapped.mediaType());
        assertEquals(List.of("request-123"), mapped.headers().get("X-Request-Id"));
    }

    // Verifica o mapeamento de resposta sem corpo nem tipo de mídia.
    @Test
    void mapsResponsesWithoutEntityOrMediaType() {
        Response response = Response.noContent().build();

        ForwardedResponse mapped = RestClientResponseMapper.map(response);

        assertEquals(204, mapped.status());
        assertNull(mapped.entity());
        assertNull(mapped.mediaType());
    }

    // Verifica o mapeamento de uma resposta reativa.
    @Test
    void mapsReactiveResponse() {
        Response response = Response.status(Response.Status.ACCEPTED).entity("queued").build();

        ForwardedResponse mapped = RestClientResponseMapper.map(Uni.createFrom().item(response))
            .await().indefinitely();

        assertEquals(202, mapped.status());
        assertEquals("queued", mapped.entity());
    }

    // Verifica o mapeamento de erro do cliente e a resposta padrão sem conteúdo.
    @Test
    void mapsClientErrorAndFallbackForMissingResponse() {
        Response error = Response.status(Response.Status.BAD_GATEWAY).entity("upstream unavailable").build();

        ForwardedResponse mappedError = RestClientResponseMapper.fromClientError(error);
        ForwardedResponse missing = RestClientResponseMapper.fromClientError(null);

        assertEquals(502, mappedError.status());
        assertEquals("upstream unavailable", mappedError.entity());
        assertEquals(500, missing.status());
        assertNull(missing.entity());
        assertNull(missing.mediaType());
        assertEquals(Map.of(), missing.headers());
    }
}
