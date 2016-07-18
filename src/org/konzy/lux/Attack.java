package org.konzy.lux;//Created By: Brian Konzman on 7/13/2016

import com.sillysoft.lux.Country;

public class Attack {

    Country fromCountry;
    Country toCountry;
    SimulationBoard board;
    int fromID;
    int toID;

    public Attack(Country fromCountry, Country toCountry, SimulationBoard board) {
        this.fromCountry = fromCountry;
        this.toCountry = toCountry;
        this.board = board;
        this.fromID = fromCountry.getOwner();
        this.toID = toCountry.getOwner();
   }


    public boolean fromCountryIsWeakestOfPlayer() {
        return fromCountry.getCode() == board.getWeakestCountryOfPlayer(fromID).getCode();
    }

    public boolean toCountryIsWeakgestOfPlayer() {
        return toCountry.getCode() == board.getWeakestCountryOfPlayer(toID).getCode();
    }

    public boolean fromCountryIsStrongestOfPlayer() {
        return fromCountry.getCode() == board.getStrongestCountryOfPlayer(fromID).getCode();
    }

    public boolean toCountryIsStrongestOfPlayer() {
        return toCountry.getCode() == board.getStrongestCountryOfPlayer(toID).getCode();
    }

    public Country getFromCountry() {
        return fromCountry;
    }

    public Country getToCountry() {
        return toCountry;
    }

    public SimulationBoard getBoard() {
        return board;
    }
}
