package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Player;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.service.PlayerService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class TeamController {

    private final TeamService teamService;
    private final PlayerService playerService;

    public TeamController(TeamService teamService, PlayerService playerService){
        this.playerService = playerService;
        this.teamService = teamService;
    }

    @GetMapping("/teams")
    public String list(Model model){
        model.addAttribute("teams", this.teamService.findAll());
        return "teams/list";
    }

    @GetMapping("/teams/{id}")
    public String show(@PathVariable("id") Long id, Model model){
        Optional<Team> optional = this.teamService.findById(id);
        if(optional.isEmpty()) return "redirect:/teams";
        model.addAttribute("team", optional.get());
        return "teams/show";
    }

    @GetMapping("/admin/teams/new")
    public String createForm(Model model){
        Team team = new Team();
        team.setPlayers(new ArrayList<>());
        model.addAttribute("team", team);
        model.addAttribute("players", playerService.findAll());
        return "admin/teams/form";
    }

    @PostMapping("/admin/teams")
    public String save(@Valid @ModelAttribute("team") Team team,
                       BindingResult bindingResult,
                       Model model,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) Long playerId,
                       @RequestParam(required = false) List<Long> playerIds){

        List<Player> players = new ArrayList<>();
        if(playerIds != null){
            for(Long id : playerIds){
                playerService.findById(id).ifPresent(players::add);
            }
        }
        team.setPlayers(players);

        if("addPlayer".equals(action)){
            if(playerId != null && playerId > 0){
                playerService.findById(playerId).ifPresent(p -> {
                    if(!team.getPlayers().contains(p))
                        team.getPlayers().add(p);
                });
            }
            model.addAttribute("players", playerService.findAll());
            return "admin/teams/form";
        }

        if("removePlayer".equals(action)){
            if(playerId != null && playerId > 0){
                team.getPlayers().removeIf(p -> p.getId().equals(playerId));
            }
            model.addAttribute("players", playerService.findAll());
            return "admin/teams/form";
        }

        if(!bindingResult.hasErrors()){
            teamService.save(team);
            return "redirect:/teams";
        }
        model.addAttribute("players", playerService.findAll());
        return "admin/teams/form";
    }

    @GetMapping("/admin/teams/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        Optional<Team> optional = teamService.findById(id);
        if(optional.isEmpty()) return "redirect:/teams";
        model.addAttribute("team", optional.get());
        model.addAttribute("players", playerService.findAll());
        return "admin/teams/edit";
    }

    @PostMapping("/admin/teams/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("team") Team team,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam(required = false) String action,
                           @RequestParam(required = false) Long playerId,
                           @RequestParam(required = false) List<Long> playerIds){

        Optional<Team> esistenteOpt = teamService.findById(id);
        if(esistenteOpt.isEmpty()) return "redirect:/teams";
        Team esistente = esistenteOpt.get();

        List<Player> players = new ArrayList<>();
        if(playerIds != null){
            for(Long pid : playerIds){
                playerService.findById(pid).ifPresent(players::add);
            }
        }
        team.setPlayers(players);

        if("addPlayer".equals(action)){
            if(playerId != null && playerId > 0){
                Optional<Player> playerOpt = playerService.findById(playerId);
                if(playerOpt.isPresent()){
                    Player player = playerOpt.get();
                    player.setTeam(esistente);
                    playerService.save(player);
                }
            }
            return "redirect:/admin/teams/" + id + "/edit";
        }

        if("removePlayer".equals(action)){
            if(playerId != null && playerId > 0){
                Optional<Player> playerOpt = playerService.findById(playerId);
                if(playerOpt.isPresent()){
                    Player player = playerOpt.get();
                    player.setTeam(null);
                    playerService.save(player);
                }
            }
            return "redirect:/admin/teams/" + id + "/edit";
        }

        if(!bindingResult.hasErrors()){
            teamService.update(team, esistente);
            return "redirect:/teams";
        }
        model.addAttribute("team", team);
        model.addAttribute("players", playerService.findAll());
        return "admin/teams/edit";
    }

    @PostMapping("/admin/teams/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        teamService.delete(id);
        return "redirect:/teams";
    }
}