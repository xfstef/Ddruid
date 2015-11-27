package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Class is used to store the current Configurations into the memory

    //TODO Establish Data Structure

public class ConfigFile {
    private static ConfigFile ourInstance = new ConfigFile();

    public static ConfigFile getInstance() {
        return ourInstance;
    }

    private ConfigFile() {
    }
}
