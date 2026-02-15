package com.vbforge.security.websec.controller;

import com.vbforge.security.websec.dto.ProductDTO;
import com.vbforge.security.websec.dto.TagDTO;
import com.vbforge.security.websec.service.ProductService;
import com.vbforge.security.websec.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web Controller for rendering HTML pages with Thymeleaf.
 *
 * This is different from REST controllers - these return view names,
 * not JSON responses.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final ProductService productService;
    private final TagService tagService;

    /**
     * Home page (public)
     */
    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "home";
    }

    /**
     * Custom login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    /**
     * Products page (requires authentication)
     */
    @GetMapping("/products")
    public String products(Model model, Authentication authentication) {
        List<ProductDTO> products = productService.getAllProducts();
        List<TagDTO> tags = tagService.getAllTags();

        model.addAttribute("products", products);
        model.addAttribute("tags", tags);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        return "products";
    }

    /**
     * Admin page (requires ADMIN role)
     */
    @GetMapping("/admin")
    public String admin(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin";
    }

    /**
     * Access denied page (403)
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "access-denied";
    }


}
