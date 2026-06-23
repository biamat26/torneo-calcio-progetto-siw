package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import it.uniroma3.siw.torneo_calcio.dto.StandingRowDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * DTO (Data Transfer Object) per la classifica del torneo.
 *
 * Un DTO è un oggetto che serve esclusivamente a trasportare dati verso il client,
 * senza contenere logica di business. In questo caso viene usato per due motivi:
 *
 * 1. Evitare il loop infinito di serializzazione JSON: StandingRow contiene un oggetto
 *    Team, che contiene Tournament, che contiene di nuovo Team, e così via.
 *    Jackson (la libreria che converte in JSON) andrebbe in loop infinito.
 *    Il DTO "appiattisce" i dati, sostituendo l'oggetto Team con il solo nome della squadra.
 *
 * 2. Esporre solo i dati necessari: non è corretto esporre direttamente le entità JPA
 *    tramite API REST, perché contengono più informazioni di quelle necessarie al client
 *    e possono creare problemi di sicurezza o di performance.
 */
@RestController
public class TournamentRestController {

    private final TournamentService tournamentService;

    public TournamentRestController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/api/tournaments/{id}/standings")
    public List<StandingRowDTO> getStandings(@PathVariable Long id) {
        List<StandingRow> standings = tournamentService.getStandings(id);
        return standings.stream()
                .map(StandingRowDTO::new)
                .collect(Collectors.toList());
    }
}