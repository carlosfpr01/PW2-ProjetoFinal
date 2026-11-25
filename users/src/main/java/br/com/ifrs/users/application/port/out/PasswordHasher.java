package br.com.ifrs.users.application.port.out;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String hashedPassword);
}
