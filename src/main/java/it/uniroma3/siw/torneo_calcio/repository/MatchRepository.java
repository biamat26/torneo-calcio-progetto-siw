package it.uniroma3.siw.torneo_calcio.repository;


import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournament_IdAndState(Long tournamentId, MatchStatus state);
    List<Match> findTop5ByStateOrderByDateTimeDesc(MatchStatus state);
    List<Match> findTop5ByStateOrderByDateTimeAsc(MatchStatus state);
    @Query(
            "SELECT m FROM Match m " +
            "WHERE m.tournament = :tournament " +
                "AND m.state = :state " +
                "AND (m.homeTeam = :team OR m.awayTeam = :team)")
    List<Match> findScheduledByTournamentAndTeam(@Param("tournament") Tournament tournament,
                                                 @Param("state") MatchStatus state,
                                                 @Param("team") Team team);
}
