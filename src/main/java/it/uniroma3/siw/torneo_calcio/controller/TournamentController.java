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

    /**
     * Mostra la lista di tutti i tornei disponibili.
     *
     * @param model il model Spring MVC in cui viene iniettata la lista dei tornei
     * @return la view "tournaments/list"
     */
    @GetMapping("/tournaments")
    public String list(Model model){
        model.addAttribute("tournaments", this.tournamentService.findAll());
        return "/tournaments/list";
    }

    /**
     * Mostra il dettaglio di un singolo torneo, incluse le squadre partecipanti.
     * Se il torneo non esiste, reindirizza alla lista dei tornei.
     *
     * @param id    l'id del torneo da visualizzare
     * @param model il model Spring MVC in cui viene iniettato il torneo
     * @return la view "tournaments/show", oppure redirect a /tournaments se non trovato
     */
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

    /**
     * Mostra il calendario delle partite programmate (stato SCHEDULED) di un torneo.
     * Se il torneo non esiste, reindirizza alla lista dei tornei.
     *
     * @param id    l'id del torneo
     * @param model il model Spring MVC in cui vengono iniettati il torneo e le partite
     * @return la view "tournaments/fixtures", oppure redirect a /tournaments se non trovato
     */
    @GetMapping("/tournaments/{id}/fixtures")
    public String fixtures(@PathVariable("id") Long id, Model model){
        Optional<Tournament> optional = this.tournamentService.findById(id);
        if(optional.isEmpty()){
            return "redirect:/tournaments";
        }
        Tournament tournament = optional.get();
        model.addAttribute("tournament", tournament);
        model.addAttribute("fixtures", this.tournamentService.getFixtures(id));
        return "tournaments/fixtures";
    }

    /**
     * Mostra la classifica del torneo, calcolata dinamicamente a partire
     * dai risultati delle partite con stato PLAYED.
     * Se il torneo non esiste, reindirizza alla lista dei tornei.
     *
     * @param id    l'id del torneo
     * @param model il model Spring MVC in cui vengono iniettati il torneo e la classifica
     * @return la view "tournaments/standings", oppure redirect a /tournaments se non trovato
     */
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

    /**
     * Mostra il form per la creazione di un nuovo torneo.
     * Inizializza un torneo vuoto con lista squadre vuota e carica
     * tutte le squadre disponibili per la selezione.
     *
     * @param model il model Spring MVC
     * @return la view "admin/tournaments/form"
     */
    @GetMapping("/admin/tournaments/new")
    public String createForm(Model model){
        Tournament tournament = new Tournament();
        tournament.setTeams(new ArrayList<>());
        model.addAttribute("tournament", tournament);
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/form";
    }

    /**
     * Gestisce il submit del form di creazione (e indirettamente di modifica)
     * del torneo. Supporta tre azioni distinte tramite il parametro "action":
     * <ul>
     *   <li><b>addTeam</b>: aggiunge una squadra alla lista del torneo e ricarica il form</li>
     *   <li><b>removeTeam</b>: rimuove una squadra dalla lista del torneo e ricarica il form</li>
     *   <li><b>save</b> (default): valida e salva il torneo, poi reindirizza alla lista</li>
     * </ul>
     * Ad ogni submit, la lista delle squadre viene ricostruita dagli hidden input "teamIds"
     * per non perdere le selezioni già effettuate tra un submit e l'altro.
     *
     * @param tournament    l'oggetto torneo popolato dal form
     * @param bindingResult risultato della validazione
     * @param model         il model Spring MVC
     * @param action        l'azione richiesta ("addTeam", "removeTeam", o "save")
     * @param teamId        l'id della squadra da aggiungere o rimuovere
     * @param teamIds       la lista degli id delle squadre già presenti nel torneo
     * @return la view del form se ci sono errori o azioni intermedie,
     *         altrimenti redirect a /tournaments
     */
    @PostMapping("/admin/tournaments")
    public String save(@Valid @ModelAttribute("tournament") Tournament tournament,
                       BindingResult bindingResult,
                       Model model,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) Long teamId,
                       @RequestParam(required = false) List<Long> teamIds){

        List<Team> teams = new ArrayList<>();
        if (teamIds != null){
            for(Long id : teamIds){
                Optional<Team> optional = teamService.findById(id);
                optional.ifPresent(teams::add);
            }
        }
        tournament.setTeams(teams);

        if("addTeam".equals(action)){
            if(teamId != null && teamId > 0){
                Optional<Team> team = teamService.findById(teamId);
                if(team.isPresent() && !tournament.getTeams().contains(team.get())){
                    tournament.getTeams().add(team.get());
                }
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
        if (!bindingResult.hasErrors()) {
            tournamentService.save(tournament);
            return "redirect:/tournaments";
        }
        model.addAttribute("teams", teamService.findAll());
        return "admin/tournaments/form";
    }

    /**
     * Rimuove una squadra da un torneo già esistente e persistito.
     * Usato dal form di modifica tramite "formaction" sul bottone Rimuovi,
     * così da evitare conflitti tra i parametri "teamId" del form principale.
     * Dopo la rimozione reindirizza al form di modifica del torneo.
     *
     * @param tournamentId l'id del torneo da cui rimuovere la squadra
     * @param teamId       l'id della squadra da rimuovere
     * @return redirect al form di modifica del torneo
     */
    @PostMapping("/admin/tournaments/removeTeam")
    public String removeTeam(@RequestParam Long tournamentId,
                             @RequestParam Long teamId) {
        Optional<Tournament> optional = tournamentService.findById(tournamentId);
        if (optional.isEmpty()) return "redirect:/tournaments";
        Tournament tournament = optional.get();
        tournament.getTeams().removeIf(t -> t.getId().equals(teamId));
        tournamentService.save(tournament);
        return "redirect:/admin/tournaments/" + tournamentId + "/edit";
    }

    /**
     * Mostra il form di modifica di un torneo esistente.
     * Carica il torneo con le sue squadre attuali e tutte le squadre
     * disponibili per aggiungerne di nuove.
     * Se il torneo non esiste, reindirizza alla lista dei tornei.
     *
     * @param id    l'id del torneo da modificare
     * @param model il model Spring MVC
     * @return la view "admin/tournaments/edit", oppure redirect a /tournaments se non trovato
     */
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

    /**
     * Gestisce il submit del form di modifica di un torneo esistente.
     * Imposta l'id sul torneo ricevuto dal form e delega la logica
     * al metodo save(), che gestisce anche addTeam e removeTeam.
     *
     * @param id            l'id del torneo da modificare
     * @param tournament    l'oggetto torneo popolato dal form
     * @param bindingResult risultato della validazione
     * @param model         il model Spring MVC
     * @param action        l'azione richiesta (opzionale)
     * @return risultato del metodo save()
     */
    @PostMapping("/admin/tournaments/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("tournament") Tournament tournament,
                           BindingResult bindingResult,
                           Model model,
                           @RequestParam(required = false) String action){
        tournament.setId(id);
        return save(tournament, bindingResult, model, action, null, null);
    }

    /**
     * Elimina un torneo e tutte le sue partite associate (cascade).
     * Dopo l'eliminazione reindirizza alla lista dei tornei.
     *
     * @param id l'id del torneo da eliminare
     * @return redirect a /tournaments
     */
    @PostMapping("/admin/tournaments/{id}/delete")
    public String delete(@PathVariable("id") Long id){
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }
}