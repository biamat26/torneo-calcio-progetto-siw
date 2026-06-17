package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.*;
import it.uniroma3.siw.torneo_calcio.repository.PlayerRepository;
import it.uniroma3.siw.torneo_calcio.repository.TeamRepository;
import it.uniroma3.siw.torneo_calcio.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository, TournamentRepository tournamentRepository){
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional(readOnly = true)
    public List<Team> findAll(){
        return this.teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Team> findById(Long id){
        return this.teamRepository.findById(id);
    }

    @Transactional
    public Team save(Team team){
        return teamRepository.save(team);
    }

    public void update(Team dati, Team esistente) {
        esistente.setName(dati.getName());
        esistente.setCity(dati.getCity());
        esistente.setFoundationYear(dati.getFoundationYear());
        esistente.setPlayers(dati.getPlayers());
        if (dati.getLogoUrl() != null) esistente.setLogoUrl(dati.getLogoUrl());
        teamRepository.save(esistente);
    }

    /**
     * Prima di cancellare una squadra, devo rimuoverla dai tornei.
     * I giocatori vengono scollegati (non cancellati).
     * Le partite associate vengono cancellate in cascade.
     */
    @Transactional
    public void delete(Long id){
        Optional<Team> teamOptional = teamRepository.findById(id);
        if(teamOptional.isPresent()){
            Team team = teamOptional.get();
            for(Tournament tournament : team.getTournaments()){
                tournament.getTeams().remove(team);
                tournamentRepository.save(tournament);
            }
            playerRepository.findByTeam(team).forEach(p -> {
                p.setTeam(null);
                playerRepository.save(p);
            });
            teamRepository.delete(team);
        }
    }
}