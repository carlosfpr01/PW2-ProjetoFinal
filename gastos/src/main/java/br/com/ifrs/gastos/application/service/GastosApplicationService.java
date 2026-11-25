package br.com.ifrs.gastos.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.ifrs.gastos.application.port.in.GastosUseCases;
import br.com.ifrs.gastos.application.port.out.DespesaRepository;
import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GastosApplicationService implements GastosUseCases {

    private final DespesaRepository despesaRepository;

    @Inject
    public GastosApplicationService(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    @Override
    @WithTransaction
    public Uni<Despesa> create(
        Long userId,
        Double amount,
        Despesa.operations operation,
        String tag,
        LocalDate date
    ) {
        return despesaRepository.save(new Despesa(userId, amount, operation, tag, date));
    }

    @Override
    @WithTransaction
    public Uni<List<Despesa>> getDespesasByFilters(
        Long userId,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    ) {
        return despesaRepository.findByFilters(userId, operation, tag, dateStart, dateEnd);
    }

    @Override
    @WithTransaction
    public Uni<Despesa> update(
        Long id,
        Double amount,
        Despesa.operations operation,
        String tag,
        LocalDate date
    ) {
        return despesaRepository.findById(id).flatMap(found -> {
            if (found.isEmpty()) {
                return Uni.createFrom().nullItem();
            }

            Despesa despesa = found.get();
            if (amount != null) {
                despesa.setAmount(amount);
            }
            if (operation != null) {
                despesa.setOperation(operation);
            }
            if (tag != null && !tag.trim().isEmpty()) {
                despesa.setTag(tag);
            }
            if (date != null) {
                despesa.setDate(date);
            }
            return despesaRepository.save(despesa);
        });
    }

    @Override
    @WithTransaction
    public Uni<Optional<Despesa>> delete(Long id) {
        return despesaRepository.deleteById(id);
    }

    @Override
    @WithTransaction
    public Uni<List<TagSum>> getTagsByUserId(Long userId, String dateStart, String dateEnd) {
        return despesaRepository.sumByTag(userId, dateStart, dateEnd);
    }

    @Override
    @WithTransaction
    public Uni<Double> calculateSaldoByUserId(Long userId) {
        return despesaRepository.calculateBalance(userId);
    }
}
