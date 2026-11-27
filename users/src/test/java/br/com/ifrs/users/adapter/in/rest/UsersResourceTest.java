package br.com.ifrs.users.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.ifrs.users.application.port.in.UserUseCases;
import br.com.ifrs.users.domain.model.User;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

class UsersResourceTest {

    private SecurityIdentity securityIdentity;
    private JsonWebToken jwt;
    private UserUseCases userUseCases;
    private UsersResource resource;

    @BeforeEach
    void setUp() {
        securityIdentity = mock(SecurityIdentity.class);
        jwt = mock(JsonWebToken.class);
        userUseCases = mock(UserUseCases.class);
        resource = new UsersResource();
        resource.securityIdentity = securityIdentity;
        resource.jwt = jwt;
        resource.userUseCases = userUseCases;
    }

    // Verifica que o login retorna o token quando as credenciais são válidas.
    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        when(userUseCases.login("ana@example.test", "secret"))
            .thenReturn(Uni.createFrom().item(Optional.of("access-token")));

        Response response = resource.getToken("ana@example.test", "secret").await().indefinitely();

        assertEquals(200, response.getStatus());
        assertEquals("access-token", response.getEntity());
        verify(userUseCases).login("ana@example.test", "secret");
    }

    // Verifica que credenciais inválidas resultam em resposta não autorizada.
    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() {
        when(userUseCases.login("ana@example.test", "wrong"))
            .thenReturn(Uni.createFrom().item(Optional.empty()));

        Response response = resource.getToken("ana@example.test", "wrong").await().indefinitely();

        assertEquals(401, response.getStatus());
        assertEquals("Credenciais inválidas", response.getEntity());
    }

    // Verifica que a criação encaminha os dados e retorna o usuário criado.
    @Test
    void createUserDelegatesInputAndReturnsCreatedUser() {
        User created = new User(42L, "Ana", "ana@example.test", "hashed", "2026-11-26");
        when(userUseCases.create("Ana", "ana@example.test", "secret"))
            .thenReturn(Uni.createFrom().item(created));

        User result = resource.createUser("Ana", "ana@example.test", "secret").await().indefinitely();

        assertSame(created, result);
        verify(userUseCases).create("Ana", "ana@example.test", "secret");
    }

    // Verifica que a atualização rejeita identidade ausente, anônima ou sem JWT.
    @Test
    void updateRejectsMissingAuthentication() {
        resource.securityIdentity = null;
        assertEquals(401, resource.updateUser("Ana", null, null).await().indefinitely().getStatus());

        when(securityIdentity.isAnonymous()).thenReturn(true);
        assertEquals(401, resource.updateUser("Ana", null, null).await().indefinitely().getStatus());

        when(securityIdentity.isAnonymous()).thenReturn(false);
        resource.jwt = null;
        assertEquals(401, resource.updateUser("Ana", null, null).await().indefinitely().getStatus());

        verifyNoInteractions(userUseCases);
    }

    // Verifica que a atualização rejeita claim de usuário ausente ou inválida.
    @Test
    void updateRejectsMissingOrInvalidUserIdClaim() {
        when(jwt.getClaim("id")).thenReturn(null);
        assertEquals(401, resource.updateUser(null, null, null).await().indefinitely().getStatus());

        when(jwt.getClaim("id")).thenReturn("not-a-number");
        assertEquals(400, resource.updateUser(null, null, null).await().indefinitely().getStatus());

        verifyNoInteractions(userUseCases);
    }

    // Verifica a atualização com claim numérica e usuário encontrado.
    @Test
    void updateReturnsUpdatedUserForNumericIdClaim() {
        User updated = new User(42L, "Ana Silva", "ana@example.test", "hashed", "2026-11-26");
        when(jwt.getClaim("id")).thenReturn(42L);
        when(userUseCases.update(42L, "Ana Silva", null, null))
            .thenReturn(Uni.createFrom().item(Optional.of(updated)));

        Response response = resource.updateUser("Ana Silva", null, null).await().indefinitely();

        assertEquals(200, response.getStatus());
        assertSame(updated, response.getEntity());
        verify(userUseCases).update(42L, "Ana Silva", null, null);
    }

    // Verifica a conversão da claim textual e a resposta para usuário inexistente.
    @Test
    void updateReturnsNotFoundForMissingUserWithTextIdClaim() {
        when(jwt.getClaim("id")).thenReturn("42");
        when(userUseCases.update(42L, null, "ana@example.test", "secret"))
            .thenReturn(Uni.createFrom().item(Optional.empty()));

        Response response = resource.updateUser(null, "ana@example.test", "secret").await().indefinitely();

        assertEquals(404, response.getStatus());
        assertEquals("Usuário não encontrado", response.getEntity());
    }

    // Verifica que a exclusão rejeita identidade ausente, anônima ou sem JWT.
    @Test
    void deleteRejectsMissingAuthentication() {
        resource.securityIdentity = null;
        assertEquals(401, resource.deleteUser().await().indefinitely().getStatus());

        when(securityIdentity.isAnonymous()).thenReturn(true);
        assertEquals(401, resource.deleteUser().await().indefinitely().getStatus());

        when(securityIdentity.isAnonymous()).thenReturn(false);
        resource.jwt = null;
        assertEquals(401, resource.deleteUser().await().indefinitely().getStatus());

        verifyNoInteractions(userUseCases);
    }

    // Verifica que a exclusão rejeita claim de usuário ausente ou inválida.
    @Test
    void deleteRejectsMissingOrInvalidUserIdClaim() {
        when(jwt.getClaim("id")).thenReturn(null);
        assertEquals(401, resource.deleteUser().await().indefinitely().getStatus());

        when(jwt.getClaim("id")).thenReturn("not-a-number");
        assertEquals(400, resource.deleteUser().await().indefinitely().getStatus());

        verifyNoInteractions(userUseCases);
    }

    // Verifica a exclusão bem-sucedida com claim numérica.
    @Test
    void deleteReturnsNoContentForNumericIdClaim() {
        when(jwt.getClaim("id")).thenReturn(42L);
        when(userUseCases.delete(42L)).thenReturn(Uni.createFrom().item(true));

        Response response = resource.deleteUser().await().indefinitely();

        assertEquals(204, response.getStatus());
        verify(userUseCases).delete(42L);
    }

    // Verifica a conversão da claim textual e a resposta para usuário inexistente.
    @Test
    void deleteReturnsNotFoundForMissingUserWithTextIdClaim() {
        when(jwt.getClaim("id")).thenReturn("42");
        when(userUseCases.delete(42L)).thenReturn(Uni.createFrom().item(false));

        Response response = resource.deleteUser().await().indefinitely();

        assertEquals(404, response.getStatus());
        assertEquals("Usuário não encontrado", response.getEntity());
    }
}
