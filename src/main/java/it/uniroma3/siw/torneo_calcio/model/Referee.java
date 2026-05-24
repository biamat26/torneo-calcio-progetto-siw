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


    // -------------- GETTER AND SETTER ---------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
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
