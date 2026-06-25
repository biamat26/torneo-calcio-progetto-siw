package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
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


    @Transactional
    public long count(){
        return this.matchRepository.count();
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

    @Transactional
    public Match save(Match match){
        return matchRepository.save(match);
    }

    @Transactional
    public void delete(Long id){
        this.matchRepository.findById(id).ifPresent(matchRepository::delete);
    }

    public List<Match> findByTournament_IdAndState(Long tournamentId, MatchStatus matchStatus) {
        return this.matchRepository.findByTournament_IdAndState(tournamentId, matchStatus);
    }

    public void deleteAll(List<Match> toDelete) {
        this.matchRepository.deleteAll();
    }

    public List<Match> findScheduledByTournamentAndTeam(Tournament tournament, MatchStatus matchStatus, Team team) {
        return this.matchRepository.findScheduledByTournamentAndTeam(tournament, matchStatus, team);
    }
}