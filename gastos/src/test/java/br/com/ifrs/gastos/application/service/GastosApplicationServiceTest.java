package br.com.ifrs.gastos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ifrs.gastos.application.port.out.DespesaRepository;
import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class GastosApplicationServiceTest {

    private static final LocalDate ORIGINAL_DATE = LocalDate.of(2026, 1, 10);
    private static final LocalDate UPDATED_DATE = LocalDate.of(2026, 2, 20);

    @Mock
    DespesaRepository despesaRepository;

    @InjectMocks
    GastosApplicationService service;

    private Despesa despesa;

    @BeforeEach
    void setUp() {
        despesa = new Despesa(7L, 3L, 25.0, Despesa.operations.D, "mercado", ORIGINAL_DATE);
    }

    // Verifica a criação da despesa com os dados do usuário e sua persistência.
    @Test
    void createBuildsAndPersistsExpenseForUser() {
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(invocation ->
            Uni.createFrom().item(invocation.getArgument(0, Despesa.class)));

        Despesa created = service.create(3L, 25.0, Despesa.operations.D, "mercado", ORIGINAL_DATE)
            .await().indefinitely();

        assertNull(created.getId());
        assertEquals(3L, created.getIdUser());
        assertEquals(25.0, created.getAmount());
        assertEquals(Despesa.operations.D, created.operationValue());
        assertEquals("mercado", created.getTag());
        assertEquals(ORIGINAL_DATE, created.getDate());
        verify(despesaRepository).save(any(Despesa.class));
    }

    // Verifica a atualização dos campos informados e a persistência da despesa.
    @Test
    void updateChangesProvidedFieldsAndPersistsExpense() {
        when(despesaRepository.findById(7L)).thenReturn(Uni.createFrom().item(Optional.of(despesa)));
        when(despesaRepository.save(despesa)).thenReturn(Uni.createFrom().item(despesa));

        Despesa updated = service.update(7L, 42.5, Despesa.operations.C, "salario", UPDATED_DATE)
            .await().indefinitely();

        assertEquals(42.5, updated.getAmount());
        assertEquals(Despesa.operations.C, updated.operationValue());
        assertEquals("salario", updated.getTag());
        assertEquals(UPDATED_DATE, updated.getDate());
        verify(despesaRepository).save(despesa);
    }

    // Verifica que valores nulos ou em branco mantêm os dados existentes.
    @Test
    void updateKeepsExistingFieldsForNullOrBlankValues() {
        when(despesaRepository.findById(7L)).thenReturn(Uni.createFrom().item(Optional.of(despesa)));
        when(despesaRepository.save(despesa)).thenReturn(Uni.createFrom().item(despesa));

        Despesa updated = service.update(7L, null, null, "   ", null).await().indefinitely();

        assertEquals(25.0, updated.getAmount());
        assertEquals(Despesa.operations.D, updated.operationValue());
        assertEquals("mercado", updated.getTag());
        assertEquals(ORIGINAL_DATE, updated.getDate());
        verify(despesaRepository).save(despesa);
    }

    // Verifica que uma tag nula preserva a tag já cadastrada.
    @Test
    void updateKeepsExistingTagWhenTagIsNull() {
        when(despesaRepository.findById(7L)).thenReturn(Uni.createFrom().item(Optional.of(despesa)));
        when(despesaRepository.save(despesa)).thenReturn(Uni.createFrom().item(despesa));

        Despesa updated = service.update(7L, 42.5, null, null, null).await().indefinitely();

        assertEquals(despesa.getTag(), updated.getTag(), "A tag existente deve ser mantida quando a nova tag é nula.");
        verify(despesaRepository).save(despesa);
    }

    // Verifica que a atualização retorna nulo quando a despesa não existe.
    @Test
    void updateReturnsNullWhenExpenseDoesNotExist() {
        when(despesaRepository.findById(404L)).thenReturn(Uni.createFrom().item(Optional.empty()));

        assertNull(service.update(404L, 42.5, Despesa.operations.C, "salario", UPDATED_DATE)
            .await().indefinitely());

        verify(despesaRepository, never()).save(any(Despesa.class));
    }

    // Verifica o encaminhamento dos filtros de despesas ao repositório.
    @Test
    void getDespesasByFiltersDelegatesAllFilters() {
        List<Despesa> expected = List.of(despesa);
        when(despesaRepository.findByFilters(3L, "D", "mercado", "2026-01-01", "2026-01-31"))
            .thenReturn(Uni.createFrom().item(expected));

        assertEquals(expected, service.getDespesasByFilters(3L, "D", "mercado", "2026-01-01", "2026-01-31")
            .await().indefinitely());
    }

    // Verifica o encaminhamento da exclusão ao repositório.
    @Test
    void deleteDelegatesToRepository() {
        when(despesaRepository.deleteById(7L)).thenReturn(Uni.createFrom().item(Optional.of(despesa)));

        assertEquals(Optional.of(despesa), service.delete(7L).await().indefinitely());
    }

    // Verifica o encaminhamento dos cálculos de totais por tag e saldo.
    @Test
    void tagSumsAndBalanceDelegateToRepository() {
        List<TagSum> sums = List.of(new TagSum("mercado", 25.0));
        when(despesaRepository.sumByTag(3L, "2026-01-01", "2026-01-31"))
            .thenReturn(Uni.createFrom().item(sums));
        when(despesaRepository.calculateBalance(3L)).thenReturn(Uni.createFrom().item(-25.0));

        assertEquals(sums, service.getTagsByUserId(3L, "2026-01-01", "2026-01-31").await().indefinitely());
        assertEquals(-25.0, service.calculateSaldoByUserId(3L).await().indefinitely());
    }
}
