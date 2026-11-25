package br.com.ifrs.gateway.adapter.out.restclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

final class RestClientResponseMapper {

    private RestClientResponseMapper() {
    }

    static Uni<ForwardedResponse> map(Uni<Response> response) {
        return response.onItem().transform(RestClientResponseMapper::map);
    }

    static ForwardedResponse map(Response response) {
        try {
            Map<String, List<String>> headers = response.getStringHeaders().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new ArrayList<>(entry.getValue())
                ));
            String mediaType = response.getMediaType() == null
                ? null
                : response.getMediaType().toString();
            String entity = response.hasEntity() ? response.readEntity(String.class) : null;
            return new ForwardedResponse(response.getStatus(), entity, mediaType, headers);
        } finally {
            response.close();
        }
    }

    static ForwardedResponse fromClientError(Response response) {
        if (response == null) {
            return new ForwardedResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                null,
                null,
                Map.of()
            );
        }

        try {
            int status = response.getStatus();
            String entity = null;
            if (response.hasEntity()) {
                try {
                    entity = response.readEntity(String.class);
                } catch (IllegalStateException exception) {
                    Object originalEntity = response.getEntity();
                    entity = originalEntity == null ? null : originalEntity.toString();
                }
            }
            return new ForwardedResponse(status, entity, null, Map.of());
        } finally {
            response.close();
        }
    }
}
