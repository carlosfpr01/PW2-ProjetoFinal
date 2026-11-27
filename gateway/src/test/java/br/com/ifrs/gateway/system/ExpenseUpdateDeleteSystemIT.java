package br.com.ifrs.gateway.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;

class ExpenseUpdateDeleteSystemIT extends SystemApiTestSupport {

    // Verifica a atualização, exclusão e ausência posterior da despesa.
    @Test
    void updatesAndDeletesExpenseThenConfirmsItIsGone() throws Exception {
        String originalTag = "qa-original-" + UUID.randomUUID();
        String updatedTag = "qa-updated-" + UUID.randomUUID();
        long expenseId = createExpense(10.0, "D", originalTag, LocalDate.now());

        APIResponse update = request().patch(
            "/api/gastos/despesa/update?id=" + expenseId
                + "&amount=18.75&operation=C&tag=" + updatedTag
                + "&date=" + LocalDate.now(),
            authorizedRequest()
        );
        assertEquals(200, update.status(), update.text());
        assertEquals(18.75, json(update.text()).path("amount").asDouble());

        APIResponse updatedList = listExpenses("C", updatedTag);
        assertEquals(200, updatedList.status(), updatedList.text());
        assertEquals(expenseId, json(updatedList.text()).get(0).path("id").asLong());

        APIResponse deleted = request().delete(
            "/api/gastos/despesa/delete?id=" + expenseId,
            authorizedRequest()
        );
        assertTrue(deleted.status() == 200 || deleted.status() == 204, deleted.text());
        forgetExpense(expenseId);

        APIResponse afterDelete = listExpenses("C", updatedTag);
        assertEquals(200, afterDelete.status(), afterDelete.text());
        JsonNode expenses = json(afterDelete.text());
        assertFalse(expenses.elements().hasNext(), afterDelete.text());
    }
}
