package br.com.ifrs.gastos.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.smallrye.mutiny.Uni;

public interface GastosUseCases {

    Uni<Despesa> create(Long userId, Double amount, Despesa.operations operation, String tag, LocalDate date);

    Uni<List<Despesa>> getDespesasByFilters(
        Long userId,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    );

    Uni<Despesa> update(Long id, Double amount, Despesa.operations operation, String tag, LocalDate date);

    Uni<Optional<Despesa>> delete(Long id);

    Uni<List<TagSum>> getTagsByUserId(Long userId, String dateStart, String dateEnd);

    Uni<Double> calculateSaldoByUserId(Long userId);
}
