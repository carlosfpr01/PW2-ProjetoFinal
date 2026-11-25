package br.com.ifrs.gastos.application.port.out;

import java.util.List;
import java.util.Optional;

import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.smallrye.mutiny.Uni;

public interface DespesaRepository {

    Uni<Despesa> save(Despesa despesa);

    Uni<Optional<Despesa>> findById(Long id);

    Uni<Optional<Despesa>> deleteById(Long id);

    Uni<List<Despesa>> findByFilters(
        Long userId,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    );

    Uni<List<TagSum>> sumByTag(Long userId, String dateStart, String dateEnd);

    Uni<Double> calculateBalance(Long userId);
}
