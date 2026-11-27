package br.com.ifrs.gastos.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.ifrs.gastos.application.port.in.GastosUseCases;
import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.WebApplicationException;

class GastosResourceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 25);
    private static final String TAG = "mercado";
    private static final String INVALID_USER_ID = "invalid";
    private static final String DEFAULT_TAG = "Outros";
    private static final String SALARY_TAG = "salário";
    private static final String DATE_START = "2026-11-01";
    private static final String DATE_END = "2026-11-30";

    private SecurityIdentity securityIdentity;
    private JsonWebToken jwt;
    private GastosUseCases gastosUseCases;
    private GastosResource resource;

    @BeforeEach
    void setUp() {
        securityIdentity = mock(SecurityIdentity.class);
        jwt = mock(JsonWebToken.class);
        gastosUseCases = mock(GastosUseCases.class);
        resource = new GastosResource();
        resource.securityIdentity = securityIdentity;
        resource.jwt = jwt;
        resource.gastosUseCases = gastosUseCases;
    }

    // Verifica se o endpoint de autenticação retorna as claims do JWT.
    @Test
    void testAuthReturnsJwtClaimsAndAuthenticationStatus() {
        authenticateAs("42");
        when(jwt.getName()).thenReturn("user@example.com");
        when(jwt.getGroups()).thenReturn(Set.of("user"));

        String entity = resource.testAuth().await().indefinitely().getEntity().toString();

        assertEquals(
            "{\"userId\":\"42\",\"upn\":\"user@example.com\",\"groups\":\"[user]\",\"authenticated\":true}",
            entity,
            "A resposta deve conter as claims do JWT e o estado autenticado."
        );
    }

    // Verifica a resposta do endpoint quando não há JWT disponível.
    @Test
    void testAuthReturnsNullValuesWhenJwtIsMissing() {
        resourceWithoutJwt();
        when(securityIdentity.isAnonymous()).thenReturn(true);

        String entity = resource.testAuth().await().indefinitely().getEntity().toString();

        assertEquals(
            "{\"userId\":\"null\",\"upn\":\"null\",\"groups\":\"null\",\"authenticated\":false}",
            entity,
            "A resposta deve representar as claims ausentes como nulas."
        );
    }

    // Verifica que a criação rejeita a ausência da identidade de segurança.
    @Test
    void createRejectsMissingSecurityIdentity() {
        resourceWithoutSecurityIdentity();

        assertFailureStatus(resource.createDespesa(10.0, "D", TAG, DATE), 401);
    }

    // Verifica que a criação rejeita uma identidade anônima.
    @Test
    void createRejectsAnonymousIdentity() {
        when(securityIdentity.isAnonymous()).thenReturn(true);

        assertFailureStatus(resource.createDespesa(10.0, "D", TAG, DATE), 401);
    }

    // Verifica que a criação rejeita a ausência do token JWT.
    @Test
    void createRejectsMissingJwt() {
        resourceWithoutJwt();
        when(securityIdentity.isAnonymous()).thenReturn(false);

        assertFailureStatus(resource.createDespesa(10.0, "D", TAG, DATE), 401);
    }

    // Verifica que a criação rejeita a ausência da identificação do usuário.
    @Test
    void createRejectsMissingUserIdClaim() {
        authenticateAs(null);

        assertFailureStatus(resource.createDespesa(10.0, "D", TAG, DATE), 401);
    }

    // Verifica que a criação rejeita uma identificação de usuário inválida.
    @Test
    void createRejectsInvalidUserIdClaim() {
        authenticateAs(INVALID_USER_ID);
        assertFailureStatus(resource.createDespesa(10.0, "D", TAG, DATE), 400);
    }

    // Verifica que a criação rejeita a ausência do valor da despesa.
    @Test
    void createRejectsMissingAmount() {
        authenticateAs("42");

        assertFailureStatus(resource.createDespesa(null, "D", TAG, DATE), 400);
    }

    // Verifica que a criação rejeita a ausência da operação.
    @Test
    void createRejectsMissingOperation() {
        authenticateAs("42");

        assertFailureStatus(resource.createDespesa(10.0, null, TAG, DATE), 400);
    }

    // Verifica que a criação rejeita uma operação em branco.
    @Test
    void createRejectsBlankOperation() {
        authenticateAs("42");

        assertFailureStatus(resource.createDespesa(10.0, "  ", TAG, DATE), 400);
    }

    // Verifica que a criação rejeita uma operação não suportada.
    @Test
    void createRejectsUnsupportedOperation() {
        authenticateAs("42");

        assertFailureStatus(resource.createDespesa(10.0, "X", TAG, DATE), 400);
    }

    // Verifica que a criação rejeita a ausência da data.
    @Test
    void createRejectsMissingDate() {
        authenticateAs("42");

        assertFailureStatus(resource.createDespesa(10.0, "D", "mercado", null), 400);
    }

    // Verifica o uso da tag padrão e a normalização da operação na criação.
    @Test
    void createDefaultsMissingTagAndNormalizesOperation() {
        authenticateAs("42");
        Despesa expense = new Despesa(3L, 42L, 10.0, Despesa.operations.D, DEFAULT_TAG, DATE);
        when(gastosUseCases.create(42L, 10.0, Despesa.operations.D, DEFAULT_TAG, DATE))
            .thenReturn(Uni.createFrom().item(expense));

        resource.createDespesa(10.0, " d ", null, DATE).await().indefinitely();

        verify(gastosUseCases).create(42L, 10.0, Despesa.operations.D, DEFAULT_TAG, DATE);
    }

    // Verifica o uso da tag padrão quando a tag recebida está em branco.
    @Test
    void createDefaultsBlankTag() {
        authenticateAs("42");
        when(gastosUseCases.create(42L, 10.0, Despesa.operations.C, DEFAULT_TAG, DATE))
            .thenReturn(Uni.createFrom().nullItem());

        resource.createDespesa(10.0, "C", "  ", DATE).await().indefinitely();

        verify(gastosUseCases).create(42L, 10.0, Despesa.operations.C, DEFAULT_TAG, DATE);
    }

    // Verifica que a criação retorna a despesa com a tag informada.
    @Test
    void createPreservesProvidedTag() {
        authenticateAs("42");
        Despesa expense = new Despesa(3L, 42L, 10.0, Despesa.operations.C, SALARY_TAG, DATE);
        when(gastosUseCases.create(42L, 10.0, Despesa.operations.C, SALARY_TAG, DATE))
            .thenReturn(Uni.createFrom().item(expense));

        Despesa result = resource.createDespesa(10.0, "C", SALARY_TAG, DATE).await().indefinitely();

        assertSame(expense, result, "A despesa criada deve ser devolvida ao cliente.");
    }

    // Verifica que a consulta rejeita a ausência da identidade de segurança.
    @Test
    void getDespesasRejectsMissingSecurityIdentity() {
        resourceWithoutSecurityIdentity();

        assertFailureStatus(resource.getDespesas(null, null, null, null), 401);
    }

    // Verifica que a consulta rejeita uma identidade anônima.
    @Test
    void getDespesasRejectsAnonymousIdentity() {
        when(securityIdentity.isAnonymous()).thenReturn(true);

        assertFailureStatus(resource.getDespesas(null, null, null, null), 401);
    }

    // Verifica que a consulta rejeita a ausência do token JWT.
    @Test
    void getDespesasRejectsMissingJwt() {
        resourceWithoutJwt();
        when(securityIdentity.isAnonymous()).thenReturn(false);

        assertFailureStatus(resource.getDespesas(null, null, null, null), 401);
    }

    // Verifica que a consulta rejeita a ausência da identificação do usuário.
    @Test
    void getDespesasRejectsMissingUserIdClaim() {
        authenticateAs(null);
        assertFailureStatus(resource.getDespesas(null, null, null, null), 401);
    }

    // Verifica que a consulta rejeita uma identificação de usuário inválida.
    @Test
    void getDespesasRejectsInvalidUserIdClaim() {
        authenticateAs(INVALID_USER_ID);
        assertFailureStatus(resource.getDespesas(null, null, null, null), 400);
    }

    // Verifica que datas em branco são normalizadas antes de consultar as despesas.
    @Test
    void getDespesasNormalizesBlankDatesAndDelegatesFilters() {
        authenticateAs("42");
        when(gastosUseCases.getDespesasByFilters(42L, "D", TAG, null, null))
            .thenReturn(Uni.createFrom().item(List.of()));

        resource.getDespesas("D", TAG, "  ", "").await().indefinitely();

        verify(gastosUseCases).getDespesasByFilters(42L, "D", TAG, null, null);
    }

    // Verifica que datas preenchidas são encaminhadas sem alteração.
    @Test
    void getDespesasPreservesNonblankDates() {
        authenticateAs("42");
        when(gastosUseCases.getDespesasByFilters(42L, null, null, DATE_START, DATE_END))
            .thenReturn(Uni.createFrom().item(List.of()));

        resource.getDespesas(null, null, DATE_START, DATE_END).await().indefinitely();

        verify(gastosUseCases).getDespesasByFilters(42L, null, null, DATE_START, DATE_END);
    }

    // Verifica que datas ausentes são encaminhadas como nulas.
    @Test
    void getDespesasKeepsNullDatesNull() {
        authenticateAs("42");
        when(gastosUseCases.getDespesasByFilters(42L, null, null, null, null))
            .thenReturn(Uni.createFrom().item(List.of()));

        resource.getDespesas(null, null, null, null).await().indefinitely();

        verify(gastosUseCases).getDespesasByFilters(42L, null, null, null, null);
    }

    // Verifica o encaminhamento dos campos informados e da operação normalizada.
    @Test
    void updateDelegatesOptionalFieldsAndParsedOperation() {
        when(gastosUseCases.update(7L, 25.0, Despesa.operations.C, SALARY_TAG, DATE))
            .thenReturn(Uni.createFrom().nullItem());

        resource.updateDespesa(7L, 25.0, " c ", SALARY_TAG, DATE).await().indefinitely();

        verify(gastosUseCases).update(7L, 25.0, Despesa.operations.C, SALARY_TAG, DATE);
    }

    // Verifica que a atualização aceita operação ausente ou em branco.
    @Test
    void updateAllowsMissingOrBlankOperation() {
        when(gastosUseCases.update(7L, null, null, null, null)).thenReturn(Uni.createFrom().nullItem());

        resource.updateDespesa(7L, null, null, null, null).await().indefinitely();
        resource.updateDespesa(7L, null, "  ", null, null).await().indefinitely();

        verify(gastosUseCases, org.mockito.Mockito.times(2)).update(7L, null, null, null, null);
    }

    // Verifica que a atualização rejeita uma operação não suportada.
    @Test
    void updateRejectsUnsupportedOperation() {
        assertThrowsWithStatus(
            400,
            () -> resource.updateDespesa(7L, null, "X", null, null)
        );
    }

    // Verifica que a exclusão retorna a despesa encontrada.
    @Test
    void deleteReturnsExpenseWhenRepositoryFindsIt() {
        Despesa expense = new Despesa(7L, 42L, 10.0, Despesa.operations.D, TAG, DATE);
        when(gastosUseCases.delete(7L)).thenReturn(Uni.createFrom().item(Optional.of(expense)));

        Despesa result = resource.deleteDespesa(7L).await().indefinitely();

        assertSame(expense, result, "A despesa encontrada deve ser retornada.");
    }

    // Verifica que a exclusão retorna nulo quando a despesa não existe.
    @Test
    void deleteReturnsNullWhenExpenseDoesNotExist() {
        when(gastosUseCases.delete(404L)).thenReturn(Uni.createFrom().item(Optional.empty()));

        Despesa result = resource.deleteDespesa(404L).await().indefinitely();

        assertNull(result, "A exclusão deve retornar nulo quando a despesa não existe.");
    }

    // Verifica que a consulta por tags rejeita a ausência da identidade de segurança.
    @Test
    void getDebitByTagsRejectsMissingSecurityIdentity() {
        resourceWithoutSecurityIdentity();

        assertFailureStatus(resource.getDebitByTags(null, null, null), 401);
    }

    // Verifica que a consulta por tags rejeita uma identidade anônima.
    @Test
    void getDebitByTagsRejectsAnonymousIdentity() {
        when(securityIdentity.isAnonymous()).thenReturn(true);

        assertFailureStatus(resource.getDebitByTags(null, null, null), 401);
    }

    // Verifica que a consulta por tags rejeita a ausência do token JWT.
    @Test
    void getDebitByTagsRejectsMissingJwt() {
        resourceWithoutJwt();
        when(securityIdentity.isAnonymous()).thenReturn(false);

        assertFailureStatus(resource.getDebitByTags(null, null, null), 401);
    }

    // Verifica que a consulta por tags rejeita a ausência da identificação do usuário.
    @Test
    void getDebitByTagsRejectsMissingUserIdClaim() {
        authenticateAs(null);
        assertFailureStatus(resource.getDebitByTags(null, null, null), 401);
    }

    // Verifica que a consulta por tags rejeita uma identificação de usuário inválida.
    @Test
    void getDebitByTagsRejectsInvalidUserIdClaim() {
        authenticateAs(INVALID_USER_ID);
        assertFailureStatus(resource.getDebitByTags(null, null, null), 400);
    }

    // Verifica o encaminhamento do usuário e do período na consulta por tags.
    @Test
    void getDebitByTagsDelegatesUserAndDateRange() {
        authenticateAs("42");
        List<TagSum> sums = List.of(new TagSum(TAG, 10.0));
        when(gastosUseCases.getTagsByUserId(42L, DATE_START, DATE_END))
            .thenReturn(Uni.createFrom().item(sums));

        List<TagSum> result = resource.getDebitByTags(TAG, DATE_START, DATE_END)
            .await().indefinitely();

        assertSame(sums, result, "O resultado do cálculo deve ser retornado.");
    }

    // Verifica que o cálculo do saldo rejeita a ausência da identidade de segurança.
    @Test
    void getSaldoRejectsMissingSecurityIdentity() {
        resourceWithoutSecurityIdentity();

        assertFailureStatus(resource.getSaldo(), 401);
    }

    // Verifica que o cálculo do saldo rejeita uma identidade anônima.
    @Test
    void getSaldoRejectsAnonymousIdentity() {
        when(securityIdentity.isAnonymous()).thenReturn(true);

        assertFailureStatus(resource.getSaldo(), 401);
    }

    // Verifica que o cálculo do saldo rejeita a ausência do token JWT.
    @Test
    void getSaldoRejectsMissingJwt() {
        resourceWithoutJwt();
        when(securityIdentity.isAnonymous()).thenReturn(false);

        assertFailureStatus(resource.getSaldo(), 401);
    }

    // Verifica que o cálculo do saldo rejeita a ausência da identificação do usuário.
    @Test
    void getSaldoRejectsMissingUserIdClaim() {
        authenticateAs(null);
        assertFailureStatus(resource.getSaldo(), 401);
    }

    // Verifica que o cálculo do saldo rejeita uma identificação de usuário inválida.
    @Test
    void getSaldoRejectsInvalidUserIdClaim() {
        authenticateAs(INVALID_USER_ID);
        assertFailureStatus(resource.getSaldo(), 400);
    }

    // Verifica que o saldo calculado é solicitado para o usuário autenticado.
    @Test
    void getSaldoDelegatesAuthenticatedUserId() {
        authenticateAs("42");
        when(gastosUseCases.calculateSaldoByUserId(42L)).thenReturn(Uni.createFrom().item(25.0));

        Double result = resource.getSaldo().await().indefinitely();

        assertEquals(25.0, result, "O saldo calculado deve ser retornado.");
    }

    private void resourceWithoutSecurityIdentity() {
        resource = new GastosResource();
        resource.jwt = jwt;
        resource.gastosUseCases = gastosUseCases;
    }

    private void resourceWithoutJwt() {
        resource = new GastosResource();
        resource.securityIdentity = securityIdentity;
        resource.gastosUseCases = gastosUseCases;
    }

    private void authenticateAs(String userId) {
        when(securityIdentity.isAnonymous()).thenReturn(false);
        doReturn(userId).when(jwt).getClaim("id");
    }

    private static void assertFailureStatus(Uni<?> result, int expectedStatus) {
        WebApplicationException exception = assertThrows(
            WebApplicationException.class,
            () -> result.await().indefinitely()
        );
        assertEquals(expectedStatus, exception.getResponse().getStatus(), "O status HTTP deve corresponder ao erro.");
    }

    private static void assertThrowsWithStatus(int expectedStatus, Runnable operation) {
        WebApplicationException exception = assertThrows(WebApplicationException.class, operation::run);
        assertEquals(expectedStatus, exception.getResponse().getStatus(), "O status HTTP deve corresponder ao erro.");
    }
}
