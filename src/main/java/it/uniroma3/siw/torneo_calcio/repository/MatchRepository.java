package it.uniroma3.siw.torneo_calcio.repository;


import it.uniroma3.siw.torneo_calcio.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

}
