package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Referee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String first_name;

    @Column(nullable = false)
    private String last_name;

    @Column(nullable = false, unique = true)
    private String referee_code;

    @OneToMany(mappedBy = "referee")
    private List<Match> matches;
}
