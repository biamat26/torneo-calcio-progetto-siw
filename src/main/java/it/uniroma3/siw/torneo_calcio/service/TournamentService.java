package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.repository.MatchRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.repository.TournamentRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRowComparator;
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

    @Transactional(readOnly  = true)
    public List<Tournament> findAll(){
        return this.tournamentRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<StandingRow> getStandings(Long tournamentId){
        List<Match> matches = matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);
        Map<Team, StandingRow> table = new HashMap<>();
        for(Match match : matches){
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();
            if(!table.containsKey(homeTeam)){ table.put(homeTeam, new StandingRow(homeTeam)); }
            if(!table.containsKey(awayTeam)){ table.put(awayTeam, new StandingRow(awayTeam)); }
            table.get(homeTeam).addMatch(match.getGoalsHome(), match.getGoalsAway());
            table.get(awayTeam).addMatch(match.getGoalsAway(), match.getGoalsHome());
        }
        List<StandingRow> standings = new ArrayList<StandingRow>(table.values());
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


