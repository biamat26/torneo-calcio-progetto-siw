package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Player;
import it.uniroma3.siw.torneo_calcio.model.Role;
import it.uniroma3.siw.torneo_calcio.service.PlayerService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;

    public PlayerController(PlayerService playerService, TeamService teamService) {
        this.playerService = playerService;
        this.teamService = teamService;
    }

    @GetMapping("/players/{id}")
    public String show(@PathVariable Long id, Model model) {
        Optional<Player> optional = playerService.findById(id);
        if (optional.isEmpty()) return "redirect:/teams";
        model.addAttribute("player", optional.get());
        return "players/show";
    }

    @GetMapping("/admin/players/new")
    public String createForm(Model model) {
        model.addAttribute("player", new Player());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/players/form";
    }

    @PostMapping("/admin/players")
    public String save(@Valid @ModelAttribute("player") Player player,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long teamId,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("roles", Role.values());
            return "admin/players/form";
        }
        if (teamId != null) {
            teamService.findById(teamId).ifPresent(player::setTeam);
        }
        playerService.save(player);
        return "redirect:/teams";
    }

    @GetMapping("/admin/players/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Player> optional = playerService.findById(id);
        if (optional.isEmpty()) return "redirect:/teams";
        model.addAttribute("player", optional.get());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/players/edit";
    }

    @PostMapping("/admin/players/{id}")
    public String saveEdit(@PathVariable Long id,
                           @Valid @ModelAttribute("player") Player player,
                           BindingResult bindingResult,
                           @RequestParam(required = false) Long teamId,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("roles", Role.values());
            return "admin/players/edit";
        }
        player.setId(id);
        if (teamId != null) {
            teamService.findById(teamId).ifPresent(player::setTeam);
        }
        playerService.save(player);
        return "redirect:/teams";
    }

    @PostMapping("/admin/players/{id}/delete")
    public String delete(@PathVariable Long id) {
        Optional<Player> optional = playerService.findById(id);
        Long teamId = optional.map(p -> p.getTeam() != null ? p.getTeam().getId() : null).orElse(null);
        playerService.delete(id);
        return teamId != null ? "redirect:/teams/" + teamId : "redirect:/teams";
    }
}