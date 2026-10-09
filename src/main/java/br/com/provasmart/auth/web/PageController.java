package br.com.provasmart.auth.web;

import br.com.provasmart.auth.user.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class PageController {
    private final UserService users;

    public PageController(UserService users) {
        this.users = users;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String register(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/cadastro")
    public String register(
            @Valid @ModelAttribute("form") RegistrationForm form, BindingResult errors) {
        if (errors.hasErrors()) {
            form.setPassword(null);
            form.setConfirmPassword(null);
            return "register";
        }
        try {
            users.register(form.getName(), form.getEmail(), form.getPassword());
        } catch (AccountException ex) {
            errors.reject("registration", ex.getMessage());
            form.setPassword(null);
            form.setConfirmPassword(null);
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/painel")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("account", users.getByEmail(principal.getName()));
        return "dashboard";
    }

    @GetMapping("/perfil")
    public String profile(Principal principal, Model model) {
        model.addAttribute("account", users.getByEmail(principal.getName()));
        return "profile";
    }

    @GetMapping("/professor/painel")
    public String teacher(Model model) {
        model.addAttribute("area", "Professor");
        model.addAttribute("description", "Página disponível apenas para professores.");
        return "role";
    }

    @GetMapping("/estudante/painel")
    public String student(Model model) {
        model.addAttribute("area", "Estudante");
        model.addAttribute("description", "Página disponível apenas para estudantes.");
        return "role";
    }

    @GetMapping("/acesso-negado")
    public String denied() {
        return "error/403";
    }
}
