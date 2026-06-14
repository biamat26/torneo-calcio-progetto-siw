package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Credentials;
import it.uniroma3.siw.torneo_calcio.model.User;
import it.uniroma3.siw.torneo_calcio.service.CredentialsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

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
            return "redirect:/login";
        }
        return "authentication/registerUser";
    }
}
