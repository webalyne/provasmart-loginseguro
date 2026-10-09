package br.com.provasmart.auth.security;

import br.com.provasmart.auth.user.*;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsService implements UserDetailsService {
    private final UserRepository users;

    public AccountDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var account =
                users.findByEmail(UserService.normalizeEmail(email))
                        .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .disabled(!account.isEnabled())
                .build();
    }
}
