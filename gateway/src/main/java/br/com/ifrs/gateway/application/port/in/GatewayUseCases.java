package br.com.ifrs.gateway.application.port.in;

import java.time.LocalDate;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import io.smallrye.mutiny.Uni;

public interface GatewayUseCases {

    Uni<ForwardedResponse> login(String email, String password);

    Uni<ForwardedResponse> createUser(String name, String email, String password);

    Uni<ForwardedResponse> updateUser(String authorization, String name, String email, String password);

    Uni<ForwardedResponse> deleteUser(String authorization);

    Uni<ForwardedResponse> createDespesa(
        String authorization,
        Double amount,
        String operation,
        String tag,
        LocalDate date
    );

    Uni<ForwardedResponse> getDespesas(
        String authorization,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    );

    Uni<ForwardedResponse> updateDespesa(
        String authorization,
        Long id,
        Double amount,
        String operation,
        String tag,
        LocalDate date
    );

    Uni<ForwardedResponse> deleteDespesa(String authorization, Long id);

    Uni<ForwardedResponse> getTagSums(String authorization, String tag, String dateStart, String dateEnd);

    Uni<ForwardedResponse> getSaldo(String authorization);
}
