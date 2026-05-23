package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Long foundation_year;

    private String city;

    @ManyToMany(mappedBy = "teams")
    private List<Tournament> tournaments;

    @OneToMany(mappedBy = "team")
    private List<Player> players;

    @ManyToOne
    private Tournament tournament;

    @OneToMany(mappedBy = "homeTeam")
    private List<Match> homeMatches;

    @OneToMany(mappedBy = "awayTeam")
    private List<Match> awayMatches;
}
