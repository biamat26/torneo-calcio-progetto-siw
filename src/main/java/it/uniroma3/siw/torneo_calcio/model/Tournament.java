package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long year;

    @Column(length = 2000)
    private String desc;

    @ManyToMany
    private List<Team> teams;

    @OneToMany(mappedBy = "tournament")
    private List<Match> matches;

}
