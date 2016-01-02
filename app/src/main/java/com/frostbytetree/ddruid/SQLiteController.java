package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Background Thread is used to store and load the data from a local SQLite DB.

    //TODO Interface with the SQLite DB
    //TODO Load Raw Data from memory and update the DB if needed
    //TODO Load the Config File from memory and update the DB if needed
    //TODO Provide Raw Data to the app if mode is "starting" or "offline"
    //TODO Provide the Config File to the app if mode is "starting" or "offline"

public class SQLiteController extends Thread{
    private static SQLiteController ourInstance = new SQLiteController();
    private static short my_id = 3;
    IACInterface commInterface = IACInterface.getInstance();

    public static SQLiteController getInstance() {
        return ourInstance;
    }

    private SQLiteController() {
    }
}
