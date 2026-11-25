package br.com.ifrs.gateway.application.port.out;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;

public interface UsersServicePort {

    Uni<ForwardedResponse> login(String email, String password);

    Uni<ForwardedResponse> createUser(String name, String email, String password);

    Uni<ForwardedResponse> updateUser(String authorization, String name, String email, String password);

    Uni<ForwardedResponse> deleteUser(String authorization);
}
