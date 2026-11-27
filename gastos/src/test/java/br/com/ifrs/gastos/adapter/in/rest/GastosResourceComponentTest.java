package br.com.ifrs.gastos.adapter.in.rest;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class GastosResourceComponentTest {

    // Verifica que operações protegidas rejeitam requisições sem autenticação.
    @Test
    void rejectsAnonymousRequestsToProtectedExpenseOperations() {
        given()
            .when().get("/gastos/saldo")
            .then()
            .statusCode(401);

        given()
            .when().get("/gastos/despesas/list")
            .then()
            .statusCode(401);

        given()
            .queryParam("amount", 10.0)
            .queryParam("operation", "D")
            .queryParam("date", "2026-11-25")
            .when().post("/gastos/despesa/create")
            .then()
            .statusCode(401);
    }

    // Verifica que um token Bearer inválido é rejeitado.
    @Test
    void rejectsInvalidBearerToken() {
        given()
            .header("Authorization", "Bearer invalid-token")
            .when().get("/gastos/saldo")
            .then()
            .statusCode(401);
    }
}
