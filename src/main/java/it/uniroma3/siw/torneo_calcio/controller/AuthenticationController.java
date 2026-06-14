package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Credentials;
import it.uniroma3.siw.torneo_calcio.model.User;
import it.uniroma3.siw.torneo_calcio.service.CredentialsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

import java.util.List;

@Controller
public class AuthenticationController {

    private final CredentialsService credentialsService;

    public AuthenticationController(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @GetMapping(value="/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "authentication/registerUser";
    }

    @GetMapping(value = "/login")
    public String showLogin(Model model){
        return "authentication/login";
    }

    @GetMapping(value = "/admin/index")
    public String index(){
        return "admin/index";
    }

    @PostMapping(value={"/register"})
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult userBindingResult,
                               @Valid @ModelAttribute("credentials") Credentials credentials,
                               BindingResult credentialsBindingResult) {
        // !userBindingResult.hasErrors(): true if User si valid
        // !credentialsBindingResult.hasErrors(): true if Credentials is valid
        if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            credentials.setUser(user);
            Credentials credentialsSaved = credentialsService.saveCredentials(credentials);
            // auto-login
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            credentialsSaved.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority(credentialsSaved.getRole()))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/";
        }
        return "authentication/registerUser";
    }
}
