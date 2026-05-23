package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date_and_hour;

    @Column(nullable = false)
    private String local;

    private Integer goalsHome;

    private Integer goalsAway;


    @Enumerated(EnumType.STRING)
    private MatchState state;


    @ManyToOne
    private Tournament tournament;


    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    @ManyToOne
    private Referee referee;

}
