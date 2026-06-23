package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.dto.PlayerDTO;
import it.uniroma3.siw.torneo_calcio.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PlayerRestController {

    private final PlayerService playerService;

    public PlayerRestController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Restituisce la lista di tutti i giocatori in formato JSON.
     *
     * Non espone le entità Player direttamente: le converte in PlayerDTO
     * per evitare loop di serializzazione JSON (Player → Team → players → Player...)
     * e per non esporre dati non necessari al frontend.
     *
     * Il filtraggio (per ruolo, per nome) avviene lato client in React —
     * non serve un endpoint separato per ogni combinazione di filtri.
     */
    @GetMapping("/api/players")
    public List<PlayerDTO> getPlayers() {
        return playerService.findAll()
                .stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }
}