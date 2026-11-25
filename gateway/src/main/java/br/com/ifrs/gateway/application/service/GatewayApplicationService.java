package br.com.ifrs.gateway.application.service;

import java.time.LocalDate;

import br.com.ifrs.gateway.application.model.ForwardedResponse;
import br.com.ifrs.gateway.application.port.in.GatewayUseCases;
import br.com.ifrs.gateway.application.port.out.GastosServicePort;
import br.com.ifrs.gateway.application.port.out.UsersServicePort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GatewayApplicationService implements GatewayUseCases {

    private final UsersServicePort usersService;
    private final GastosServicePort gastosService;

    @Inject
    public GatewayApplicationService(UsersServicePort usersService, GastosServicePort gastosService) {
        this.usersService = usersService;
        this.gastosService = gastosService;
    }

    @Override
    public Uni<ForwardedResponse> login(String email, String password) {
        return usersService.login(email, password);
    }

    @Override
    public Uni<ForwardedResponse> createUser(String name, String email, String password) {
        return usersService.createUser(name, email, password);
    }

    @Override
    public Uni<ForwardedResponse> updateUser(String authorization, String name, String email, String password) {
        return usersService.updateUser(authorization, name, email, password);
    }

    @Override
    public Uni<ForwardedResponse> deleteUser(String authorization) {
        return usersService.deleteUser(authorization);
    }

    @Override
    public Uni<ForwardedResponse> createDespesa(
        String authorization,
        Double amount,
        String operation,
        String tag,
        LocalDate date
    ) {
        return gastosService.createDespesa(authorization, amount, operation, tag, date);
    }

    @Override
    public Uni<ForwardedResponse> getDespesas(
        String authorization,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    ) {
        return gastosService.getDespesas(authorization, operation, tag, dateStart, dateEnd);
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
        return gastosService.updateDespesa(authorization, id, amount, operation, tag, date);
    }

    @Override
    public Uni<ForwardedResponse> deleteDespesa(String authorization, Long id) {
        return gastosService.deleteDespesa(authorization, id);
    }

    @Override
    public Uni<ForwardedResponse> getTagSums(String authorization, String tag, String dateStart, String dateEnd) {
        return gastosService.getTagSums(authorization, tag, dateStart, dateEnd);
    }

    @Override
    public Uni<ForwardedResponse> getSaldo(String authorization) {
        return gastosService.getSaldo(authorization);
    }
}
