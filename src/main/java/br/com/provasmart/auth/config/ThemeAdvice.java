package br.com.provasmart.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ThemeAdvice {
    @Value("${app.brand:ProvaSmart Auth}")
    private String brand;

    @Value("${app.theme:provasmart}")
    private String theme;

    @ModelAttribute("brand")
    public String brand() {
        return brand;
    }

    @ModelAttribute("themeCss")
    public String themeCss() {
        return "/css/themes/" + (theme.equals("neutral") ? "neutral" : "provasmart") + ".css";
    }
}
