package br.com.provasmart.auth.web;

import br.com.provasmart.auth.user.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminController {
    private final UserService users;

    public AdminController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.list());
        return "admin/users";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable String id, Model model) {
        var user = users.get(id);
        var form = new AdminUserForm();
        form.setName(user.getName());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());
        model.addAttribute("form", form);
        model.addAttribute("account", user);
        model.addAttribute("roles", Role.values());
        return "admin/edit";
    }

    @PostMapping("/{id}/editar")
    public String update(
            @PathVariable String id,
            @Valid @ModelAttribute("form") AdminUserForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                users.update(
                        id, form.getName(), form.getRole(), form.isEnabled(), principal.getName());
                redirect.addFlashAttribute("success", "Conta atualizada.");
                return "redirect:/admin/usuarios";
            } catch (AccountException ex) {
                errors.reject("update", ex.getMessage());
            }
        }
        model.addAttribute("account", users.get(id));
        model.addAttribute("roles", Role.values());
        return "admin/edit";
    }

    @PostMapping("/{id}/excluir")
    public String delete(
            @PathVariable String id, Principal principal, RedirectAttributes redirect) {
        try {
            users.delete(id, principal.getName());
            redirect.addFlashAttribute("success", "Conta excluída.");
        } catch (AccountException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @ExceptionHandler(AccountException.class)
    public String accountError(AccountException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/usuarios";
    }
}
