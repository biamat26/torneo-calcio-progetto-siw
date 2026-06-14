package it.uniroma3.siw.torneo_calcio.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
public class Referee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String referee_code;

    @OneToMany(mappedBy = "referee")
    private List<Match> matches;


    // ------------- EQUALS AND HASHCODE -------------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Referee referee = (Referee) o;
        return Objects.equals(getId(), referee.getId());
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getReferee_code() {
        return referee_code;
    }

    public void setReferee_code(String referee_code) {
        this.referee_code = referee_code;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
