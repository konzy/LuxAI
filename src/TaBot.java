import com.sillysoft.lux.Board;
import com.sillysoft.lux.Card;
import com.sillysoft.lux.Country;
import com.sillysoft.lux.agent.LuxAgent;
import com.sillysoft.lux.agent.Quo;
import com.sillysoft.lux.util.BoardHelper;
import org.konzy.lux.Attack;
import org.konzy.lux.SimulationBoard;

import java.util.ArrayList;

public class TaBot extends Quo {

    private int ID;
    private Board board;
    private Country[] countries;

    private static final int MINIMUM_ARMIES_TO_ATTACK = 2;

    private void evenPlacement(int i) {
        while(i > 0) {
            int countryWithLowestArmies = -1;
            int numberOfLowestArmies = Integer.MAX_VALUE;
            for (Country country : countries) {
                if (country.getOwner() == ID && country.getArmies() < numberOfLowestArmies) {
                    countryWithLowestArmies = country.getCode();
                    numberOfLowestArmies = country.getArmies();
                }
            }
            System.out.println("");
            board.placeArmies(1, countryWithLowestArmies);
            i--;
        }
    }

    private Attack attackWeakestCountry() {
        Country easiestEnemy = board.getCountries()[getEasiestContToTake()];
        Country strongestCountry = null;
        for (Country c :
                easiestEnemy.getAdjoiningList()) {

            if (c.getOwner() == ID) {
                if (strongestCountry == null) {
                    strongestCountry = c;
                } else if (strongestCountry.getArmies() < c.getArmies()) {
                    strongestCountry = c;
                }
            }
        }
        if (strongestCountry != null) {
            return new Attack(strongestCountry, easiestEnemy, new SimulationBoard(board));
        }
        return null;
    }

    private ArrayList<Attack> determineAllPossibleAttacks() {
        ArrayList<Attack> attacks = new ArrayList<>();
        for (Country countryAttackingFrom : countries) {
            if (countryAttackingFrom.getArmies() > MINIMUM_ARMIES_TO_ATTACK) { //attack only with a minimum amount of armies
                for (int enemyCountry : countryAttackingFrom.getHostileAdjoiningCodeList()) {
                    if (countries[enemyCountry].getArmies() > countryAttackingFrom.getArmies()) { //attack only if we have more armies
                        attacks.add(new Attack(countryAttackingFrom, countries[enemyCountry], new SimulationBoard(board)));
                    }
                }
            }
        }
        return attacks;
    }

    @Override
    public void setPrefs(int i, Board board) {
        this.ID = i;
        this.board = board;
        countries = board.getCountries();
    }

    @Override
    public int pickCountry() {
        int cc = 0;
        while (countries[cc].getOwner() != -1) {
            cc++;
        }
        System.out.println("picked country " + String.valueOf(cc));
        return cc;
    }

    @Override
    public void placeInitialArmies(int i) {
        System.out.println("place armies");
        evenPlacement(i);
    }

    @Override
    public void cardsPhase(Card[] cards) {
        System.out.println("card phase");

    }

    @Override
    public void placeArmies(int i) {
        System.out.println("placed armies");
        evenPlacement(i);
    }

    @Override
    public void attackPhase() {
        System.out.println("attack phase");

        ArrayList<Attack> allPossibleAttacks = determineAllPossibleAttacks();

        SimulationBoard initialBoard = new SimulationBoard(board);

        Quo quo = new Quo();
        SimulationBoard simBoard = new SimulationBoard(board);
        quo.setPrefs(ID, simBoard);

        quo.attackPhase();
        quo.fortifyPhase();
        endOfTurnMaintenance(ID, simBoard);

        for (int id = ID + 1; id != ID; id %= (simBoard.getNumberOfPlayers() + 1)) {
            if (simBoard.countriesOwnedByPlayer(id).length > 0) {
                quo = new Quo();
                quo.setPrefs(id, simBoard);
                quo.placeArmies(simNewArmies(id, simBoard));
                quo.attackPhase();
                quo.fortifyPhase();
                endOfTurnMaintenance(id, simBoard);
            }
        }

        BoardHelper.playerOwnsAnyPositiveContinent( ID, countries, board );
        double initialRating = initialBoard.getRating(ID);
        double simRating = simBoard.getRating(ID);
    }

    private void endOfTurnMaintenance(int id, SimulationBoard board) {
        if (board.tookOverACountry()) {
            board.incrementCardsForPlayer(id);
        }
    }

    private int simNewArmies(int id, SimulationBoard board) {
        int total = board.getPlayerIncome(id);
        if (total > 0 && board.getPlayerCards(id) >= 3) {
            total += board.getNextCardSetValue();
            board.cashCardsForPlayer(id);
        }
        return total;
    }


    private SimulationBoard fullRoundSimulation(LuxAgent agent, SimulationBoard initialBoard) {
        int currentID = ID;
        int lastID = (ID + initialBoard.getNumberOfPlayers()) % initialBoard.getNumberOfPlayers();
        int newArmies = (initialBoard.numberOfCountriesOwnedByPlayer(ID) / 3);
        return null;
    }

    @Override
    public int moveArmiesIn(int cca, int ccd) {
        System.out.println("move armies in");
        return 100000;
    }

    @Override
    public void fortifyPhase() {
        System.out.println("fortify");
        return;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public float version() {
        return 0;
    }

    @Override
    public String description() {
        return "Testing";
    }

    @Override
    public String youWon() {
        return "nothing needs to go here";
    }

    @Override
    public String message(String s, Object o) {
        return "message";
    }







}



 /*
    Code for determining the amount of armies a turn in would give
    // Count the number of countries each player owns:
    int[] owned = new int[numPlayers];
    for (int p = 0; p < numPlayers; p++)
    owned[p] = 0;

    int owner;
    for (int i = 0; i < countries.length; i++)
    {
        owner = countries[i].getOwner();
        if (owner != -1)
        {
            owned[owner]++;
        }
    }

    // Now get an income for each player
    int[] incomes = new int[numPlayers];
    for (int p = 0; p < incomes.length; p++)
    {
        if (owned[p] < 1)
            incomes[p] = 0;
        else
        {
            // Divide by three (ditching any fraction):
            incomes[p] = owned[p]/3;

            // But there's a 3-army minimum from countries owned:
            incomes[p] = Math.max( incomes[p], 3);

            // Now we should see if this guy owns any continents:
            for (int i = 0; i < board.getNumberOfContinents(); i++)
            {
                if ( BoardHelper.playerOwnsContinent( p, i, countries ) )
                {
                    incomes[p] += board.getContinentBonus(i);
                }
            }

            // there can be negative continent values. give a minimum of 3 income in all cases
            incomes[p] = Math.max( incomes[p], 3);
        }
    }

    */