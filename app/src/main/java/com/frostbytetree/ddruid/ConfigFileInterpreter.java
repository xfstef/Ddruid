package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 1/16/2016.
 */

    //TODO Load the Config File
    //TODO Interpret the Config File

public class ConfigFileInterpreter {
    ConfigFile configFile;

    private static ConfigFileInterpreter ourInstance = new ConfigFileInterpreter();

    public static ConfigFileInterpreter getInstance() {
        return ourInstance;
    }

    private ConfigFileInterpreter() {
        configFile = ConfigFile.getInstance();
    }

    public void buildDataModels(){

    }

    public void buildWidgets(){
        synchronized (configFile.cfg_file_lock){
            System.out.println("The object: " + configFile.json_form.toString());
        }
    }
}
