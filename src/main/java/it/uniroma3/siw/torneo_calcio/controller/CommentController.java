package it.uniroma3.siw.torneo_calcio.controller;

import it.uniroma3.siw.torneo_calcio.model.Comment;
import it.uniroma3.siw.torneo_calcio.model.Credentials;
import it.uniroma3.siw.torneo_calcio.model.Match;
import it.uniroma3.siw.torneo_calcio.service.CommentService;
import it.uniroma3.siw.torneo_calcio.service.CredentialsService;
import it.uniroma3.siw.torneo_calcio.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final MatchService matchService;
    private final CredentialsService credentialsService;

    public CommentController(CommentService commentService,
                             MatchService matchService,
                             CredentialsService credentialsService) {
        this.commentService = commentService;
        this.matchService = matchService;
        this.credentialsService = credentialsService;
    }

    @PostMapping("/matches/{matchId}/comments")
    public String addComment(@PathVariable Long matchId,
                             @Valid @ModelAttribute("newComment") Comment comment,
                             BindingResult bindingResult,
                             Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "redirect:/matches/" + matchId;
        }
        Optional<Match> matchOpt = matchService.findById(matchId);
        if (matchOpt.isEmpty()) return "redirect:/tournaments";

        Credentials author = credentialsService.getCredentials(authentication.getName());
        comment.setMatch(matchOpt.get());
        comment.setAuthor(author);
        commentService.save(comment);
        return "redirect:/matches/" + matchId;
    }

    @GetMapping("/matches/{matchId}/comments/{commentId}/edit")
    public String editForm(@PathVariable Long matchId,
                           @PathVariable Long commentId,
                           Authentication authentication,
                           Model model) {
        Optional<Comment> commentOpt = commentService.findById(commentId);
        if (commentOpt.isEmpty()) return "redirect:/matches/" + matchId;

        Comment comment = commentOpt.get();
        if (!comment.getAuthor().getUsername().equals(authentication.getName())) {
            return "redirect:/matches/" + matchId;
        }
        model.addAttribute("comment", comment);
        model.addAttribute("matchId", matchId);
        return "comments/edit";
    }

    @PostMapping("/matches/{matchId}/comments/{commentId}/edit")
    public String saveEdit(@PathVariable Long matchId,
                           @PathVariable Long commentId,
                           @Valid @ModelAttribute("comment") Comment updated,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {
        Optional<Comment> commentOpt = commentService.findById(commentId);
        if (commentOpt.isEmpty()) return "redirect:/matches/" + matchId;

        Comment existing = commentOpt.get();
        if (!existing.getAuthor().getUsername().equals(authentication.getName())) {
            return "redirect:/matches/" + matchId;
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("comment", updated);
            model.addAttribute("matchId", matchId);
            return "comments/edit";
        }
        existing.setText(updated.getText());
        commentService.save(existing);
        return "redirect:/matches/" + matchId;
    }
}