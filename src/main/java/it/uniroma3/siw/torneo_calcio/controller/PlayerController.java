package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Player;
import it.uniroma3.siw.torneo_calcio.model.Role;
import it.uniroma3.siw.torneo_calcio.service.FileUploadService;
import it.uniroma3.siw.torneo_calcio.service.PlayerService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;
    private final FileUploadService fileUploadService;
    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    public PlayerController(PlayerService playerService, TeamService teamService, FileUploadService fileUploadService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/players")
    public String list(Model model) {
        model.addAttribute("players", playerService.findAll());
        return "players/list";
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
                       @RequestParam(required = false) MultipartFile photo,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("roles", Role.values());
            return "admin/players/form";
        }
        if (teamId != null) {
            teamService.findById(teamId).ifPresent(player::setTeam);
        }
        try {
            String photoUrl = fileUploadService.save(photo, "players");
            if (photoUrl != null) player.setPhotoUrl(photoUrl);
        } catch (IOException e) {
            log.error("Errore upload foto giocatore: {}", e.getMessage());
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
        // Mantieni photoUrl esistente
        Optional<Player> esistenteOpt = playerService.findById(id);
        esistenteOpt.ifPresent(p -> player.setPhotoUrl(p.getPhotoUrl()));

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

    @PostMapping("/admin/players/{id}/image")
    public String uploadImage(@PathVariable Long id,
                              @RequestParam MultipartFile photo) {
        Optional<Player> optional = playerService.findById(id);
        if(optional.isEmpty()) return "redirect:/teams";
        Player player = optional.get();
        try {
            String photoUrl = fileUploadService.save(photo, "players");
            if (photoUrl != null) player.setPhotoUrl(photoUrl);
            playerService.save(player);
        } catch (IOException e) {
            log.error("Errore upload foto giocatore: {}", e.getMessage());
        }
        return "redirect:/admin/players/" + id + "/edit";
    }

    @PostMapping("/admin/players/{id}/image/delete")
    public String deleteImage(@PathVariable Long id) {
        Optional<Player> optional = playerService.findById(id);
        if(optional.isEmpty()) return "redirect:/teams";
        Player player = optional.get();
        player.setPhotoUrl(null);
        playerService.save(player);
        return "redirect:/admin/players/" + id + "/edit";
    }


}