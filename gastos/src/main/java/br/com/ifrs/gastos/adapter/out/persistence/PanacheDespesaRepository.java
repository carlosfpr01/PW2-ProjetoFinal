package br.com.ifrs.gastos.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.ifrs.gastos.application.port.out.DespesaRepository;
import br.com.ifrs.gastos.domain.model.Despesa;
import br.com.ifrs.gastos.domain.model.TagSum;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PanacheDespesaRepository implements DespesaRepository {

    @Override
    public Uni<Despesa> save(Despesa despesa) {
        if (despesa.getId() == null) {
            DespesaEntity entity = DespesaEntity.fromDomain(despesa);
            return entity.persistAndFlush().map(ignored -> entity.toDomain());
        }

        return DespesaEntity.<DespesaEntity>findById(despesa.getId())
            .onItem().ifNull().failWith(() -> new IllegalStateException("Despesa not found: " + despesa.getId()))
            .invoke(entity -> {
                entity.idUser = despesa.getIdUser();
                entity.amount = despesa.getAmount();
                entity.operation = despesa.operationValue();
                entity.tag = despesa.getTag();
                entity.date = despesa.getDate();
            })
            .call(DespesaEntity::persistAndFlush)
            .map(DespesaEntity::toDomain);
    }

    @Override
    public Uni<Optional<Despesa>> findById(Long id) {
        return DespesaEntity.<DespesaEntity>findById(id)
            .map(entity -> Optional.ofNullable(entity).map(DespesaEntity::toDomain));
    }

    @Override
    public Uni<Optional<Despesa>> deleteById(Long id) {
        return DespesaEntity.<DespesaEntity>findById(id)
            .onItem().ifNotNull().transformToUni(entity ->
                entity.delete().replaceWith(Optional.of(entity.toDomain())))
            .onItem().ifNull().continueWith(Optional.empty());
    }

    @Override
    public Uni<List<Despesa>> findByFilters(
        Long userId,
        String operation,
        String tag,
        String dateStart,
        String dateEnd
    ) {
        StringBuilder query = new StringBuilder("SELECT d FROM Despesa d WHERE d.idUser = :userId");
        if (operation != null && !operation.trim().isEmpty()) {
            query.append(" AND d.operation = :operation");
        }
        if (tag != null && !tag.trim().isEmpty()) {
            query.append(" AND d.tag = :tag");
        }
        if (dateStart != null) {
            query.append(" AND d.date >= :dateStart");
        }
        if (dateEnd != null) {
            query.append(" AND d.date <= :dateEnd");
        }

        return Panache.getSession().onItem().transformToUni(session -> {
            var statement = session.createQuery(query.toString(), DespesaEntity.class)
                .setParameter("userId", userId);
            if (operation != null && !operation.trim().isEmpty()) {
                statement.setParameter("operation", Despesa.operations.valueOf(operation.trim().toUpperCase()));
            }
            if (tag != null && !tag.trim().isEmpty()) {
                statement.setParameter("tag", tag);
            }
            if (dateStart != null) {
                statement.setParameter("dateStart", LocalDate.parse(dateStart));
            }
            if (dateEnd != null) {
                statement.setParameter("dateEnd", LocalDate.parse(dateEnd));
            }
            return statement.getResultList();
        }).map(entities -> entities.stream().map(DespesaEntity::toDomain).collect(Collectors.toList()));
    }

    @Override
    public Uni<List<TagSum>> sumByTag(Long userId, String dateStart, String dateEnd) {
        StringBuilder query = new StringBuilder(
            "SELECT d.tag, COALESCE(SUM(d.amount), 0) FROM Despesa d " +
                "WHERE d.idUser = :userId AND d.operation = :operation"
        );
        if (dateStart != null) {
            query.append(" AND d.date >= :dateStart");
        }
        if (dateEnd != null) {
            query.append(" AND d.date <= :dateEnd");
        }
        query.append(" GROUP BY d.tag");

        return Panache.getSession().onItem().transformToUni(session -> {
            var statement = session.createQuery(query.toString(), Object[].class)
                .setParameter("userId", userId)
                .setParameter("operation", Despesa.operations.D);
            if (dateStart != null) {
                statement.setParameter("dateStart", LocalDate.parse(dateStart));
            }
            if (dateEnd != null) {
                statement.setParameter("dateEnd", LocalDate.parse(dateEnd));
            }
            return statement.getResultList();
        }).map(rows -> rows.stream().map(row -> {
            Number total = (Number) row[1];
            return new TagSum((String) row[0], total == null ? 0.0 : total.doubleValue());
        }).collect(Collectors.toList()));
    }

    @Override
    public Uni<Double> calculateBalance(Long userId) {
        String query = "SELECT COALESCE(SUM(CASE WHEN d.operation = :credit THEN d.amount " +
            "WHEN d.operation = :debit THEN -d.amount ELSE 0 END), 0) " +
            "FROM Despesa d WHERE d.idUser = :userId";
        return Panache.getSession().onItem().transformToUni(session ->
            session.createQuery(query, Double.class)
                .setParameter("credit", Despesa.operations.C)
                .setParameter("debit", Despesa.operations.D)
                .setParameter("userId", userId)
                .getSingleResult()
        );
    }
}
