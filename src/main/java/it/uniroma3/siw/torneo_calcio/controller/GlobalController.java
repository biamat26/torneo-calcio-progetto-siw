package it.uniroma3.siw.torneo_calcio.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Per far modo che lo username sia sempre disponibile ai template possiamo definire una classe con
 * annotazione @ControllerAdvice che definisce un metodo con annotazione @ModelAttribute("userDetails")
 * che restituisce l'oggetto UserDetails dell'utente autenticato, se presente, altrimenti null.
 * In questo modo, nei template Thymeleaf,
 * possiamo accedere all'oggetto userDetails per visualizzare lo username o altre informazioni dell'utente autenticato.
 */
@ControllerAdvice
public class GlobalController {

    @ModelAttribute("userDetails")
    public UserDetails getUser() {
        UserDetails user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return user;
    }
}
