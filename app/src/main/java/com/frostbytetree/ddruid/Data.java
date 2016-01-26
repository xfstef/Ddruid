package com.frostbytetree.ddruid;

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
    Object data_lock = new Object();

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
    ArrayList<DataSet> dataSets;
    ArrayList<Widget> usedBy;
    ArrayList<Action> myActions;
}

class DataSet{
    ArrayList<String> set;
}

class Attribute{
    String name;
    String attribute_description;   // Maybe irrelevant ?!?!
    short attribute_type;
    // Here we declare what the different attribute types are:
    // 0 - Text View;
    // 1 - Edit Text;
    // 2 - Spinner;
    // 3 - Check Box;
    // 4 - Image;
    // 5 - Date;
    // 6 - Timestamp;
    // ...

    // By default NULL. Only needed if this attribute is a spinner.
    String spinner_name = null;
    Spinner items = null;
}

class Action{
    String name;
    ArrayList<Attribute> attributes;
    ArrayList<Boolean> attribute_required;
    ArrayList<Boolean> attribute_readonly;
}

class Spinner{
    String myName;
    ArrayList<String> dataSetName;
    ArrayList<String> items;
}
