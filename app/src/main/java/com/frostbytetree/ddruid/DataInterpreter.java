package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Thread runs in the Background and any changes made to the Config File

    //TODO Signal the Data Transfer Controller if there were problems
    //TODO Build the Data Models
    //TODO Signal the UIBuilder

public class DataInterpreter {

    Data data;

    private static DataInterpreter ourInstance = new DataInterpreter();

    public static DataInterpreter getInstance() {
        return ourInstance;
    }

    private DataInterpreter() {

        data = Data.getInstance();

    }
}
