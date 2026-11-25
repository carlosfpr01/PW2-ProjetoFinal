package br.com.ifrs.users.application.port.out;

import br.com.ifrs.users.domain.model.User;

public interface TokenIssuer {

    String issue(User user);
}
