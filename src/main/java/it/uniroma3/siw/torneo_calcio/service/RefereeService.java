package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Referee;
import it.uniroma3.siw.torneo_calcio.repository.RefereeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RefereeService {

    private final RefereeRepository refereeRepository;

    public RefereeService(RefereeRepository refereeRepository){
        this.refereeRepository = refereeRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Referee> findById(Long id){
        return this.refereeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Referee> findAll(){return this.refereeRepository.findAll();}
}
