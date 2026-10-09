package br.com.provasmart.auth.config;

import br.com.provasmart.auth.security.AccountStatusFilter;
import br.com.provasmart.auth.user.UserRepository;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http, UserRepository users) throws Exception {
        http.authorizeHttpRequests(
                        a ->
                                a.requestMatchers("/", "/login", "/cadastro", "/css/**", "/error")
                                        .permitAll()
                                        .requestMatchers("/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/professor/**")
                                        .hasRole("PROFESSOR")
                                        .requestMatchers("/estudante/**")
                                        .hasRole("ESTUDANTE")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        f ->
                                f.loginPage("/login")
                                        .usernameParameter("email")
                                        .defaultSuccessUrl("/painel", true)
                                        .failureUrl("/login?error")
                                        .permitAll())
                .logout(
                        l ->
                                l.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                                        .logoutSuccessUrl("/login?logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("SESSION"))
                .sessionManagement(s -> s.sessionFixation(f -> f.changeSessionId()))
                .exceptionHandling(e -> e.accessDeniedPage("/acesso-negado"))
                .addFilterBefore(new AccountStatusFilter(users), AuthorizationFilter.class);
        return http.build();
    }
}
