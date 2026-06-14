package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository){
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<Match> findAll(){
        return matchRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<Match> findById(Long id){
        return matchRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Match> getPlayedMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournament_IdAndState(tournamentId, MatchStatus.PLAYED);
    }

    @Transactional(readOnly = true)
    public List<Match> getLastResults(){
        return matchRepository.findTop5ByStateOrderByDateTimeDesc(MatchStatus.PLAYED);
    }

    @Transactional(readOnly = true)
    public List<Match> getUpcomingMatches(){
        return matchRepository.findTop5ByStateOrderByDateTimeAsc(MatchStatus.SCHEDULED);
    }

}
