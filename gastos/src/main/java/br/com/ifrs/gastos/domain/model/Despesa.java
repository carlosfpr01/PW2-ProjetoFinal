package br.com.ifrs.gastos.domain.model;

import java.time.LocalDate;

public class Despesa {

    private Long id;
    private Long idUser;
    private Double amount;

    public enum operations{
        D,
        C
    }
    private operations operation;

    private String tag;
    private LocalDate date;

    public Despesa() {}

    public Despesa(Long id, Long idUser, Double amount, operations operation, String tag, LocalDate date) {
        this.id = id;
        this.idUser = idUser;
        this.amount = amount;
        this.operation = operation;
        this.tag = tag;
        this.date = date;
    }

    public Despesa(Long idUser, Double amount, operations operation, String tag, LocalDate date) {
        this(null, idUser, amount, operation, tag, date);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public operations operationValue() {
        return operation;
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
}
