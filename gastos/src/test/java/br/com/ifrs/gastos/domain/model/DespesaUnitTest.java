package br.com.ifrs.gastos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DespesaUnitTest {

    // Verifica se o construtor e os acessores preservam os dados da despesa.
    @Test
    void constructorAndAccessorsPreserveExpenseData() {
        LocalDate date = LocalDate.of(2026, 11, 25);
        Despesa expense = new Despesa(7L, 3L, 25.5, Despesa.operations.D, "mercado", date);

        assertEquals(7L, expense.getId());
        assertEquals(3L, expense.getIdUser());
        assertEquals(25.5, expense.getAmount());
        assertEquals("D", expense.getOperation());
        assertEquals(Despesa.operations.D, expense.operationValue());
        assertEquals("mercado", expense.getTag());
        assertEquals(date, expense.getDate());
    }

    // Verifica se os setters atualizam os dados da despesa.
    @Test
    void settersUpdateExpenseData() {
        LocalDate date = LocalDate.of(2026, 11, 25);
        Despesa expense = new Despesa();

        expense.setId(8L);
        expense.setIdUser(4L);
        expense.setAmount(42.0);
        expense.setOperation(Despesa.operations.C);
        expense.setTag("salario");
        expense.setDate(date);

        assertEquals(8L, expense.getId());
        assertEquals(4L, expense.getIdUser());
        assertEquals(42.0, expense.getAmount());
        assertEquals("C", expense.getOperation());
        assertEquals("salario", expense.getTag());
        assertEquals(date, expense.getDate());
    }
}
