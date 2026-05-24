package it.uniroma3.siw.torneo_calcio.standings;

import java.util.Comparator;

public class StandingRowComparator implements Comparator<StandingRow> {

    @Override
    public int compare(StandingRow s1, StandingRow s2) {
        if(s1.getPoints() != s2.getPoints()){
            return s2.getPoints()-s1.getPoints();
        }
        return s2.getGoalDifference() - s1.getGoalDifference();

    }
}
