package com.frostbytetree.ddruid;

import android.util.Pair;
import android.widget.ArrayAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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

    // This Function acts as a SELECT * FROM ... SQL Query. The result is a DataSet with all found rows.
    public void executeLookup(LookupTable lookupTable, ArrayList<String> parameter){

        ArrayList<Short> searched_indices = new ArrayList<>();
        for(int x = 0; x < lookupTable.lookup_strings.size(); x += 5) {
            short searched_index = getIndexOfAttribute(lookupTable.referenced_table, lookupTable.lookup_strings.get(x+1));
            searched_indices.add(searched_index);
        }
        lookupTable.results = getSetData(lookupTable.referenced_table, searched_indices, parameter);
    }

    public String getXFromYWhereZ(String target_table, String target_attribute, String target_value){
        String result = null;
        Table table = getTable(target_table);
        short taget_column = getIndexOfAttribute(table, target_attribute);
        ArrayList<DataSet> searched = new ArrayList<>();

        for(int x = 0; x < table.dataSets.size(); x++) {
            DataSet new_set = new DataSet();
            new_set.set = new ArrayList<>();
            ArrayList<String> maybe = new ArrayList<>();
            maybe = null;
            for (int y = 0; y < table.attributes.size(); y++)
                if (table.dataSets.get(x).set.get(y).matches(target_value))
                    maybe = table.dataSets.get(x).set;
                else
                    maybe = null;
            if(maybe != null) {
                new_set.set.add(String.valueOf(maybe));
                searched.add(new_set);
            }
        }

        if(searched.size() > 0)
            result = searched.get(0).set.get(taget_column);

        return result;
    }

    // This function is used to filter down the result attributes.
    public ArrayList<String> filterColumn(LookupTable lookupTable, String column_name){
        ArrayList<String> result = new ArrayList<>();

        short column_index = getIndexOfAttribute(lookupTable.referenced_table, column_name);
        if(column_index >= 0)
            for(int x = 0; x < lookupTable.results.size(); x++)
                result.add(lookupTable.results.get(x).set.get(column_index));

        return result;
    }

    public Table getTable(String name){
        for(int x = 0; x < tables.size(); x++)
            if(tables.get(x).table_name.matches(name))
                return tables.get(x);

        return null;
    }

    public short getIndexOfAttribute(Table table, String attribute){
        if(table == null)
            return -1;

        for(int x = 0; x < table.attributes.size(); x++)
            if(table.attributes.get(x).name.matches(attribute))
                return (short) x;

        return -1;
    }

    public ArrayList<DataSet> getSetData(Table table, ArrayList<Short> indexes, ArrayList<String> parameters){
        if (table == null)
            return null;
        if(indexes.size() != parameters.size())
            return null;

        ArrayList<DataSet> searched = new ArrayList<>();

        for(int x = 0; x < table.dataSets.size(); x++) {
            DataSet new_set = new DataSet();
            new_set.set = new ArrayList<>();
            ArrayList<String> maybe = new ArrayList<>();
            maybe = null;
            for (int y = 0; y < indexes.size(); y++)
                if (table.dataSets.get(x).set.get(indexes.get(y)).matches(parameters.get(y)))
                    maybe = table.dataSets.get(x).set;
                else
                    maybe = null;
            if(maybe != null) {
                new_set.set = maybe;
                searched.add(new_set);
            }
        }

        return searched;
    }

    public Pair<String, String> splitTableFromAttribute(String input){
        Pair<String, String> the_pair = null;

        for(int x = input.length()-1; x >= 0 ; x--)
            if(input.charAt(x) == '.') {
                the_pair = new Pair<>(input.substring(0, x), input.substring(x+1, input.length()));
                return the_pair;
            }

        return the_pair;
    }

}

class Table{
    String table_name;
    Boolean cached_only;
    int attribute_count;
    ArrayList<Attribute> attributes;
    ArrayList<DataSet> dataSets = new ArrayList<>();
    ArrayList<Widget> usedBy;
    ArrayList<Action> myActions;
    ArrayList<Table> children = new ArrayList<>();
    ArrayList<String> sclable_states;
}

class DataSet{
    // TODO: Enable more data types.
    ArrayList<String> set = new ArrayList<>();
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
    String reference_name;
    Spinner items = null;
    Table vader = null;    // I am your father !
}

class Action{
    String name;
    short type; // 0 - For create new data set;
                // 1 - For simple edit data set;
                // 2 - For complex edit with form;
                // 3 - For delete data set.
                // 4 - Read action.
    String sclablePreState;
    String sclablePostState;
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
    Table referenced_table = null;
    ArrayList<String> dataSetName;
    ArrayList<DataSet> items;
    int source_column;
    ArrayList<Integer> target_columns;
}

// This class is used to prepare data that requires a lookup.
class LookupTable{
    short uses = 0; // This describes what kind of input device the lookup needs on the UI.
                    // 0 - Standard for keyboard;
                    // 1 - Scanner;
                    // 2 - GPS;
                    // 3 - Selection (spinner).
    String referenced_table_name = null;
    Table referenced_table = null; // Table used for the Query.

    ArrayList<String> lookup_strings = new ArrayList<>();   // Strings needed for the Query.
    ArrayList<DataSet> results = new ArrayList<>();   // The result from the Query.
}