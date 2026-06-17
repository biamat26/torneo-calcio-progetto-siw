package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Long foundationYear;

    private String city;

    private String logoUrl;

    @ManyToMany(mappedBy = "teams")
    private List<Tournament> tournaments;

    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    private List<Player> players;

    /**
     * Con
     * cascade = CascadeType.REMOVE
     * in quanto se cancello una squadra, devo cancellare anche
     * le partite associate.
     */
    @OneToMany(mappedBy = "homeTeam", cascade = CascadeType.REMOVE)
    private List<Match> homeMatches;

    @OneToMany(mappedBy = "awayTeam", cascade = CascadeType.REMOVE)
    private List<Match> awayMatches;


    // ------------- EQUALS AND HASHCODE -------------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(getId(), team.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }


    // -------------- GETTER AND SETTER ---------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFoundationYear() {
        return foundationYear;
    }

    public void setFoundationYear(Long foundationYear) {
        this.foundationYear = foundationYear;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Tournament> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<Tournament> tournaments) {
        this.tournaments = tournaments;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Match> getHomeMatches() {
        return homeMatches;
    }

    public void setHomeMatches(List<Match> homeMatches) {
        this.homeMatches = homeMatches;
    }

    public List<Match> getAwayMatches() {
        return awayMatches;
    }

    public void setAwayMatches(List<Match> awayMatches) {
        this.awayMatches = awayMatches;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
