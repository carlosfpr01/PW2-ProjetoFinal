package run.gastos.model;

import java.time.LocalDate;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;


@Entity
public class Despesa extends PanacheEntity {

    @Column(nullable = false)
    private Long idUser;

    @Column(nullable = false)
    private Double amount;

    public enum operations{
        D,
        C
    }
    @Column(nullable = false)
    private operations operation;

    @Column(nullable = true)
    private String tag;

    @Column(nullable = false)
    private LocalDate date;

    public Despesa() {}
    public Despesa(Long idUser, Double amount, operations operation, String tag, LocalDate date) {
        this.idUser = idUser;
        this.amount = amount;
        this.operation = operation;
        this.tag = tag;
        this.date = date;
    }

    public Long getIdUser() {
        return idUser;
    }
    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getOperation() {
        return operation.name();
    }
    public void setOperation(operations operation) {
        this.operation = operation;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public static io.smallrye.mutiny.Uni<java.util.List<Despesa>> getDespesasByFilters(Long userId, String operation, String tag, String dateStart, String dateEnd) {
        StringBuilder queryBuilder = new StringBuilder("SELECT d FROM Despesa d WHERE d.idUser = :userId");
        
        if (operation != null && !operation.trim().isEmpty()) {
            queryBuilder.append(" AND d.operation = :operation");
        }
        if (tag != null && !tag.trim().isEmpty()) {
            queryBuilder.append(" AND d.tag = :tag");
        }
        if (dateStart != null) {
            queryBuilder.append(" AND d.date >= :dateStart");
        }
        if (dateEnd != null) {
            queryBuilder.append(" AND d.date <= :dateEnd");
        }

        String query = queryBuilder.toString();
        
        return io.quarkus.hibernate.reactive.panache.Panache.getSession()
            .onItem().transformToUni(session -> {
                var q = session.createQuery(query, Despesa.class)
                    .setParameter("userId", userId);
                
                if (operation != null && !operation.trim().isEmpty()) {
                    q.setParameter("operation", Despesa.operations.valueOf(operation.trim().toUpperCase()));
                }
                if (tag != null && !tag.trim().isEmpty()) {
                    q.setParameter("tag", tag);
                }
                if (dateStart != null) {
                    q.setParameter("dateStart", LocalDate.parse(dateStart));
                }
                if (dateEnd != null) {
                    q.setParameter("dateEnd", LocalDate.parse(dateEnd));
                }
                
                return q.getResultList();
            });
    }

    public static io.smallrye.mutiny.Uni<Double> calculateSaldoByUserId(Long userId) {
        String query = "SELECT COALESCE(SUM(CASE WHEN d.operation = run.gastos.model.Despesa$operations.C THEN d.amount " +
                       "WHEN d.operation = run.gastos.model.Despesa$operations.D THEN -d.amount ELSE 0 END), 0) " +
                       "FROM Despesa d WHERE d.idUser = :userId";
        return io.quarkus.hibernate.reactive.panache.Panache.getSession()
            .onItem().transformToUni(session ->
                session.createQuery(query, Double.class)
                    .setParameter("userId", userId)
                    .getSingleResult()
            );
    }

}
