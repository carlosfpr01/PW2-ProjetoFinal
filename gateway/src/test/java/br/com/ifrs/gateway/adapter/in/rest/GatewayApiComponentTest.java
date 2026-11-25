package br.com.ifrs.gateway.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class GatewayApiComponentTest {

    @Test
    void healthEndpointReportsGatewayUp() {
        given()
            .when().get("/api/health")
            .then()
            .statusCode(200)
            .body(containsString("\"status\":\"UP\""))
            .body(containsString("\"service\":\"API Gateway\""));
    }

    @Test
    void protectedExpenseEndpointRejectsAnonymousRequest() {
        given()
            .when().get("/api/gastos/saldo")
            .then()
            .statusCode(is(401));
    }
}
