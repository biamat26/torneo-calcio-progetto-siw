package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // TODO username, password, role
}
