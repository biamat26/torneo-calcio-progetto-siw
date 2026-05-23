package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Referee;
import it.uniroma3.siw.torneo_calcio.repository.RefereeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RefereeService {

    private final RefereeRepository refereeRepository;

    public RefereeService(RefereeRepository refereeRepository){
        this.refereeRepository = refereeRepository;
    }

    public Optional<Referee> findById(Long id){
        return this.refereeRepository.findById(id);
    }
}
