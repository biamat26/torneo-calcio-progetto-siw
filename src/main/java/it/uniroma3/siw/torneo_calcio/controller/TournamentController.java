package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Team;
import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class TournamentController {

    private final TournamentService tournamentService;
    private final TeamService teamService;

    public TournamentController(TournamentService tournamentService, TeamService teamService){
        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }

    @GetMapping("/tournaments")
    public String list(Model model){
        model.addAttribute("tournaments", this.tournamentService.findAll());
        return "/tournaments/list";
    }

    @GetMapping("/tournaments/{id}")
    public String show(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isPresent()){
            Tournament tournament = optional.get();
            model.addAttribute("tournament", tournament);
        }else{
            return "redirect:/tournaments";
        }
        return "tournaments/show";
    }

    @GetMapping("/tournaments/{id}/fixtures")
    public String fixtures(@PathVariable("id")Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isEmpty()){
            return "redirect:/tournaments";
        }
        Tournament tournament = optional.get();
        model.addAttribute("tournament", tournament);
        model.addAttribute("fixtures", this.tournamentService.getFixtures(id));
        return "tournaments/fixtures";
    }

    @GetMapping("/tournaments/{id}/standings")
    public String standings(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isEmpty()){
            return "redirect:/tournaments";
        }
        Tournament tournament = optional.get();
        List<StandingRow> standings = this.tournamentService.getStandings(id);
        model.addAttribute("tournament", tournament);
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
                       @RequestParam (required = false) String action,
                       @RequestParam (required = false) Long teamId,
                       @RequestParam (required = false) List<Long> teamIds){

        /*
         * Problema: ogni volta che premo un bottone il form fa la submit e la pagina si ricarica perdendo i dati del form (in particolare la lista di team).
         * Soluzione: ricostruire la lista di team a partire dagli hidden input (teamIds) e poi, se l'azione è addTeam, aggiungere il team selezionato alla lista.
         */

        List<Team> teams = new ArrayList<>();
        if (teamIds != null){
            for(Long id : teamIds){
                Optional<Team> optional = teamService.findById(id);
                optional.ifPresent(teams::add);
            }
        }
        tournament.setTeams(teams);

        /*
         * Aggiungi Team. Quando si preme il bottone aggiungi team che manda action = addTeam
         * Prende il team selezionato dalla dropdown, lo aggiunge alla lista solo se non è gia presente
         * e poi ricarica la pagina con i dati aggiornati (in particolare la lista di team).
         */
        if("addTeam".equals(action)){
            if(teamId != null && teamId > 0){
                Optional<Team> team = teamService.findById(teamId);
                if(team.isPresent() && !tournament.getTeams().contains(team.get())){
                    tournament.getTeams().add(team.get());
                }
                model.addAttribute("teams", teamService.findAll());
                return "admin/tournaments/form";
            }
        }
        if (!bindingResult.hasErrors()) {
            tournamentService.save(tournament);
            return "redirect:/tournaments";
        }
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/form";
    }


    @GetMapping("/admin/tournaments/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model){
        Optional<Tournament> tournamentOptional = tournamentService.findById(id);
        if(tournamentOptional.isPresent()){
            model.addAttribute("tournament", tournamentOptional.get());
            model.addAttribute("teams", teamService.findAll());
            return "admin/tournaments/edit";
        }
        return "redirect:/tournaments";
    }

    @PostMapping("/admin/tournaments/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid  @ModelAttribute("tournament") Tournament tournament,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam (required = false) String action){
        tournament.setId(id);
        return save(tournament, bindingResult, model, null, null, null);
    }


    @PostMapping("/admin/tournaments/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }
}
