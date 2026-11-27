package br.com.ifrs.gateway.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class ForwardedHttpResponseTest {

    // Verifica o encaminhamento de status, corpo, tipo de mídia e cabeçalhos.
    @Test
    void buildsResponseWithStatusEntityMediaTypeAndForwardedHeaders() {
        ForwardedResponse forwarded = new ForwardedResponse(
            201,
            "{\"id\":7}",
            "application/json",
            Map.of(
                "X-Request-Id", List.of("request-123"),
                "Content-Type", List.of("application/json")
            )
        );

        Response response = ForwardedHttpResponse.build(Uni.createFrom().item(forwarded)).await().indefinitely();

        assertEquals(201, response.getStatus());
        assertEquals("{\"id\":7}", response.getEntity());
        assertEquals("application/json", response.getMediaType().toString());
        assertEquals("request-123", response.getHeaderString("X-Request-Id"));
        response.close();
    }

    // Verifica que corpo e tipo de mídia ausentes não são incluídos na resposta.
    @Test
    void omitsAbsentEntityAndMediaType() {
        Response response = ForwardedHttpResponse.build(
            Uni.createFrom().item(new ForwardedResponse(204, null, null, Map.of()))
        ).await().indefinitely();

        assertEquals(204, response.getStatus());
        assertNull(response.getEntity());
        assertNull(response.getMediaType());
        response.close();
    }
}
