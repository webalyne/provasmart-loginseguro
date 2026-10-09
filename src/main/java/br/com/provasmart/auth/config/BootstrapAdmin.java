package br.com.provasmart.auth.config;

import br.com.provasmart.auth.user.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapAdmin implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap.email}")
    private String email;

    @Value("${app.bootstrap.password}")
    private String password;

    public BootstrapAdmin(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserService.validatePassword(password);
        var normalized = UserService.normalizeEmail(email);
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalStateException(
                    "Configure um e-mail válido para o administrador inicial.");
        var existing = users.findByEmail(normalized);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ADMIN || !existing.get().isEnabled())
                throw new IllegalStateException(
                        "O e-mail de inicialização pertence a uma conta sem acesso"
                            + " administrativo.");
            return;
        }
        users.save(
                new UserAccount("Administrador", normalized, encoder.encode(password), Role.ADMIN));
    }
}
