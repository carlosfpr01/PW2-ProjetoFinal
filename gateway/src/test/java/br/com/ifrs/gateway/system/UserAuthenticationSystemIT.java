package br.com.ifrs.gateway.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

class UserAuthenticationSystemIT extends SystemApiTestSupport {

    // Verifica credenciais inválidas e válidas, além do acesso autenticado ao saldo.
    @Test
    void rejectsWrongPasswordAndAcceptsValidCredentials() {
        APIResponse invalidLogin = request().post(
            "/api/users/login",
            RequestOptions.create()
                .setHeader("email", email)
                .setHeader("password", "incorrect-password")
        );
        assertEquals(401, invalidLogin.status(), invalidLogin.text());

        APIResponse validLogin = request().post(
            "/api/users/login",
            RequestOptions.create()
                .setHeader("email", email)
                .setHeader("password", password)
        );
        assertEquals(200, validLogin.status(), validLogin.text());
        assertFalse(validLogin.text().isBlank());

        APIResponse anonymousBalance = request().get("/api/gastos/saldo");
        assertEquals(401, anonymousBalance.status(), anonymousBalance.text());

        APIResponse authenticatedBalance = request().get("/api/gastos/saldo", authorizedRequest());
        assertEquals(200, authenticatedBalance.status(), authenticatedBalance.text());
        assertEquals(0.0, Double.parseDouble(authenticatedBalance.text()), 0.0001);
    }
}
