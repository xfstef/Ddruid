package com.frostbytetree.ddruid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

    public void processTableData(Table table){
        // TODO: As of now every data type is converted to String. This should be changed in the future,
        // since a lot of the data are of other types.
        synchronized (data.temp_object_lock){
            try {
                JSONArray data_list = data.temp_object.getJSONArray(table.table_name);
                table.dataSets = new ArrayList<DataSet>(data_list.length());
                for(int x = 0; x < data_list.length(); x++){
                    DataSet new_set = new DataSet();
                    JSONObject set = data_list.getJSONObject(x);
                    JSONArray attribute_names = set.names();
                    new_set.set = new ArrayList<String>(set.length());
                    for(int y = 0; y < set.length(); y++){
                        new_set.set.add(set.getString(attribute_names.getString(y)));
                    }
                    table.dataSets.add(new_set);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        data.temp_object = null;
    }
}
