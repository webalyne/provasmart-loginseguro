package br.com.provasmart.auth.web;

import br.com.provasmart.auth.user.Role;

import jakarta.validation.constraints.*;

public class AdminUserForm {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotNull private Role role;
    private boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role value) {
        role = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }
}
