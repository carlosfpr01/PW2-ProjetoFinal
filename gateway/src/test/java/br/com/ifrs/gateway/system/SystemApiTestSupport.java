package br.com.ifrs.gateway.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;

abstract class SystemApiTestSupport {

    private static final ObjectMapper JSON = new ObjectMapper();

    protected Playwright playwright;
    protected APIRequestContext request;
    protected String email;
    protected String password;
    protected String token;

    private boolean userCreated;
    private final List<Long> expenseIds = new ArrayList<>();

    @BeforeEach
    void startSystemContextAndCreateUser() {
        String baseUrl = System.getenv().getOrDefault("SYSTEM_TEST_BASE_URL", "http://localhost:8080");
        playwright = Playwright.create();
        request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(baseUrl));

        email = "qa-" + UUID.randomUUID() + "@example.test";
        password = "system-test-password";

        APIResponse createdUser = request.post(
            "/api/users/create",
            RequestOptions.create()
                .setHeader("name", "System Test User")
                .setHeader("email", email)
                .setHeader("password", password)
        );
        assertEquals(200, createdUser.status(), createdUser.text());
        userCreated = true;

        token = login(password);
    }

    @AfterEach
    void cleanSystemTestDataAndClosePlaywright() {
        try {
            if (userCreated) {
                if (token == null || token.isBlank()) {
                    token = login(password);
                }
                if (token != null && !token.isBlank()) {
                    for (Long expenseId : List.copyOf(expenseIds)) {
                        APIResponse deletedExpense = request.delete(
                            "/api/gastos/despesa/delete?id=" + expenseId,
                            authorizedRequest()
                        );
                        assertTrue(
                            deletedExpense.status() == 200
                                || deletedExpense.status() == 204
                                || deletedExpense.status() == 404,
                            deletedExpense.text()
                        );
                    }

                    APIResponse deletedUser = request.post("/api/users/delete", authorizedRequest());
                    assertEquals(204, deletedUser.status(), deletedUser.text());
                }
            }
        } finally {
            if (request != null) {
                request.dispose();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    protected String login(String loginPassword) {
        APIResponse response = request.post(
            "/api/users/login",
            RequestOptions.create()
                .setHeader("email", email)
                .setHeader("password", loginPassword)
        );
        assertEquals(200, response.status(), response.text());
        String value = response.text();
        assertFalse(value.isBlank());
        return value;
    }

    protected long createExpense(double amount, String operation, String tag, LocalDate date) throws Exception {
        APIResponse response = request.post(
            "/api/gastos/despesa/create?amount=" + amount
                + "&operation=" + operation
                + "&tag=" + tag
                + "&date=" + date,
            authorizedRequest()
        );
        assertEquals(200, response.status(), response.text());

        long id = JSON.readTree(response.text()).path("id").asLong();
        assertTrue(id > 0, response.text());
        expenseIds.add(id);
        return id;
    }

    protected APIResponse listExpenses(String operation, String tag) {
        return request.get(
            "/api/gastos/despesas/list?operation=" + operation + "&tag=" + tag,
            authorizedRequest()
        );
    }

    protected APIRequestContext request() {
        return request;
    }

    protected RequestOptions authorizedRequest() {
        return RequestOptions.create().setHeader("Authorization", "Bearer " + token);
    }

    protected JsonNode json(String body) throws Exception {
        return JSON.readTree(body);
    }

    protected void forgetExpense(long id) {
        expenseIds.remove(id);
    }
}
