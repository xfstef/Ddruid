package com.frostbytetree.ddruid;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Class is used to store all or most needed Data into memory

    //TODO Establish the data structures

public class Data {
    private static Data ourInstance = new Data();
    private String test;
    private boolean persistancy = false;
    ArrayList<Table> tables;
    JSONObject temp_object = null;
    Object data_lock = new Object();
    Object temp_object_lock = new Object();

    public static Data getInstance() {
        return ourInstance;
    }

    private Data() {
        tables = new ArrayList<Table>();
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

class Table{
    String table_name;
    Boolean cached_only;
    int attribute_count;
    ArrayList<Attribute> attributes;
    ArrayList<DataSet> dataSets = new ArrayList<>(1);
    ArrayList<Widget> usedBy;
    ArrayList<Action> myActions;
}

class DataSet{
    // TODO: Enable more data types.
    ArrayList<String> set = new ArrayList<>(1);
}

class Attribute{
    String name;
    String attribute_description;   // Maybe irrelevant ?!?!
    short attribute_type;
    // Here we declare what the different attribute types are:
    // 0 - Text View;
    // 2 - Spinner;
    // 3 - Check Box;
    // 4 - Image;
    // 5 - Date;
    // 6 - Timestamp;
    // ...

    // By default NULL. Only needed if this attribute is a spinner.
    String spinner_name = null;
    String reference_name = null;
    Spinner items = null;
}

class Action{
    String name;
    short type; // 0 - For create new data set;
                // 1 - For simple edit data set;
                // 2 - For complex edit with form;
                // 3 - For delete data set.
    short sclablePreState = 0;
    short sclablePostState = 0;
    // The sclable States can be:
    // 0 - null;
    // 1 - incoming;
    // 2 - second_level;
    // 3 - done.
    ArrayList<Attribute> attributes;
    ArrayList<Boolean> attribute_required;
    ArrayList<Boolean> attribute_readonly;
}

class Spinner{
    String myName;
    ArrayList<String> dataSetName;
    ArrayList<String> items;
}
