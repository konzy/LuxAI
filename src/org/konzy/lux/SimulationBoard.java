package org.konzy.lux;//Created By: Brian Konzman on 7/13/2016

import com.sillysoft.lux.Board;
import com.sillysoft.lux.Card;
import com.sillysoft.lux.Country;
import com.sillysoft.lux.agent.LuxAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class SimulationBoard extends Board {

    private static final int SIDES_OF_DICE = 6;
    private static final int MAX_NUMBER_OF_CARDS = 5;

    private Board readOnlyBoard;
    private Country[] countries;
    private int cardCashesPerformed;
    private String cardProgression;
    private int numberOfContinents;
    private int continentIncrease;
    private String mapPath;
    private int numberOfPlayers;
    private int turnCount;
    private int turnSecondsLeft;
    private int[] playersCards;
    private int[] playersIncome;
    private String[] playersName;
    private int[] continentBonuses;
    private String[] continentNames;
    private Random rand = new Random();
    private boolean tookOverACountry = false;
    private boolean transferCards;
    private boolean immediateCash;
    private String[] agentsName;
    private boolean useCards;
    private int lastDefender = -1;
    private int lastAttacker = -1;
    private HashMap<String, String> stringStorage = new HashMap<>();
    private HashMap<String, Integer> integerStorage = new HashMap<>();
    private HashMap<String, Float> floatStorage = new HashMap<>();
    private HashMap<String, Boolean> booleanStorage = new HashMap<>();

    public SimulationBoard(Board board) {
        super(null);
        readOnlyBoard = board;
        countries = Utils.getCountriesCopy(board.getCountries());
        cardCashesPerformed = board.getCardCashesPerformed();
        cardProgression = board.getCardProgression();
        numberOfContinents = board.getNumberOfContinents();
        continentIncrease = board.getContinentIncrease();
        mapPath = board.getMapPath();
        numberOfPlayers = board.getNumberOfPlayers();
        turnCount = board.getTurnCount();
        turnSecondsLeft = board.getTurnSecondsLeft();
        transferCards = board.transferCards();
        immediateCash = board.immediateCash();
        useCards = board.useCards();

        //Player stuff
        playersCards = new int[numberOfPlayers];
        playersIncome = new int[numberOfPlayers];
        playersName = new String[numberOfPlayers];
        agentsName = new String[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            playersCards[i] = board.getPlayerCards(i);
            playersIncome[i] = board.getPlayerIncome(i);
            playersName[i] = board.getPlayerName(i);
            agentsName[i] = board.getAgentName(i);
        }

        //Continent Stuff
        continentBonuses = new int[numberOfContinents];
        continentNames = new String[numberOfContinents];
        for (int i = 0; i < numberOfContinents; i++) {
            continentBonuses[i] = board.getContinentBonus(i);
            continentNames[i] = board.getContinentName(i);
        }
    }

    public int cashCardsForPlayer(int id) {
        int armies = 0;
        if (getPlayerCards(id) >= 3) {
            playersCards[id] -= 3;
            armies = getNextCardSetValue();
            cardCashesPerformed++;
        }
        return armies;
    }

    public Country getStrongestCountryOfPlayer(int id) {
        return getStrongWeakCountryOfPlayerHelper(id, true);
    }

    public Country getWeakestCountryOfPlayer(int id) {
        return getStrongWeakCountryOfPlayerHelper(id, false);
    }

    private Country getStrongWeakCountryOfPlayerHelper(int id, boolean isStrongest) {
        Country result = null;
        for (Country c :
                countriesOwnedByPlayer(id)) {
            if (result == null) {
                result = c;
            } else if (c.getArmies() > result.getArmies() && isStrongest) {
                result = c;
            } else if (c.getArmies() < result.getArmies() && !isStrongest) {
                result = c;
            }
        }
        return result;
    }

    public double getRating(int id) {
        double cardConst = 1.0;
        double incomeConst = 1.0;
        double countryConst = 1.0;
        double maxIncome = 0.0;

        for (int income : playersIncome) {
            maxIncome = Math.max(maxIncome, income);
        }

        return playersCards[id] / (double)MAX_NUMBER_OF_CARDS * cardConst +
                playersIncome[id] / maxIncome * incomeConst +
                countriesOwnedByPlayer(id).length / (double)countries.length * countryConst;
    }

    public double[] getAllRatings() {
        double[] result = new double[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            result[i] = getRating(i);
        }
        return result;
    }

    public Country[] countriesOwnedByPlayer(int id) {
        Country[] result = new Country[numberOfCountriesOwnedByPlayer(id)];
        int count = 0;
        for (Country c : countries) {
            if (c.getOwner() == id) {
                result[count] = c;
                count++;
            }
        }
        return result;
    }

    public int numberOfCountriesOwnedByPlayer(int id) {
        int result = 0;
        for (Country c : countries) {
            if (c.getOwner() == id) {
                result++;
            }
        }
        return result;
    }

    public int ownerOfContinent(int continentID) {
        int owner = -1;
        for (Country c : countries) {
            if (c.getContinent() == continentID) {
                if (owner == -1) {
                    owner = c.getOwner();
                } else if (owner != c.getOwner()) {
                    return -1;
                }
            }
        }
        return owner;
    }

    public ArrayList<Integer> continentsOwnedByPlayer(int playerID) {
        ArrayList<Integer> continentsOwned = new ArrayList<>();
        for (int i = 0; i < numberOfContinents; i++) {
            if(ownerOfContinent(i) == playerID) {
                continentsOwned.add(i);
            }
        }
        return continentsOwned;
    }

    @Override
    public boolean cashCards(Card card, Card card1, Card card2) {
        return false;
    }

    @Override
    public void placeArmies(int numberOfArmies, Country country) {
        placeArmies(numberOfArmies, country.getCode());
    }

    @Override
    public void placeArmies(int numberOfArmies, int countryCode) {
        countries[countryCode].setArmies(numberOfArmies, null);
    }

    @Override
    public int attack( Country attacker, Country defender, boolean attackTillDead) {
        int returnValue = 0;
        lastDefender = defender.getOwner();
        lastAttacker = attacker.getOwner();

        do {
            int numAttackers = Math.min(attacker.getArmies() - 1, 3);
            int numDefenders = Math.min(defender.getArmies(), 2);
            int[] attackerRolls = new int[numAttackers];
            int[] defenderRolls = new int[numDefenders];

            for (int i = 0; i < attackerRolls.length; i++) {
                attackerRolls[i] = rand.nextInt(SIDES_OF_DICE);
            }

            for (int i = 0; i < defenderRolls.length; i++) {
                defenderRolls[i] = rand.nextInt(SIDES_OF_DICE);
            }

            Arrays.sort(attackerRolls);
            Arrays.sort(defenderRolls);

            int temp = attackerRolls[0];
            attackerRolls[0] = attackerRolls[attackerRolls.length - 1];
            attackerRolls[attackerRolls.length - 1] = temp;

            temp = defenderRolls[0];
            defenderRolls[0] = defenderRolls[defenderRolls.length - 1];
            defenderRolls[defenderRolls.length - 1] = temp;

            int attackerIndex = 0;
            for (int i = 0; i < defenderRolls.length || attackerIndex < attackerRolls.length; i++) {
                if (defenderRolls[i] >= attackerRolls[attackerIndex]) {
                    attacker.setArmies(attacker.getArmies() - 1, null);
                    returnValue--;
                    if (attacker.getArmies() == 1) {
                        return returnValue;
                    }
                } else {
                    defender.setArmies(defender.getArmies() - 1, null);
                    returnValue++;
                    if (defender.getArmies() == 0) {
                        tookOverACountry = true;
                        defender.setOwner(attacker.getOwner(), null);
                        defender.setArmies(attackerIndex + 1, null);
                        attacker.setArmies(attacker.getArmies() - (attackerIndex + 1), null);

                        return returnValue;
                    }
                }
                attackerIndex--;
            }
        }while(attackTillDead);

        return returnValue;
    }

    @Override
    public int attack( int countryCodeAttacker, int countryCodeDefender, boolean attackTillDead) {
        return attack(countries[countryCodeAttacker], countries[countryCodeDefender], attackTillDead);
    }

    @Override
    public int fortifyArmies( int numberOfArmies, Country origin, Country destination) {
        if (origin.getMoveableArmies() == 0) {
            return -1;
        } else if (origin.getMoveableArmies() <= numberOfArmies) {
            numberOfArmies = origin.getMoveableArmies() - 1;
        }
        origin.setArmies(origin.getArmies() - numberOfArmies, null);
        destination.setArmies(destination.getArmies() + numberOfArmies, null);
        return 1;
    }

    @Override
    public int fortifyArmies( int numberOfArmies, int countryCodeOrigin, int countryCodeDestination) {
        return fortifyArmies(numberOfArmies, countries[countryCodeOrigin], countries[countryCodeDestination]);
    }

    public void incrementCardsForPlayer(int id) {
        playersCards[id]++;
    }

    @Override
    public void setContinentNames(String[] strings) {

    }

    @Override
    public void finished() {

    }

    @Override
    public Country[] getCountries() {
        return countries;
    }

    @Override
    public int getNumberOfCountries() {
        return countries.length;
    }

    @Override
    public int getNumberOfContinents() {
        return numberOfContinents;
    }

    @Override
    public int getContinentBonus(int i) {
        return continentBonuses[i];
    }

    @Override
    public String getContinentName(int i) {
        return continentNames[i];
    }

    @Override
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    @Override
    public int getNumberOfPlayersLeft() {
        int result = 0;
        for (int i = 0; i < numberOfPlayers; i++) {
            if (numberOfCountriesOwnedByPlayer(i) > 0) {
                result++;
            }
        }
        return result;
    }

    @Override
    public int getPlayerIncome(int id) {
        int result = numberOfCountriesOwnedByPlayer(id) / 3;
        for (int continents : continentsOwnedByPlayer(id)) {
            result += getContinentBonus(continents);
        }
        return result;
    }

    @Override
    public String getPlayerName(int i) {
        return playersName[i];
    }

    @Override
    public String getAgentName(int i) {
        return agentsName[i];
    }

    @Override
    public int getPlayerCards(int i) {
        return playersCards[i];
    }

    @Override
    public int getNextCardSetValue() {
        return readOnlyBoard.getNextCardSetValue();
    }

    @Override
    public String getMapTitle() {
        return null;
    }

    @Override
    public boolean tookOverACountry() {
        return tookOverACountry;
    }

    @Override
    public boolean useCards() {
        return useCards;
    }

    @Override
    public boolean transferCards() {

        return transferCards;
    }

    @Override
    public boolean immediateCash() {
        return immediateCash;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean playAudioAtURL(String s) {
        return true;
    }

    @Override
    public boolean sendChat(String s, LuxAgent luxAgent) {
        return true;
    }

    @Override
    public boolean sendChat(String s) {
        return true;
    }

    @Override
    public boolean sendEmote(String s) {
        return true;
    }

    @Override
    public boolean sendEmote(String s, LuxAgent luxAgent) {
        return true;
    }

    @Override
    public String storageGet(String key, String defaultValue) {
        if (stringStorage.containsKey(key)) {
            return stringStorage.get(key);
        }
        return defaultValue;
    }

    @Override
    public boolean storageGetBoolean(String key, boolean defaultValue) {
        if (booleanStorage.containsKey(key)) {
            return booleanStorage.get(key);
        }
        return defaultValue;
    }

    @Override
    public int storageGetInt(String key, int defaultValue) {
        if (integerStorage.containsKey(key)) {
            return integerStorage.get(key);
        }
        return defaultValue;
    }

    @Override
    public float storageGetFloat(String key, float defaultValue) {
        if (floatStorage.containsKey(key)) {
            return floatStorage.get(key);
        }
        return defaultValue;
    }

    @Override
    public void storagePut(String key, String value) {
        stringStorage.put(key, value);
    }

    @Override
    public void storagePutBoolean(String key, boolean value) {
        booleanStorage.put(key, value);
    }

    @Override
    public void storagePutInt(String key, int value) {
        integerStorage.put(key, value);
    }

    @Override
    public void storagePutFloat(String key, float value) {
        floatStorage.put(key, value);
    }

    @Override
    public void storageRemoveKey(String key) {
        stringStorage.remove(key);
    }

    @Override
    public LuxAgent getAgentInstance(String s) {
        return null;
    }

    @Override
    public String getCardProgression() {
        return cardProgression;
    }

    @Override
    public int getCardCashesPerformed() {
        return cardCashesPerformed;
    }

    @Override
    public int getNthCardCashValue(int i) {
        return readOnlyBoard.getNthCardCashValue(i);
    }

    @Override
    public int getContinentIncrease() {
        return continentIncrease;
    }

    @Override
    public int getTurnSecondsLeft() {
        return turnSecondsLeft;
    }

    @Override
    public int getTurnCount() {
        return turnCount;
    }

    @Override
    public boolean useScenario() {
        return false;
    }

    @Override
    public String getMapPath() {
        return mapPath;
    }
}