package br.com.ifrs.users.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
class UsersResourceComponentTest {

    @Test
    void createsAuthenticatesAndDeletesAnIsolatedUser() {
        String email = "qa-" + UUID.randomUUID() + "@example.test";
        String password = "test-password";
        String token = null;

        try {
            given()
                .header("name", "QA User")
                .header("email", email)
                .header("password", password)
                .when().post("/users/create")
                .then()
                .statusCode(200);

            Response login = given()
                .header("email", email)
                .header("password", password)
                .when().post("/users/login")
                .then()
                .statusCode(200)
                .extract().response();
            token = login.asString();

            assertNotNull(token);
            assertFalse(token.isBlank());

            given()
                .header("email", email)
                .header("password", "incorrect-password")
                .when().post("/users/login")
                .then()
                .statusCode(401);
        } finally {
            if (token != null && !token.isBlank()) {
                given()
                    .header("Authorization", "Bearer " + token)
                    .when().post("/users/delete")
                    .then()
                    .statusCode(204);
            }
        }
    }

    @Test
    void rejectsProtectedDeleteWithoutAuthentication() {
        given()
            .when().post("/users/delete")
            .then()
            .statusCode(401);
    }
}
