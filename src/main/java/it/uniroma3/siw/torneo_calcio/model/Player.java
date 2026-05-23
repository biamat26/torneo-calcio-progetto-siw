package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String first_name;

    @Column(nullable = false)
    private String last_name;

    private LocalDate birth_date;

    private Integer height;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Team team;

}
