package br.com.ifrs.gateway.adapter.out.restclient;

import java.time.LocalDate;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import br.com.ifrs.gateway.application.port.out.GastosServicePort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GastosServiceAdapter implements GastosServicePort {

    @Inject
    @RestClient
    GastosRestClient restClient;

    @Override
    public Uni<ForwardedResponse> createDespesa(
        String authorization,
        Double amount,
        String operation,
        String tag,
        LocalDate date
    ) {
        return RestClientResponseMapper.map(restClient.createDespesa(authorization, amount, operation, tag, date));
    }

    @Override
    public Uni<ForwardedResponse> getDespesas(
        String authorization,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    ) {
        return RestClientResponseMapper.map(
            restClient.getDespesas(authorization, operation, tag, dateStart, dateEnd)
        );
    }

    @Override
    public Uni<ForwardedResponse> updateDespesa(
        String authorization,
        Long id,
        Double amount,
        String operation,
        String tag,
        LocalDate date
    ) {
        return RestClientResponseMapper.map(
            restClient.updateDespesa(authorization, id, amount, operation, tag, date)
        );
    }

    @Override
    public Uni<ForwardedResponse> deleteDespesa(String authorization, Long id) {
        return RestClientResponseMapper.map(restClient.deleteDespesa(authorization, id));
    }

    @Override
    public Uni<ForwardedResponse> getTagSums(String authorization, String tag, String dateStart, String dateEnd) {
        return RestClientResponseMapper.map(restClient.getTagSums(authorization, tag, dateStart, dateEnd));
    }

    @Override
    public Uni<ForwardedResponse> getSaldo(String authorization) {
        return RestClientResponseMapper.map(restClient.getSaldo(authorization));
    }
}
