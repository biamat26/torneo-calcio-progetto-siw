package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Credentials;
import it.uniroma3.siw.torneo_calcio.model.User;
import it.uniroma3.siw.torneo_calcio.service.CredentialsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
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


    /**
     * Gestisce la registrazione di un nuovo utente e ne esegue il login automatico.
     *
     * Salva la password in chiaro prima della codifica BCrypt, poi persiste
     * l'utente e le credenziali. Se la validazione ha successo, autentica
     * immediatamente l'utente tramite {@code request.login()} senza richiedere
     * un secondo accesso manuale.
     *
     * @param user                    i dati anagrafici del nuovo utente (nome, cognome, email)
     * @param userBindingResult       risultato della validazione su {@code user}
     * @param credentials             le credenziali di accesso (username e password)
     * @param credentialsBindingResult risultato della validazione su {@code credentials}
     * @param request                 la richiesta HTTP corrente, usata per il login automatico
     * @return redirect alla home se la registrazione va a buon fine,
     *         altrimenti ritorna al form di registrazione con gli errori
     * @throws ServletException se {@code request.login()} fallisce
     */
    @PostMapping(value = "/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult userBindingResult,
                               @Valid @ModelAttribute("credentials") Credentials credentials,
                               BindingResult credentialsBindingResult,
                               HttpServletRequest request) throws ServletException {

        if (!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            String rawPassword = credentials.getPassword();
            credentials.setUser(user);
            credentialsService.saveCredentials(credentials);

            if (request.getUserPrincipal() != null) {
                request.logout();
            }
            request.login(credentials.getUsername(), rawPassword);

            return "redirect:/";
        }
        return "authentication/registerUser";
    }
}
