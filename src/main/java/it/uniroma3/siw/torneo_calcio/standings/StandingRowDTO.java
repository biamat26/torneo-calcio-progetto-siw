package it.uniroma3.siw.torneo_calcio.standings;

public class StandingRowDTO {
    private String teamName;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int points;

    public StandingRowDTO(StandingRow row) {
        this.teamName = row.getTeam().getName();
        this.played = row.getPlayed();
        this.wins = row.getWins();
        this.draws = row.getDraws();
        this.losses = row.getLosses();
        this.goalsFor = row.getGoalsFor();
        this.goalsAgainst = row.getGoalsAgainst();
        this.goalDifference = row.getGoalDifference();
        this.points = row.getPoints();
    }

    // getter per tutti i campi
    public String getTeamName() { return teamName; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalDifference() { return goalDifference; }
    public int getPoints() { return points; }
}