package br.com.ifrs.gateway.application.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import br.com.ifrs.gateway.application.port.out.GastosServicePort;
import br.com.ifrs.gateway.application.port.out.UsersServicePort;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class GatewayApplicationServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 25);
    private static final ForwardedResponse RESPONSE = new ForwardedResponse(200, "body", "text/plain", Map.of());

    @Mock
    UsersServicePort usersService;

    @Mock
    GastosServicePort gastosService;

    @InjectMocks
    GatewayApplicationService service;

    // Verifica o encaminhamento das operações de usuário sem alterar dados ou resposta.
    @Test
    void delegatesUserOperationsWithoutChangingArgumentsOrResponse() {
        Uni<ForwardedResponse> result = Uni.createFrom().item(RESPONSE);
        when(usersService.login("ana@example.test", "password")).thenReturn(result);
        when(usersService.createUser("Ana", "ana@example.test", "password")).thenReturn(result);
        when(usersService.updateUser("Bearer token", "Ana Silva", "ana.silva@example.test", "new-password"))
            .thenReturn(result);
        when(usersService.deleteUser("Bearer token")).thenReturn(result);

        assertSame(RESPONSE, service.login("ana@example.test", "password").await().indefinitely());
        assertSame(RESPONSE, service.createUser("Ana", "ana@example.test", "password").await().indefinitely());
        assertSame(RESPONSE, service.updateUser(
            "Bearer token", "Ana Silva", "ana.silva@example.test", "new-password"
        ).await().indefinitely());
        assertSame(RESPONSE, service.deleteUser("Bearer token").await().indefinitely());

        verify(usersService).login("ana@example.test", "password");
        verify(usersService).createUser("Ana", "ana@example.test", "password");
        verify(usersService).updateUser("Bearer token", "Ana Silva", "ana.silva@example.test", "new-password");
        verify(usersService).deleteUser("Bearer token");
    }

    // Verifica o encaminhamento das operações de despesas sem alterar dados ou resposta.
    @Test
    void delegatesExpenseOperationsWithoutChangingArgumentsOrResponse() {
        Uni<ForwardedResponse> result = Uni.createFrom().item(RESPONSE);
        when(gastosService.createDespesa("Bearer token", 12.5, "D", "mercado", DATE)).thenReturn(result);
        when(gastosService.getDespesas("Bearer token", "D", "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(result);
        when(gastosService.updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE)).thenReturn(result);
        when(gastosService.deleteDespesa("Bearer token", 7L)).thenReturn(result);
        when(gastosService.getTagSums("Bearer token", "mercado", "2026-11-01", "2026-11-30"))
            .thenReturn(result);
        when(gastosService.getSaldo("Bearer token")).thenReturn(result);

        assertSame(RESPONSE, service.createDespesa("Bearer token", 12.5, "D", "mercado", DATE)
            .await().indefinitely());
        assertSame(RESPONSE, service.getDespesas(
            "Bearer token", "D", "mercado", "2026-11-01", "2026-11-30"
        ).await().indefinitely());
        assertSame(RESPONSE, service.updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE)
            .await().indefinitely());
        assertSame(RESPONSE, service.deleteDespesa("Bearer token", 7L).await().indefinitely());
        assertSame(RESPONSE, service.getTagSums(
            "Bearer token", "mercado", "2026-11-01", "2026-11-30"
        ).await().indefinitely());
        assertSame(RESPONSE, service.getSaldo("Bearer token").await().indefinitely());

        verify(gastosService).createDespesa("Bearer token", 12.5, "D", "mercado", DATE);
        verify(gastosService).getDespesas("Bearer token", "D", "mercado", "2026-11-01", "2026-11-30");
        verify(gastosService).updateDespesa("Bearer token", 7L, 15.0, "C", "salario", DATE);
        verify(gastosService).deleteDespesa("Bearer token", 7L);
        verify(gastosService).getTagSums("Bearer token", "mercado", "2026-11-01", "2026-11-30");
        verify(gastosService).getSaldo("Bearer token");
    }
}
