package it.uniroma3.siw.torneo_calcio.repository;

import it.uniroma3.siw.torneo_calcio.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Carica tutti i tornei insieme alle loro squadre in una singola query SQL,
     * risolvendo il problema N+1.
     *
     * Senza questa query, Hibernate userebbe la strategia LAZY di default:
     * 1 query per caricare i tornei + 1 query per ogni torneo per caricare
     * le squadre associate (N+1 query totali).
     *
     * Con LEFT JOIN FETCH, Hibernate carica tornei e squadre in una sola query
     * tramite JOIN sulla tabella di join "tournament_teams".
     *
     * DISTINCT è necessario perché il JOIN produce righe duplicate:
     * un torneo con 3 squadre appare 3 volte nel risultato SQL.
     * DISTINCT elimina i duplicati a livello di oggetto Java.
     */
    @Query("SELECT DISTINCT t FROM Tournament t LEFT JOIN FETCH t.teams")
    List<Tournament> findAllWithTeams();
}
