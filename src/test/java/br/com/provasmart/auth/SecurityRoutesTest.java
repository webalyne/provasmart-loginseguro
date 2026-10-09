package br.com.provasmart.auth;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.provasmart.auth.config.*;
import br.com.provasmart.auth.security.*;
import br.com.provasmart.auth.user.*;
import br.com.provasmart.auth.web.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

@WebMvcTest({PageController.class, AdminController.class})
@Import({SecurityConfig.class, AccountDetailsService.class, ThemeAdvice.class})
class SecurityRoutesTest {
    @Autowired MockMvc mvc;
    @MockitoBean UserRepository repo;
    @MockitoBean UserService service;

    @BeforeEach
    void setup() {
        for (var role : Role.values()) {
            var email = role.name().toLowerCase() + "@example.com";
            var account = new UserAccount("Pessoa", email, "hash", role);
            when(repo.findByEmail(email)).thenReturn(Optional.of(account));
        }
    }

    @Test
    void anonymousIsRedirectedFromProtectedPage() throws Exception {
        mvc.perform(get("/painel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginAndRegistrationRenderWithCsrfAndNoPasswordValues() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
        mvc.perform(get("/cadastro")).andExpect(status().isOk());
    }

    @Test
    void studentCannotReadAdminOrTeacherRoutes() throws Exception {
        mvc.perform(get("/admin/usuarios").with(user("estudante@example.com").roles("ESTUDANTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/professor/painel").with(user("estudante@example.com").roles("ESTUDANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCanReadTeacherAreaButNotStudentArea() throws Exception {
        mvc.perform(get("/professor/painel").with(user("professor@example.com").roles("PROFESSOR")))
                .andExpect(status().isOk());
        mvc.perform(get("/estudante/painel").with(user("professor@example.com").roles("PROFESSOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/cadastro")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void mismatchPreventsRegistration() throws Exception {
        mvc.perform(
                        post("/cadastro")
                                .with(csrf())
                                .param("name", "Maria")
                                .param("email", "maria@example.com")
                                .param("password", "uma senha longa 123")
                                .param("confirmPassword", "outra senha longa 123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
        verify(service, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void ignoresPublicRoleInjection() throws Exception {
        mvc.perform(
                        post("/cadastro")
                                .with(csrf())
                                .param("name", "Maria")
                                .param("email", "maria@example.com")
                                .param("password", "uma senha longa 123")
                                .param("confirmPassword", "uma senha longa 123")
                                .param("role", "ADMIN"))
                .andExpect(redirectedUrl("/login?registered"));
        verify(service).register("Maria", "maria@example.com", "uma senha longa 123");
    }

    @Test
    void logoutRequiresPostAndCsrf() throws Exception {
        mvc.perform(post("/logout").with(user("estudante@example.com").roles("ESTUDANTE")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/logout")
                                .with(user("estudante@example.com").roles("ESTUDANTE"))
                                .with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void disabledAccountLosesExistingSession() throws Exception {
        var account = new UserAccount("Pessoa", "estudante@example.com", "hash", Role.ESTUDANTE);
        account.setEnabled(false);
        when(repo.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        mvc.perform(get("/estudante/painel").with(user(account.getEmail()).roles("ESTUDANTE")))
                .andExpect(redirectedUrl("/login?expired"));
    }

    @Test
    void staleRoleAndDeletedAccountLoseExistingSession() throws Exception {
        mvc.perform(get("/admin/usuarios").with(user("estudante@example.com").roles("ADMIN")))
                .andExpect(redirectedUrl("/login?expired"));
        mvc.perform(get("/painel").with(user("deleted@example.com").roles("ESTUDANTE")))
                .andExpect(redirectedUrl("/login?expired"));
    }
}
