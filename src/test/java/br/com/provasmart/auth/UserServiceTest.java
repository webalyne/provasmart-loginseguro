package br.com.provasmart.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.provasmart.auth.user.*;

import org.junit.jupiter.api.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

class UserServiceTest {
    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        service = new UserService(repo, new BCryptPasswordEncoder(4));
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndForcesStudent() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var user =
                service.register("  Maria Silva  ", "  MARIA@EXAMPLE.COM  ", "uma senha longa 123");
        assertThat(user.getName()).isEqualTo("Maria Silva");
        assertThat(user.getEmail()).isEqualTo("maria@example.com");
        assertThat(user.getRole()).isEqualTo(Role.ESTUDANTE);
        assertThat(user.getPasswordHash()).isNotEqualTo("uma senha longa 123");
        assertThat(
                        new BCryptPasswordEncoder()
                                .matches("uma senha longa 123", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    void rejectsDuplicateEmailIncludingConcurrentInsert() {
        when(repo.save(any())).thenThrow(new DuplicateKeyException("email"));
        assertThatThrownBy(
                        () -> service.register("Maria", "maria@example.com", "uma senha longa 123"))
                .isInstanceOf(AccountException.class);
    }

    @Test
    void rejectsShortPasswordAndUtf8Overflow() {
        assertThatThrownBy(() -> service.register("Maria", "maria@example.com", "curta"))
                .isInstanceOf(AccountException.class);
        assertThatThrownBy(() -> service.register("Maria", "maria@example.com", "á".repeat(40)))
                .isInstanceOf(AccountException.class);
        verifyNoInteractions(repo);
    }

    @Test
    void adminCannotDisableOrDeleteOwnAccount() {
        var admin = new UserAccount("Admin", "admin@example.com", "hash", Role.ADMIN);
        admin.setId("1");
        when(repo.findById("1")).thenReturn(Optional.of(admin));
        assertThatThrownBy(
                        () ->
                                service.update(
                                        "1", "Admin", Role.ESTUDANTE, false, "admin@example.com"))
                .isInstanceOf(AccountException.class);
        assertThatThrownBy(() -> service.delete("1", "admin@example.com"))
                .isInstanceOf(AccountException.class);
        verify(repo, never()).save(any());
        verify(repo, never()).delete(any(UserAccount.class));
    }

    @Test
    void adminCanChangeRoleAndDisableAnotherAccount() {
        var user = new UserAccount("Maria", "maria@example.com", "hash", Role.ESTUDANTE);
        when(repo.findById("2")).thenReturn(Optional.of(user));
        service.update("2", "Maria", Role.PROFESSOR, false, "admin@example.com");
        assertThat(user.getRole()).isEqualTo(Role.PROFESSOR);
        assertThat(user.isEnabled()).isFalse();
        verify(repo).save(user);
    }
}
