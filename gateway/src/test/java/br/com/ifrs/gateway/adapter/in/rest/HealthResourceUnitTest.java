package br.com.ifrs.gateway.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HealthResourceUnitTest {

    @Test
    void returnsGatewayHealthPayload() {
        assertEquals(
            "{\"status\":\"UP\",\"service\":\"API Gateway\"}",
            new HealthResource().health()
        );
    }
}
