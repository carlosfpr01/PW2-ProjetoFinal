package br.com.ifrs.gastos.domain.model;

public class TagSum {
    private String tag;
    private Double total;

    public TagSum() {
    }

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
}