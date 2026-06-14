package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository){
        this.teamRepository = teamRepository;
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

    @Transactional
    public void delete(Long id){
        Optional<Team> team = teamRepository.findById(id);
        team.ifPresent(teamRepository::delete);
    }
}

