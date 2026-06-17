package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.service.FileUploadService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class TournamentController {

    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final FileUploadService fileUploadService;
    private static final Logger log = LoggerFactory.getLogger(TournamentController.class);

    public TournamentController(TournamentService tournamentService, TeamService teamService, FileUploadService fileUploadService){
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/tournaments")
    public String list(Model model){
        model.addAttribute("tournaments", this.tournamentService.findAll());
        return "tournaments/list";
    }

    @GetMapping("/tournaments/{id}")
    public String show(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isPresent()){
            model.addAttribute("tournament", optional.get());
        }else{
            return "redirect:/tournaments";
        }
        return "tournaments/show";
    }

    @GetMapping("/tournaments/{id}/fixtures")
    public String fixtures(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isEmpty()) return "redirect:/tournaments";
        model.addAttribute("tournament", optional.get());
        model.addAttribute("fixtures", this.tournamentService.getFixtures(id));
        return "tournaments/fixtures";
    }

    @GetMapping("/tournaments/{id}/standings")
    public String standings(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isEmpty()) return "redirect:/tournaments";
        List<StandingRow> standings = this.tournamentService.getStandings(id);
        model.addAttribute("tournament", optional.get());
        model.addAttribute("standings", standings);
        return "tournaments/standings";
    }

    @GetMapping("/admin/tournaments/new")
    public String createForm(Model model){
        Tournament tournament = new Tournament();
        tournament.setTeams(new ArrayList<>());
        model.addAttribute("tournament", tournament);
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/form";
    }

    @PostMapping("/admin/tournaments")
    public String save(@Valid @ModelAttribute("tournament") Tournament tournament,
                       BindingResult bindingResult,
                       Model model,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) Long teamId,
                       @RequestParam(required = false) List<Long> teamIds,
                       @RequestParam(required = false) MultipartFile image){

        List<Team> teams = new ArrayList<>();
        if (teamIds != null){
            for(Long id : teamIds){
                teamService.findById(id).ifPresent(teams::add);
            }
        }
        tournament.setTeams(teams);

        if("addTeam".equals(action)){
            if(teamId != null && teamId > 0){
                teamService.findById(teamId).ifPresent(t -> {
                    if(!tournament.getTeams().contains(t))
                        tournament.getTeams().add(t);
                });
            }
            model.addAttribute("teams", teamService.findAll());
            return "admin/tournaments/form";
        }

        if("removeTeam".equals(action)){
            if(teamId != null && teamId > 0){
                tournament.getTeams().removeIf(t -> t.getId().equals(teamId));
            }
            model.addAttribute("teams", teamService.findAll());
            return "admin/tournaments/form";
        }

        if(!bindingResult.hasErrors()){
            try {
                String imageUrl = fileUploadService.save(image, "tournaments");
                if (imageUrl != null) tournament.setImageUrl(imageUrl);
            } catch (IOException e) {
                log.error("Errore upload immagine torneo: {}", e.getMessage());
            }
            tournamentService.save(tournament);
            return "redirect:/tournaments";
        }
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/form";
    }

    @PostMapping("/admin/tournaments/removeTeam")
    public String removeTeam(@RequestParam Long tournamentId,
                             @RequestParam Long teamId) {
        Optional<Tournament> tournamentOpt = tournamentService.findById(tournamentId);
        Optional<Team> teamOpt = teamService.findById(teamId);
        if(tournamentOpt.isEmpty() || teamOpt.isEmpty()) return "redirect:/tournaments";

        tournamentService.removeTeamFromTournament(tournamentOpt.get(), teamOpt.get());
        return "redirect:/admin/tournaments/" + tournamentId + "/edit";
    }

    @GetMapping("/admin/tournaments/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = tournamentService.findById(id);
        if(optional.isEmpty()) return "redirect:/tournaments";
        model.addAttribute("tournament", optional.get());
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/edit";
    }

    @PostMapping("/admin/tournaments/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("tournament") Tournament tournament,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam(required = false) String action,
                           @RequestParam(required = false) Long teamId,
                           @RequestParam(required = false) List<Long> teamIds){

        Optional<Tournament> esistenteOpt = tournamentService.findById(id);
        if(esistenteOpt.isEmpty()) return "redirect:/tournaments";
        Tournament esistente = esistenteOpt.get();

        List<Team> teams = new ArrayList<>();
        if(teamIds != null){
            for(Long tid : teamIds){
                teamService.findById(tid).ifPresent(teams::add);
            }
        }
        tournament.setTeams(teams);

        if("addTeam".equals(action)){
            if(teamId != null && teamId > 0){
                teamService.findById(teamId).ifPresent(t -> {
                    if(!tournament.getTeams().contains(t))
                        tournament.getTeams().add(t);
                });
            }
            tournamentService.update(tournament, esistente);
            return "redirect:/admin/tournaments/" + id + "/edit";
        }

        if("removeTeam".equals(action)){
            if(teamId != null && teamId > 0){
                tournament.getTeams().removeIf(t -> t.getId().equals(teamId));
            }
            tournamentService.update(tournament, esistente);
            return "redirect:/admin/tournaments/" + id + "/edit";
        }

        if(!bindingResult.hasErrors()){
            tournamentService.update(tournament, esistente);
            return "redirect:/tournaments";
        }
        model.addAttribute("tournament", tournament);
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/edit";
    }

    @PostMapping("/admin/tournaments/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }

    @PostMapping("/admin/tournaments/{id}/image")
    public String uploadImage(@PathVariable Long id,
                              @RequestParam MultipartFile image) {
        Optional<Tournament> optional = tournamentService.findById(id);
        if(optional.isEmpty()) return "redirect:/tournaments";
        Tournament tournament = optional.get();
        try {
            String imageUrl = fileUploadService.save(image, "tournaments");
            if (imageUrl != null) tournament.setImageUrl(imageUrl);
            tournamentService.save(tournament);
        } catch (IOException e) {
            log.error("Errore upload immagine torneo: {}", e.getMessage());
        }
        return "redirect:/admin/tournaments/" + id + "/edit";
    }

    @PostMapping("/admin/tournaments/{id}/image/delete")
    public String deleteImage(@PathVariable Long id) {
        Optional<Tournament> optional = tournamentService.findById(id);
        if(optional.isEmpty()) return "redirect:/tournaments";
        Tournament tournament = optional.get();
        tournament.setImageUrl(null);
        tournamentService.save(tournament);
        return "redirect:/admin/tournaments/" + id + "/edit";
    }
}