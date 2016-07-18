package org.konzy.lux;//Created By: Brian Konzman on 7/17/2016 

import com.sillysoft.lux.Country;

public class Utils {

    // Create a copy of the countries array, for simulation
    public static Country[] getCountriesCopy(Country[] countries) //Made by Bertrand http://sillysoft.net/forums/memberlist.php?mode=viewprofile&u=1753
    {
        Country[] countriesCopy = new Country[countries.length];
        // pass 1: allocate the countries
        for (int i = 0; i < countries.length; i++)
        {
            countriesCopy[i] = new Country(i, countries[i].getContinent(), null);
            countriesCopy[i].setArmies(countries[i].getArmies(), null);
            countriesCopy[i].setName(countries[i].getName(), null);
            countriesCopy[i].setOwner(countries[i].getOwner(), null);
        }
        // pass 2: create the AdjoiningLists
        for (int i = 0; i < countries.length; i++)
        {
            Country[] around = countries[i].getAdjoiningList();
            for (int j = 0; j < around.length; j++)
                countriesCopy[i].addToAdjoiningList(countriesCopy[around[j].getCode()], null);
        }
        return countriesCopy;
    }
}
