package br.com.ifrs.gateway.adapter.out.restclient;

import br.com.ifrs.gateway.application.port.out.UsersServicePort;
import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@ApplicationScoped
public class UsersServiceAdapter implements UsersServicePort {

    @Inject
    @RestClient
    UsersRestClient restClient;

    @Override
    public Uni<ForwardedResponse> login(String email, String password) {
        return forward(restClient.login(email, password));
    }

    @Override
    public Uni<ForwardedResponse> createUser(String name, String email, String password) {
        return forward(restClient.createUser(name, email, password));
    }

    @Override
    public Uni<ForwardedResponse> updateUser(String authorization, String name, String email, String password) {
        return forward(restClient.updateUser(authorization, name, email, password));
    }

    @Override
    public Uni<ForwardedResponse> deleteUser(String authorization) {
        return forward(restClient.deleteUser(authorization));
    }

    private Uni<ForwardedResponse> forward(Uni<Response> upstream) {
        return RestClientResponseMapper.map(upstream)
            .onFailure(ClientWebApplicationException.class)
            .recoverWithItem(exception ->
                RestClientResponseMapper.fromClientError(
                    ((ClientWebApplicationException) exception).getResponse()
                )
            );
    }
}
