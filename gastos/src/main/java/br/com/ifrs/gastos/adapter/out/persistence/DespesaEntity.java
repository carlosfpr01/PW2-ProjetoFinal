package br.com.ifrs.gastos.adapter.out.persistence;

import java.time.LocalDate;

import br.com.ifrs.gastos.domain.model.Despesa;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "Despesa")
@Table(name = "Despesa")
public class DespesaEntity extends PanacheEntity {

    @Column(nullable = false)
    public Long idUser;

    @Column(nullable = false)
    public Double amount;

    @Column(nullable = false)
    public Despesa.operations operation;

    @Column(nullable = true)
    public String tag;

    @Column(nullable = false)
    public LocalDate date;

    public Despesa toDomain() {
        return new Despesa(id, idUser, amount, operation, tag, date);
    }

    public static DespesaEntity fromDomain(Despesa despesa) {
        DespesaEntity entity = new DespesaEntity();
        entity.id = despesa.getId();
        entity.idUser = despesa.getIdUser();
        entity.amount = despesa.getAmount();
        entity.operation = despesa.operationValue();
        entity.tag = despesa.getTag();
        entity.date = despesa.getDate();
        return entity;
    }
}
