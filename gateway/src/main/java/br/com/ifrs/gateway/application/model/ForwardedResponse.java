package br.com.ifrs.gateway.application.model;

import java.util.List;
import java.util.Map;

public record ForwardedResponse(
    int status,
    String entity,
    String mediaType,
    Map<String, List<String>> headers
) {

    public ForwardedResponse {
        headers = Map.copyOf(headers);
    }
}
