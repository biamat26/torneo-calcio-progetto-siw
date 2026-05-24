package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository){
        this.matchRepository = matchRepository;
    }

    public List<Match> findAll(){
        return matchRepository.findAll();
    }

    public Optional<Match> findById(Long id){
        return matchRepository.findById(id);
    }

    public List<Match> getPlayedMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);
    }


}
