package br.com.provasmart.auth.security;

import br.com.provasmart.auth.user.UserRepository;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AccountStatusFilter extends OncePerRequestFilter {
    private final UserRepository users;

    public AccountStatusFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            var account = users.findByEmail(auth.getName());
            boolean valid =
                    account.isPresent()
                            && account.get().isEnabled()
                            && auth.getAuthorities().stream()
                                    .anyMatch(
                                            a ->
                                                    a.getAuthority()
                                                            .equals(
                                                                    "ROLE_"
                                                                            + account.get()
                                                                                    .getRole()
                                                                                    .name()));
            if (!valid) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?expired");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
