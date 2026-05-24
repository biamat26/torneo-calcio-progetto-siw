package it.uniroma3.siw.torneo_calcio.standings;


import it.uniroma3.siw.torneo_calcio.model.Team;

public class StandingRow {
    private Team team;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;

    public StandingRow(Team team){
        this.team = team;
    }

    public int getGoalDifference(){
        return goalsFor - goalsAgainst;
    }

    public void addMatch(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) wins++;
        else if (scored < conceded) losses++;
        else draws++;
    }

    public int getPoints() {
        return this.wins * 3 + this.draws;
    }

    // ---------- GETTER AND SETTER ----------------

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }
}
