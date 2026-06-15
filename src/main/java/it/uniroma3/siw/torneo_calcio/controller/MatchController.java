package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Comment;
import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.model.MatchStatus;
import it.uniroma3.siw.torneo_calcio.service.CommentService;
import it.uniroma3.siw.torneo_calcio.service.MatchService;
import it.uniroma3.siw.torneo_calcio.service.RefereeService;
import it.uniroma3.siw.torneo_calcio.service.TeamService;
import it.uniroma3.siw.torneo_calcio.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class MatchController {

    private final MatchService matchService;
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final RefereeService refereeService;
    private final CommentService commentService;

    public MatchController(MatchService matchService,
                           TournamentService tournamentService,
                           TeamService teamService,
                           RefereeService refereeService,
                           CommentService commentService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.refereeService = refereeService;
        this.commentService = commentService;
    }

    @GetMapping("/matches")
    public String list(Model model) {
        model.addAttribute("lastResults", matchService.getLastResults());
        model.addAttribute("upcomingMatches", matchService.getUpcomingMatches());
        return "matches/list";
    }

    @GetMapping("/matches/{id}")
    public String show(@PathVariable Long id, Model model) {
        Optional<Match> optional = matchService.findById(id);
        if (optional.isEmpty()) return "redirect:/tournaments";
        model.addAttribute("match", optional.get());
        model.addAttribute("comments", commentService.findByMatchId(id));
        model.addAttribute("newComment", new Comment());
        return "matches/show";
    }

    @GetMapping("/admin/matches/new")
    public String createForm(Model model) {
        Match match = new Match();
        match.setState(MatchStatus.SCHEDULED);
        model.addAttribute("match", match);
        model.addAttribute("tournaments", tournamentService.findAll());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("referees", refereeService.findAll());
        return "admin/matches/form";
    }

    @PostMapping("/admin/matches")
    public String save(@Valid @ModelAttribute("match") Match match,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long tournamentId,
                       @RequestParam(required = false) Long homeTeamId,
                       @RequestParam(required = false) Long awayTeamId,
                       @RequestParam(required = false) Long refereeId,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tournaments", tournamentService.findAll());
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("referees", refereeService.findAll());
            return "admin/matches/form";
        }
        if (homeTeamId != null && homeTeamId.equals(awayTeamId)) {
            bindingResult.rejectValue("homeTeam", "error.match",
                    "Le due squadre devono essere diverse");
            model.addAttribute("tournaments", tournamentService.findAll());
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("referees", refereeService.findAll());
            return "admin/matches/form";
        }
        if (tournamentId != null)
            tournamentService.findById(tournamentId).ifPresent(match::setTournament);
        if (homeTeamId != null)
            teamService.findById(homeTeamId).ifPresent(match::setHomeTeam);
        if (awayTeamId != null)
            teamService.findById(awayTeamId).ifPresent(match::setAwayTeam);
        if (refereeId != null)
            refereeService.findById(refereeId).ifPresent(match::setReferee);
        match.setState(MatchStatus.SCHEDULED);
        matchService.save(match);
        Long tournId = match.getTournament() != null ? match.getTournament().getId() : null;
        return tournId != null ? "redirect:/tournaments/" + tournId : "redirect:/tournaments";
    }

    @GetMapping("/admin/matches/{id}/result")
    public String resultForm(@PathVariable Long id, Model model) {
        Optional<Match> optional = matchService.findById(id);
        if (optional.isEmpty()) return "redirect:/tournaments";
        model.addAttribute("match", optional.get());
        return "admin/matches/result";
    }

    @PostMapping("/admin/matches/{id}/result")
    public String saveResult(@PathVariable Long id,
                             @RequestParam Integer goalsHome,
                             @RequestParam Integer goalsAway) {
        Optional<Match> optional = matchService.findById(id);
        if (optional.isEmpty()) return "redirect:/tournaments";
        Match match = optional.get();
        if (goalsHome < 0 || goalsAway < 0)
            return "redirect:/admin/matches/" + id + "/result";
        match.setGoalsHome(goalsHome);
        match.setGoalsAway(goalsAway);
        match.setState(MatchStatus.PLAYED);
        matchService.save(match);
        Long tournId = match.getTournament() != null ? match.getTournament().getId() : null;
        return tournId != null ? "redirect:/tournaments/" + tournId : "redirect:/tournaments";
    }

    @PostMapping("/admin/matches/{id}/delete")
    public String delete(@PathVariable Long id) {
        Optional<Match> optional = matchService.findById(id);
        Long tournId = optional
                .map(m -> m.getTournament() != null ? m.getTournament().getId() : null)
                .orElse(null);
        matchService.delete(id);
        return tournId != null ? "redirect:/tournaments/" + tournId : "redirect:/tournaments";
    }
}