package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Player;
import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.service.PlayerService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.boot.Banner;
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
        if(optional.isEmpty()){
            return "redirect:/teams";
        }
        model.addAttribute("team", optional.get());
        return "teams/show";
    }


    @GetMapping("/admin/teams/new")
    public String createForm(Model model){
        Team team = new Team();
        team.setPlayers(new ArrayList<>());
        model.addAttribute("team", team);
        model.addAttribute("players", teamService.findAll());
        return "admin/teams/form";
    }

    @PostMapping("/admin/teams")
    public String save(@Valid @ModelAttribute Team team,
                       BindingResult bindingResult,
                       Model model,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) Long playerId,
                       @RequestParam(required = false)List<Long> playerIds){
        List<Player> players = new ArrayList<>();
        if(playerIds != null){
            for(Long id : playerIds){
                Optional<Player> optional = playerService.findById(id);
                if(optional.isPresent()){
                    players.add(optional.get());
                }
            }
        }
        team.setPlayers(players);

        if("addPlayer".equals(action)){
            if(playerId!=null && playerId > 0){
                Optional<Player> player = playerService.findById(playerId);
                if(player.isPresent() && !team.getPlayers().contains(player.get())){
                    team.getPlayers().add(player.get());
                }
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
        Optional<Team> teamOptional = teamService.findById(id);
        if(teamOptional.isPresent()){
            model.addAttribute("team", teamOptional.get());
            model.addAttribute("players", playerService.findAll());
            return "admin/teams/edit";
        }
        return "redirect:/teams";
    }



    @PostMapping("/admin/teams/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid  @ModelAttribute("team") Team team,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam (required = false) String action){
        team.setId(id);
        return save(team, bindingResult, model, null, null, null);
    }


    @PostMapping("/admin/teams/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        teamService.delete(id);
        return "redirect:/teams";
    }

}
