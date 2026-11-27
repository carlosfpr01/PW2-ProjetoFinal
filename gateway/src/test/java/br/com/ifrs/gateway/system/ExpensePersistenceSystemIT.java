package br.com.ifrs.gateway.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;

class ExpensePersistenceSystemIT extends SystemApiTestSupport {

    // Verifica a criação autenticada e a leitura dos dados persistidos da despesa.
    @Test
    void createsExpenseAndReadsPersistedDataThroughGateway() throws Exception {
        APIResponse anonymousList = request().get("/api/gastos/despesas/list");
        assertEquals(401, anonymousList.status(), anonymousList.text());

        String tag = "qa-" + UUID.randomUUID();
        long expenseId = createExpense(23.45, "D", tag, LocalDate.now());

        APIResponse response = listExpenses("D", tag);
        assertEquals(200, response.status(), response.text());

        JsonNode expenses = json(response.text());
        JsonNode persisted = null;
        for (JsonNode expense : expenses) {
            if (expense.path("id").asLong() == expenseId) {
                persisted = expense;
                break;
            }
        }

        assertNotNull(persisted, response.text());
        assertEquals(23.45, persisted.path("amount").asDouble());
        assertEquals(tag, persisted.path("tag").asText());
        assertEquals("D", persisted.path("operation").asText());
    }
}
