package com.frostbytetree.ddruid;

import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Class is used to store the current Configurations into the memory

    //TODO Establish Data Structure

public class ConfigFile {
    private static ConfigFile ourInstance = new ConfigFile();
    JSONObject json_form = null;
    Object cfg_file_lock = new Object();
    String server_uri = null;

    public static ConfigFile getInstance() {
        return ourInstance;
    }

    private ConfigFile() {
    }
}

class SclableURIS {
    private static SclableURIS ourInstance = new SclableURIS();
    String login = null;
    String config = null;
    String data = null;
    String data_single = null;

    public static SclableURIS getInstance() {
        return ourInstance;
    }

    private SclableURIS() {
    }
}
