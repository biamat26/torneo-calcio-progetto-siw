package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Tournament;
import it.uniroma3.siw.torneo_calcio.standings.StandingRow;
import org.springframework.ui.Model;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService){
        this.tournamentService = tournamentService;
    }

    @GetMapping()
    public String list(Model model){
        model.addAttribute("tournaments", this.tournamentService.findAll());
        return "/tournaments/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isPresent()){
            Tournament tournament = optional.get();
            model.addAttribute("tournament", tournament);
        }else{
            return "redirect:/tournaments";
        }
        return "tournaments/show";
    }

    @GetMapping("/{id}/standings")
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
}
