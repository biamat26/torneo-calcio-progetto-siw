package it.uniroma3.siw.torneo_calcio.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Controller globale con due responsabilità:
 * - espone {@code userDetails} come attributo di modello in tutti i template,
 *   così ogni pagina può mostrare lo username dell'utente autenticato;
 * - gestisce centralmente gli errori HTTP più comuni (404 e 500),
 *   reindirizzando a pagine di errore dedicate invece della pagina bianca di default.
 */
@ControllerAdvice
public class GlobalErrorController {
    /**
     * Gestisce gli errori 404 — risorsa non trovata.
     * Viene invocato quando nessun controller corrisponde all'URL richiesto.
     *
     * @param request la richiesta HTTP che ha generato l'errore
     * @param model   il modello per passare l'URL alla view
     * @return la pagina errors/404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(HttpServletRequest request, Model model) {
        model.addAttribute("requestedUrl", request.getRequestURI());
        return "errors/404";
    }

    /**
     * Gestisce gli errori 500 — errore interno del server.
     * Cattura qualsiasi eccezione non gestita altrove nell'applicazione.
     *
     * @param ex    l'eccezione che ha causato l'errore
     * @param model il modello per passare il messaggio di errore alla view
     * @return la pagina errors/500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "errors/500";
    }
}