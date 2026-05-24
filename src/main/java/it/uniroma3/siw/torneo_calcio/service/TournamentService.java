package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.repository.MatchRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.repository.TournamentRepository;
import it.uniroma3.siw.torneo_calcio.standings.StandingRowComparator;
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

    public Optional<Tournament> findById(Long id){
        return this.tournamentRepository.findById(id);
    }

    public List<Tournament> findAll(){
        return this.tournamentRepository.findAll();
    }


    public List<StandingRow> getStandings(Long tournamentId){

        List<Match> matches = matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);

        Map<Team, StandingRow> table = new HashMap<>();

        for(Match match : matches){
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();

            if(!table.containsKey(homeTeam)){
                table.put(homeTeam, new StandingRow(homeTeam));
            }

            if(!table.containsKey(awayTeam)){
                table.put(awayTeam, new StandingRow(awayTeam));
            }

            table.get(homeTeam).addMatch(match.getGoalsHome(), match.getGoalsAway());
            table.get(awayTeam).addMatch(match.getGoalsAway(), match.getGoalsHome());
        }

        List<StandingRow> standings = new ArrayList<StandingRow>(table.values());

        standings.sort(new StandingRowComparator());

        return standings;
    }
}
