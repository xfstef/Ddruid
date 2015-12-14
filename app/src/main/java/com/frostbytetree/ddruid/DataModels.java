package com.frostbytetree.ddruid;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

//This Class is used to store the current Data Models and the needed widgets into memory

    //TODO Establish the data structures

public class DataModels {
    private static DataModels ourInstance = new DataModels();
    ArrayList<Model> all_models;

    public static DataModels getInstance() {
        return ourInstance;
    }

    private DataModels() {
    }
}

class Model {

}
