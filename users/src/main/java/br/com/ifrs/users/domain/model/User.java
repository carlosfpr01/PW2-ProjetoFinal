package br.com.ifrs.users.domain.model;

public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String dataCriacao;

    public User() {
    }

    public User(String name, String email, String password) {
        this(null, name, email, password, null);
    }

    public User(Long id, String name, String email, String password, String dataCriacao) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }
    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

}