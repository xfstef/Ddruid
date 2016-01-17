package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Thread runs in the Background and any changes made to the Config File

    //TODO Signal the Data Transfer Controller if there were problems
    //TODO Build the Data Models
    //TODO Signal the UIBuilder

public class DataModelInterpreter {

    Data data;

    private static DataModelInterpreter ourInstance = new DataModelInterpreter();

    public static DataModelInterpreter getInstance() {
        return ourInstance;
    }

    private DataModelInterpreter() {

        data = Data.getInstance();

    }
}
