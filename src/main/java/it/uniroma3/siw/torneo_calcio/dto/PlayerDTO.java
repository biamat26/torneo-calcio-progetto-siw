package it.uniroma3.siw.torneo_calcio.dto;

import it.uniroma3.siw.torneo_calcio.model.Player;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) per esporre i dati di un giocatore via API REST.
 *
 * Non esponiamo direttamente l'entità Player perché contiene una relazione
 * @ManyToOne verso Team, che a sua volta contiene tournaments, homeMatches,
 * awayMatches, players... Jackson andrebbe in loop infinito di serializzazione.
 *
 * Il DTO "appiattisce" la relazione: invece dell'oggetto Team completo,
 * espone solo teamName e teamId — i dati che servono al frontend.
 */
public class PlayerDTO {

    private Long id;
    private String name;
    private String surname;
    private String role;
    private Integer height;
    private LocalDate birthDate;
    private String photoUrl;
    private String teamName;
    private Long teamId;

    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.name = player.getName();
        this.surname = player.getSurname();
        this.role = player.getRole() != null ? player.getRole().name() : null;
        this.height = player.getHeight();
        this.birthDate = player.getBirth_date();
        this.photoUrl = player.getPhotoUrl();
        if (player.getTeam() != null) {
            this.teamName = player.getTeam().getName();
            this.teamId = player.getTeam().getId();
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getRole() { return role; }
    public Integer getHeight() { return height; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhotoUrl() { return photoUrl; }
    public String getTeamName() { return teamName; }
    public Long getTeamId() { return teamId; }
}