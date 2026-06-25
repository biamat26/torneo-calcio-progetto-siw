package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.service.MatchService;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MatchService matchService;
    private final TournamentService tournamentService;

    public HomeController(MatchService matchService, TournamentService tournamentService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/")
    public String getHome(Model model) {
        model.addAttribute("lastResults", matchService.getLastResults());
        model.addAttribute("tournaments", tournamentService.findAll());
        model.addAttribute("upcomingMatches", matchService.getUpcomingMatches());
        model.addAttribute("numeroPartiteTot", tournamentService.numeroPartite());
        return "index";
    }
}