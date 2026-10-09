package br.com.provasmart.auth.web;

import jakarta.validation.constraints.*;

public class RegistrationForm {
    @NotBlank(message = "Informe seu nome.")
    @Size(min = 2, max = 100, message = "O nome deve ter de 2 a 100 caracteres.")
    private String name;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 12, max = 64, message = "Use de 12 a 64 caracteres.")
    private String password;

    @NotBlank(message = "Confirme a senha.")
    private String confirmPassword;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password = value;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String value) {
        confirmPassword = value;
    }

    @AssertTrue(message = "As senhas devem ser iguais.")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "A senha deve ter no máximo 72 bytes em UTF-8.")
    public boolean isPasswordByteLengthValid() {
        return password == null
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }
}
