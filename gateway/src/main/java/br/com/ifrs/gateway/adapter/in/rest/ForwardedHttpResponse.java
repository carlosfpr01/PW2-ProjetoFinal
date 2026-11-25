package br.com.ifrs.gateway.adapter.in.rest;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

final class ForwardedHttpResponse {

    private ForwardedHttpResponse() {
    }

    static Uni<Response> build(Uni<ForwardedResponse> result) {
        return result.map(forwarded -> {
            Response.ResponseBuilder response = Response.status(forwarded.status());
            if (forwarded.entity() != null) {
                response.entity(forwarded.entity());
            }
            if (forwarded.mediaType() != null) {
                response.type(forwarded.mediaType());
            }
            forwarded.headers().forEach((name, values) -> {
                if (!"content-type".equalsIgnoreCase(name)) {
                    values.forEach(value -> response.header(name, value));
                }
            });
            return response.build();
        });
    }
}
