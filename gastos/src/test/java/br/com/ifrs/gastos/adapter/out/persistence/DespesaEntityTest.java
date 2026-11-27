package br.com.ifrs.gastos.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.com.ifrs.gastos.domain.model.Despesa;

class DespesaEntityTest {

    // Verifica a cópia de todos os campos do domínio para a entidade.
    @Test
    void fromDomainCopiesAllExpenseFields() {
        Despesa expense = new Despesa(
            12L,
            42L,
            18.75,
            Despesa.operations.D,
            "mercado",
            LocalDate.of(2026, 11, 25)
        );

        DespesaEntity entity = DespesaEntity.fromDomain(expense);

        assertAll(
            () -> assertEquals(12L, entity.id, "O identificador deve ser copiado."),
            () -> assertEquals(42L, entity.idUser, "O usuário deve ser copiado."),
            () -> assertEquals(18.75, entity.amount, "O valor deve ser copiado."),
            () -> assertEquals(Despesa.operations.D, entity.operation, "A operação deve ser copiada."),
            () -> assertEquals("mercado", entity.tag, "A tag deve ser copiada."),
            () -> assertEquals(LocalDate.of(2026, 11, 25), entity.date, "A data deve ser copiada.")
        );
    }

    // Verifica a cópia de todos os campos da entidade para o domínio.
    @Test
    void toDomainCopiesAllEntityFields() {
        DespesaEntity entity = new DespesaEntity();
        entity.id = 12L;
        entity.idUser = 42L;
        entity.amount = 18.75;
        entity.operation = Despesa.operations.C;
        entity.tag = "salario";
        entity.date = LocalDate.of(2026, 11, 25);

        Despesa expense = entity.toDomain();

        assertAll(
            () -> assertEquals(12L, expense.getId(), "O identificador deve ser copiado."),
            () -> assertEquals(42L, expense.getIdUser(), "O usuário deve ser copiado."),
            () -> assertEquals(18.75, expense.getAmount(), "O valor deve ser copiado."),
            () -> assertEquals(Despesa.operations.C, expense.operationValue(), "A operação deve ser copiada."),
            () -> assertEquals("salario", expense.getTag(), "A tag deve ser copiada."),
            () -> assertEquals(LocalDate.of(2026, 11, 25), expense.getDate(), "A data deve ser copiada.")
        );
    }
}
