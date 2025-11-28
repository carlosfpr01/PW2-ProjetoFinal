package run.gastos.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TagSum {
    private String tag;
    private Double total;

    public TagSum(String tag, Double total) {
        this.tag = tag;
        this.total = total;
    }

    public String getTag() {
        return tag;
    }

    public Double getTotal() {
        return total;
    }

    public static io.smallrye.mutiny.Uni<List<TagSum>> getTagsByUserId(Long userId, String dateStart, String dateEnd) {
        // Retorna apenas soma de valores com operação C (Crédito/Saída)
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT d.tag, COALESCE(SUM(d.amount), 0) " +
            "FROM Despesa d WHERE d.idUser = :userId AND d.operation = D"
        );

        if (dateStart != null) {
            queryBuilder.append(" AND d.date >= :dateStart");
        }
        if (dateEnd != null) {
            queryBuilder.append(" AND d.date <= :dateEnd");
        }

        queryBuilder.append(" GROUP BY d.tag");

        String query = queryBuilder.toString();

        return io.quarkus.hibernate.reactive.panache.Panache.getSession()
            .onItem().transformToUni(session -> {
                var q = session.createQuery(query, Object[].class)
                    .setParameter("userId", userId);

                if (dateStart != null) {
                    q.setParameter("dateStart", LocalDate.parse(dateStart));
                }
                if (dateEnd != null) {
                    q.setParameter("dateEnd", LocalDate.parse(dateEnd));
                }

                return q.getResultList();
            })
            .onItem().transform(rows -> rows.stream()
                .map(arr -> {
                    String tag = (String) arr[0];
                    Number n = (Number) arr[1];
                    Double total = n == null ? 0.0 : n.doubleValue();
                    return new TagSum(tag, total);
                })
                .collect(Collectors.toList())
            );
    }
}