package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Player;
import it.uniroma3.siw.torneo_calcio.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<Player> findAll(){
        return playerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Player> findById(Long id){
        return playerRepository.findById(id);
    }

    @Transactional
    public Player save(Player player){
        return this.playerRepository.save(player);
    }

    @Transactional
    public void delete(Long idPlayer){
        Optional<Player> player = this.playerRepository.findById(idPlayer);
        player.ifPresent(playerRepository::delete);
    }
}