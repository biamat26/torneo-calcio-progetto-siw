package it.uniroma3.siw.torneo_calcio.standings;

/**
 * DTO (Data Transfer Object) per la classifica del torneo.
 *
 * Un DTO è un oggetto che serve esclusivamente a trasportare dati verso il client,
 * senza contenere logica di business. In questo caso viene usato per due motivi:
 *
 * 1. Evitare il loop infinito di serializzazione JSON: StandingRow contiene un oggetto
 *    Team, che contiene Tournament, che contiene di nuovo Team, e così via.
 *    Jackson (la libreria che converte in JSON) andrebbe in loop infinito.
 *    Il DTO "appiattisce" i dati, sostituendo l'oggetto Team con il solo nome della squadra.
 *
 * 2. Esporre solo i dati necessari: non è corretto esporre direttamente le entità JPA
 *    tramite API REST, perché contengono più informazioni di quelle necessarie al client
 *    e possono creare problemi di sicurezza o di performance.
 *
 *
 *    Cosa si intende con loop di serializzazione?
 *
 *    Esempio:
 *    Se non avessimo utilizzato StandingRowDTO allora avremmo esposto direttamente StandingRow all'API REST,
 *    Jackson cercherebbe di convertire tutto in JSON e farebbe questo:
 *
 *    StandingRow
 * └── Team (AS Roma)
 *     └── tournaments
 *         └── Tournament (Serie A)
 *             └── teams
 *                 └── Team (AS Roma) ← stesso oggetto di prima!
 *                     └── tournaments
 *                         └── Tournament (Serie A)
 *                             └── teams
 *                                 └── Team (AS Roma)
 *                                     └── ...infinito
 */

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