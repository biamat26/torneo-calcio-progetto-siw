package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.repository.MatchRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.repository.TournamentRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRowComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    public TournamentService(TournamentRepository tournamentRepository, MatchRepository matchRepository){
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly  = true)
    public Optional<Tournament> findById(Long id){
        return this.tournamentRepository.findById(id);
    }

    /**
     *
     * METODO findAll() per testare le statistiche di
     *
     * @Transactional(readOnly = true)
     *     public List<Tournament> findAll() {
     *         long start = System.currentTimeMillis();
     *         List<Tournament> result = tournamentRepository.findAll();
     *         // forza il caricamento delle squadre (altrimenti LAZY non le carica)
     *         result.forEach(t -> t.getTeams().size());
     *         long end = System.currentTimeMillis();
     *         log.debug(">>> LAZY FETCH STRATEGY: - tempo: {}ms, tornei: {}", end - start, result.size());
     *         return result;
     *     }
     */

    private static final Logger log = LoggerFactory.getLogger(TournamentService.class);

    /**
     * Un semplice findAll() che mostra anche le statistiche (utile per confrontare le statistiche)
     */
    @Transactional(readOnly = true)
    public List<Tournament> findAll() {
        long start = System.currentTimeMillis();
        List<Tournament> result = tournamentRepository.findAllWithTeams();
        result.forEach(t -> t.getTeams().size());
        long end = System.currentTimeMillis();
        log.debug(">>> JOIN FETCH STRATEGY: - tempo: {}ms, tornei: {}", end - start, result.size());
        return result;
    }


    @Transactional(readOnly = true)
    public List<StandingRow> getStandings(Long tournamentId){
        // Inizializza la tabella con TUTTE le squadre del torneo — anche quelle senza partite
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        Map<Team, StandingRow> table = new HashMap<>();
        for (Team team : tournament.getTeams()) {
            table.put(team, new StandingRow(team));
        }

        // Aggiunge i risultati delle partite giocate
        List<Match> matches = matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);
        for(Match match : matches){
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();
            if(!table.containsKey(homeTeam)){ table.put(homeTeam, new StandingRow(homeTeam)); }
            if(!table.containsKey(awayTeam)){ table.put(awayTeam, new StandingRow(awayTeam)); }
            table.get(homeTeam).addMatch(match.getGoalsHome(), match.getGoalsAway());
            table.get(awayTeam).addMatch(match.getGoalsAway(), match.getGoalsHome());
        }

        List<StandingRow> standings = new ArrayList<>(table.values());
        standings.sort(new StandingRowComparator());
        return standings;
    }

    @Transactional(readOnly = true)
    public List<Match> getFixtures(Long tournamentId){
        return matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.SCHEDULED);
    }

    @Transactional(readOnly = true)
    public List<Match> getResults(Long tournamentId){
        return matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);
    }

    @Transactional
    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public void delete(Long id){
        Optional<Tournament> tournament = tournamentRepository.findById(id);
        tournament.ifPresent(tournamentRepository::delete);
    }

    @Transactional
    public void update(Tournament dati, Tournament esistente) {
        esistente.setName(dati.getName());
        esistente.setYear(dati.getYear());
        esistente.setDescription(dati.getDescription());
        esistente.setTeams(dati.getTeams());
        if (dati.getImageUrl() != null) esistente.setImageUrl(dati.getImageUrl());
        tournamentRepository.save(esistente);
    }

    @Transactional
    public void removeTeamFromTournament(Tournament tournament, Team team) {
        // Cancella le partite SCHEDULED che coinvolgono la squadra in questo torneo
        List<Match> toDelete = matchRepository.findScheduledByTournamentAndTeam(
                tournament, MatchStatus.SCHEDULED, team);
        matchRepository.deleteAll(toDelete);

        // Rimuove la squadra dal torneo
        tournament.getTeams().remove(team);
        tournamentRepository.save(tournament);
    }

}


