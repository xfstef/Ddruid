package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Class is used to store all or most needed Data into memory

    //TODO Establish the data structures

public class RawData {
    private static RawData ourInstance = new RawData();
    private String test;
    private boolean persistancy = false;

    public static RawData getInstance() {
        return ourInstance;
    }

    private RawData() {
    }

    synchronized public void setTest(String test) {
        this.test = test;
    }

    synchronized public String getTest(){
        return this.test;
    }

    synchronized public void setPersistancy(boolean pers) {
        this.persistancy = pers;
    }

    synchronized public boolean getPersistancy(){
        return this.persistancy;
    }
}
