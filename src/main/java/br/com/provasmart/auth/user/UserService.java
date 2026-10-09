package br.com.provasmart.auth.user;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public UserAccount register(String name, String email, String password) {
        validatePassword(password);
        var normalized = normalizeEmail(email);
        if (users.existsByEmail(normalized))
            throw new AccountException("Não foi possível cadastrar com este e-mail.");
        try {
            return users.save(
                    new UserAccount(
                            name.strip(), normalized, encoder.encode(password), Role.ESTUDANTE));
        } catch (DuplicateKeyException ex) {
            throw new AccountException("Não foi possível cadastrar com este e-mail.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null
                || password.length() < 12
                || password.length() > 64
                || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AccountException(
                    "A senha deve ter de 12 a 64 caracteres e até 72 bytes em UTF-8.");
    }

    public UserAccount getByEmail(String email) {
        return users.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new AccountException("Conta não encontrada."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAccount> list() {
        return users.findAll(Sort.by("name"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserAccount get(String id) {
        return users.findById(id).orElseThrow(() -> new AccountException("Conta não encontrada."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void update(String id, String name, Role role, boolean enabled, String actorEmail) {
        var target = get(id);
        if (target.getEmail().equals(actorEmail))
            throw new AccountException("Você não pode alterar sua própria conta nesta tela.");
        if (role == null) throw new AccountException("Selecione um perfil válido.");
        target.setName(name.strip());
        target.setRole(role);
        target.setEnabled(enabled);
        try {
            users.save(target);
        } catch (OptimisticLockingFailureException ex) {
            throw new AccountException(
                    "A conta foi alterada por outra operação. Atualize a página.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id, String actorEmail) {
        var target = get(id);
        if (target.getEmail().equals(actorEmail))
            throw new AccountException("Você não pode excluir sua própria conta.");
        users.delete(target);
    }
}
