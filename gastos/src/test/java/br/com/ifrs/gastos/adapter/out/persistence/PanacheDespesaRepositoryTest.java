package br.com.ifrs.gastos.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

class PanacheDespesaRepositoryTest {

    private static final Long USER_ID = 42L;
    private static final Long EXPENSE_ID = 7L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 25);
    private static final String TAG = "mercado";
    private static final String USER_ID_PARAMETER = "userId";

    private final PanacheDespesaRepository repository = new PanacheDespesaRepository();

    // Verifica a persistência de uma nova despesa e o retorno convertido para domínio.
    @Test
    void savePersistsNewExpenseAndReturnsDomainModel() {
        Despesa input = new Despesa(USER_ID, 18.75, Despesa.operations.D, TAG, DATE);
        Despesa expected = new Despesa(EXPENSE_ID, USER_ID, 18.75, Despesa.operations.D, TAG, DATE);

        try (MockedConstruction<DespesaEntity> construction = mockConstruction(
            DespesaEntity.class,
            (entity, context) -> {
                when(entity.persistAndFlush()).thenReturn(Uni.createFrom().item(entity));
                when(entity.toDomain()).thenReturn(expected);
            }
        )) {
            Despesa saved = repository.save(input).await().indefinitely();
            DespesaEntity persisted = construction.constructed().get(0);

            assertAll(
                () -> assertSame(expected, saved, "A entidade persistida deve ser convertida para o domínio."),
                () -> verify(persisted).persistAndFlush()
            );
        }
    }

    // Verifica a atualização dos campos de uma despesa já existente.
    @Test
    void saveUpdatesExistingExpense() {
        Despesa input = new Despesa(EXPENSE_ID, USER_ID + 1, 25.0, Despesa.operations.C, "salario", DATE.plusDays(1));
        Despesa expected = new Despesa(
            EXPENSE_ID,
            USER_ID + 1,
            25.0,
            Despesa.operations.C,
            "salario",
            DATE.plusDays(1)
        );
        DespesaEntity entity = spy(new DespesaEntity());
        doReturn(Uni.createFrom().item(entity)).when(entity).persistAndFlush();
        when(entity.toDomain()).thenReturn(expected);

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().item(entity));

            Despesa saved = repository.save(input).await().indefinitely();

            assertAll(
                () -> assertSame(expected, saved, "A despesa atualizada deve ser retornada."),
                () -> assertEquals(USER_ID + 1, entity.idUser, "O usuário deve ser atualizado."),
                () -> assertEquals(25.0, entity.amount, "O valor deve ser atualizado."),
                () -> assertEquals(Despesa.operations.C, entity.operation, "A operação deve ser atualizada."),
                () -> assertEquals("salario", entity.tag, "A tag deve ser atualizada."),
                () -> assertEquals(DATE.plusDays(1), entity.date, "A data deve ser atualizada."),
                () -> verify(entity).persistAndFlush()
            );
        }
    }

    // Confirma que a atualização falha quando o identificador não existe.
    @Test
    void saveFailsWhenExistingExpenseCannotBeFound() {
        Despesa input = new Despesa(EXPENSE_ID, USER_ID, 18.75, Despesa.operations.D, TAG, DATE);

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().nullItem());

            assertThrows(
                IllegalStateException.class,
                () -> repository.save(input).await().indefinitely(),
                "A atualização de um identificador inexistente deve falhar."
            );
        }
    }

    // Verifica o mapeamento da entidade encontrada para o domínio.
    @Test
    void findByIdReturnsMappedExpenseWhenPresent() {
        Despesa expected = new Despesa(EXPENSE_ID, USER_ID, 18.75, Despesa.operations.D, TAG, DATE);
        DespesaEntity entity = new DespesaEntity();
        entity.id = EXPENSE_ID;
        entity.idUser = USER_ID;
        entity.amount = 18.75;
        entity.operation = Despesa.operations.D;
        entity.tag = TAG;
        entity.date = DATE;

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().item(entity));

            Despesa result = repository.findById(EXPENSE_ID).await().indefinitely().orElseThrow();

            assertExpenseFields(expected, result);
        }
    }

    // Confirma que uma busca sem resultado retorna Optional vazio.
    @Test
    void findByIdReturnsEmptyWhenExpenseDoesNotExist() {
        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().nullItem());

            Optional<Despesa> result = repository.findById(EXPENSE_ID).await().indefinitely();

            assertEquals(Optional.empty(), result, "A busca por identificador ausente deve retornar Optional vazio.");
        }
    }

    // Verifica a remoção e o retorno da despesa removida.
    @Test
    void deleteReturnsDeletedExpenseWhenPresent() {
        Despesa expected = new Despesa(EXPENSE_ID, USER_ID, 18.75, Despesa.operations.D, TAG, DATE);
        DespesaEntity entity = spy(new DespesaEntity());
        entity.id = EXPENSE_ID;
        entity.idUser = USER_ID;
        entity.amount = 18.75;
        entity.operation = Despesa.operations.D;
        entity.tag = TAG;
        entity.date = DATE;
        doReturn(Uni.createFrom().voidItem()).when(entity).delete();

        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().item(entity));

            Despesa result = repository.deleteById(EXPENSE_ID).await().indefinitely().orElseThrow();

            assertAll(
                () -> assertExpenseFields(expected, result),
                () -> verify(entity).delete()
            );
        }
    }

    // Confirma que remover um identificador ausente retorna Optional vazio.
    @Test
    void deleteReturnsEmptyWhenExpenseDoesNotExist() {
        try (MockedStatic<PanacheEntityBase> panacheEntities = mockStatic(PanacheEntityBase.class)) {
            panacheEntities.when(() -> DespesaEntity.<DespesaEntity>findById(EXPENSE_ID))
                .thenReturn(Uni.createFrom().nullItem());

            Optional<Despesa> result = repository.deleteById(EXPENSE_ID).await().indefinitely();

            assertEquals(Optional.empty(), result, "A exclusão de um identificador ausente deve retornar vazio.");
        }
    }

    // Verifica a montagem da consulta e os parâmetros de todos os filtros.
    @Test
    void findByFiltersBuildsQueryAndBindsAllProvidedFilters() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<DespesaEntity> statement = mockSelectionQuery();
        DespesaEntity entity = new DespesaEntity();
        entity.id = EXPENSE_ID;
        entity.idUser = USER_ID;
        entity.amount = 18.75;
        entity.operation = Despesa.operations.D;
        entity.tag = TAG;
        entity.date = DATE;
        when(session.createQuery(anyString(), eq(DespesaEntity.class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);
        when(statement.getResultList()).thenReturn(Uni.createFrom().item(List.of(entity)));

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            List<Despesa> result = repository.findByFilters(
                USER_ID,
                " d ",
                TAG,
                "2026-11-01",
                "2026-11-30"
            ).await().indefinitely();

            assertAll(
                () -> assertEquals(1, result.size(), "A consulta deve retornar a entidade mapeada."),
                () -> verify(session).createQuery(
                    "SELECT d FROM Despesa d WHERE d.idUser = :userId AND d.operation = :operation "
                        + "AND d.tag = :tag AND d.date >= :dateStart AND d.date <= :dateEnd",
                    DespesaEntity.class
                ),
                () -> verify(statement).setParameter(USER_ID_PARAMETER, USER_ID),
                () -> verify(statement).setParameter("operation", Despesa.operations.D),
                () -> verify(statement).setParameter("tag", TAG),
                () -> verify(statement).setParameter("dateStart", LocalDate.of(2026, 11, 1)),
                () -> verify(statement).setParameter("dateEnd", LocalDate.of(2026, 11, 30))
            );
        }
    }

    // Confirma que filtros opcionais em branco não entram na consulta.
    @Test
    void findByFiltersOmitsBlankOptionalFilters() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<DespesaEntity> statement = mockSelectionQuery();
        when(session.createQuery(anyString(), eq(DespesaEntity.class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);
        when(statement.getResultList()).thenReturn(Uni.createFrom().item(List.of()));

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            List<Despesa> result = repository.findByFilters(USER_ID, " ", "", null, null)
                .await().indefinitely();

            assertAll(
                () -> assertEquals(List.of(), result, "Sem filtros opcionais, a lista vazia deve ser preservada."),
                () -> verify(session).createQuery(
                    "SELECT d FROM Despesa d WHERE d.idUser = :userId",
                    DespesaEntity.class
                ),
                () -> verify(statement).setParameter(USER_ID_PARAMETER, USER_ID)
            );
        }
    }

    // Verifica a rejeição de uma operação fora dos valores permitidos.
    @Test
    void findByFiltersRejectsInvalidOperation() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<DespesaEntity> statement = mockSelectionQuery();
        when(session.createQuery(anyString(), eq(DespesaEntity.class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByFilters(USER_ID, "X", null, null, null).await().indefinitely(),
                "Uma operação fora do enum deve ser rejeitada."
            );
        }
    }

    // Confirma que uma data inválida é rejeitada ao montar os filtros.
    @Test
    void findByFiltersRejectsMalformedDate() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<DespesaEntity> statement = mockSelectionQuery();
        when(session.createQuery(anyString(), eq(DespesaEntity.class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            assertThrows(
                DateTimeParseException.class,
                () -> repository.findByFilters(USER_ID, null, null, "not-a-date", null).await().indefinitely(),
                "Uma data inválida deve ser rejeitada."
            );
        }
    }

    // Verifica a conversão das somas por tag dentro do período informado.
    @Test
    void sumByTagMapsRowsAndBindsDateRange() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<Object[]> statement = mockSelectionQuery();
        List<Object[]> rows = Collections.singletonList(new Object[] { TAG, 18.75 });
        when(session.createQuery(anyString(), eq(Object[].class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);
        when(statement.getResultList()).thenReturn(Uni.createFrom().item(rows));

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            List<TagSum> result = repository.sumByTag(USER_ID, "2026-11-01", "2026-11-30")
                .await().indefinitely();

            assertAll(
                () -> assertEquals(1, result.size(), "Deve haver um total por tag."),
                () -> assertEquals(TAG, result.get(0).getTag(), "A tag da linha deve ser preservada."),
                () -> assertEquals(18.75, result.get(0).getTotal(), "O total da linha deve ser convertido."),
                () -> verify(statement).setParameter(USER_ID_PARAMETER, USER_ID),
                () -> verify(statement).setParameter("operation", Despesa.operations.D),
                () -> verify(statement).setParameter("dateStart", LocalDate.of(2026, 11, 1)),
                () -> verify(statement).setParameter("dateEnd", LocalDate.of(2026, 11, 30))
            );
        }
    }

    // Confirma o total padrão zero e a consulta sem intervalo de datas.
    @Test
    void sumByTagDefaultsNullTotalAndSupportsNoDateRange() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<Object[]> statement = mockSelectionQuery();
        List<Object[]> rows = Collections.singletonList(new Object[] { TAG, null });
        when(session.createQuery(anyString(), eq(Object[].class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);
        when(statement.getResultList()).thenReturn(Uni.createFrom().item(rows));

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            List<TagSum> result = repository.sumByTag(USER_ID, null, null).await().indefinitely();

            assertAll(
                () -> assertEquals(0.0, result.get(0).getTotal(), "Um total nulo deve ser convertido para zero."),
                () -> verify(statement).setParameter(USER_ID_PARAMETER, USER_ID),
                () -> verify(statement).setParameter("operation", Despesa.operations.D)
            );
        }
    }

    // Verifica o retorno do saldo e os parâmetros de crédito e débito.
    @Test
    void calculateBalanceReturnsQueryResultAndBindsOperationTypes() {
        Mutiny.Session session = mock(Mutiny.Session.class);
        Mutiny.SelectionQuery<Double> statement = mockSelectionQuery();
        when(session.createQuery(anyString(), eq(Double.class))).thenReturn(statement);
        when(statement.setParameter(anyString(), any())).thenReturn(statement);
        when(statement.getSingleResult()).thenReturn(Uni.createFrom().item(12.5));

        try (MockedStatic<Panache> panache = mockStatic(Panache.class)) {
            panache.when(Panache::getSession).thenReturn(Uni.createFrom().item(session));

            Double result = repository.calculateBalance(USER_ID).await().indefinitely();

            assertAll(
                () -> assertEquals(12.5, result, "O saldo calculado deve ser retornado."),
                () -> verify(statement).setParameter("credit", Despesa.operations.C),
                () -> verify(statement).setParameter("debit", Despesa.operations.D),
                () -> verify(statement).setParameter(USER_ID_PARAMETER, USER_ID)
            );
        }
    }

    private static void assertExpenseFields(Despesa expected, Despesa actual) {
        assertAll(
            () -> assertEquals(expected.getId(), actual.getId(), "O identificador deve ser preservado."),
            () -> assertEquals(expected.getIdUser(), actual.getIdUser(), "O usuário deve ser preservado."),
            () -> assertEquals(expected.getAmount(), actual.getAmount(), "O valor deve ser preservado."),
            () -> assertEquals(expected.operationValue(), actual.operationValue(), "A operação deve ser preservada."),
            () -> assertEquals(expected.getTag(), actual.getTag(), "A tag deve ser preservada."),
            () -> assertEquals(expected.getDate(), actual.getDate(), "A data deve ser preservada.")
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> Mutiny.SelectionQuery<T> mockSelectionQuery() {
        return mock(Mutiny.SelectionQuery.class);
    }
}
