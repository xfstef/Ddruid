package com.frostbytetree.ddruid;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by XfStef on 1/16/2016.
 */

    //TODO Load the Config File
    //TODO Interpret the Config File

public class ConfigFileInterpreter {
    ConfigFile configFile;
    WidgetViews widgetViews;
    JSONArray widgets = new JSONArray();

    Data data;
    JSONArray model_structure = new JSONArray();


    Context context;
    MainActivity my_main;

    private static ConfigFileInterpreter ourInstance = new ConfigFileInterpreter();

    public static ConfigFileInterpreter getInstance() {
        return ourInstance;
    }

    private ConfigFileInterpreter() {
        configFile = ConfigFile.getInstance();
    }

    public void buildDataModels(){
        synchronized (configFile.cfg_file_lock){
            data = Data.getInstance();

            try{
                model_structure = configFile.json_form.getJSONArray("model_structure");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int x = 0; x < model_structure.length(); x++){
                Table new_table = new Table();

                try {
                    JSONObject temp_table = model_structure.getJSONObject(x);
                    new_table.table_name = temp_table.getString("name");
                    new_table.cached_only = temp_table.getBoolean("is_locally_cached");
                    JSONArray attributes = temp_table.getJSONArray("attributes");
                    new_table.attribute_count = attributes.length();
                    new_table.attributes = new ArrayList<Attribute>(new_table.attribute_count);
                    addAttibutes(new_table, attributes);
                    JSONObject references = new JSONObject();
                    references = temp_table.getJSONObject("references");
                    addReferenceAttributes(new_table, references);

                    // TODO: Read and add the actions

                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }

                new_table.dataSets = new ArrayList<DataSet>();

                synchronized (data.data_lock){
                    data.tables.add(new_table);
                }
            }

            linkTablesToWidgets();
        }
    }

    private void addReferenceAttributes(Table new_table, JSONObject references) {
        JSONArray reference_names = references.names();

        for(int z = 0; z < references.length(); z++){
            try {
                JSONArray temp_ref = new JSONArray();
                temp_ref = references.getJSONArray(reference_names.getString(z));

                Spinner new_spinner = new Spinner();
                new_spinner.items = new ArrayList<String>(temp_ref.length());
                for(int q = 0; q < temp_ref.length(); q++)
                    new_spinner.items.add(temp_ref.getString(q));
                for(int w = 0; w < new_table.attributes.size(); w++)
                    if(new_table.attributes.get(w).attribute_type == 2 &&
                            reference_names.getString(z).matches(new_table.attributes.get(w).spinner_name))
                        new_table.attributes.get(w).items = new_spinner;
            } catch (JSONException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void linkTablesToWidgets() {
        synchronized (data.data_lock){
            for(int x = 0; x < widgetViews.the_widgets.size()-1; x++)   // Only goes to n-1 because the last Widget is the Menu Widget
            // that doesn't have any tables or actions.
                for(int y = 0; y < widgetViews.the_widgets.get(x).myTableNames.size(); y++) {
                    widgetViews.the_widgets.get(x).myTables = new ArrayList<Table>(widgetViews.the_widgets.get(x).myTableNames.size());
                    for (int z = 0; z < data.tables.size(); z++)
                        if (widgetViews.the_widgets.get(x).myTableNames.get(y).matches(data.tables.get(z).table_name))
                            widgetViews.the_widgets.get(x).myTables.add(data.tables.get(z));
                }
        }
    }

    private void addAttibutes(Table new_table, JSONArray attributes) {
        for(int y = 0; y < attributes.length(); y++){
            Attribute new_attribute = new Attribute();
            try {
                JSONObject temp_attribute = attributes.getJSONObject(y);
                new_attribute.name = temp_attribute.getString("name");
                new_attribute.attribute_description = temp_attribute.getString("description");
                String type = temp_attribute.getString("data_type");
                switch (type){
                    case "text":
                        new_attribute.attribute_type = 0;
                        break;
                    case "integer":
                        new_attribute.attribute_type =  0;
                        break;
                    case "date":
                        new_attribute.attribute_type = 5;
                        break;
                    case "string":
                        new_attribute.attribute_type = 0;
                        break;
                    case "timestamp_with_timezone":
                        new_attribute.attribute_type = 6;
                        break;
                }
                if(temp_attribute.length() > 3){
                    new_attribute.attribute_type = 2;
                    new_attribute.items = new Spinner();
                    new_attribute.spinner_name = temp_attribute.getString("reference");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new_table.attributes.add(new_attribute);
        }

    }


    public void buildWidgets(){

        synchronized (configFile.cfg_file_lock){
            widgetViews = WidgetViews.getInstance();

            try {
                widgets = configFile.json_form.getJSONArray("widgets");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int x = 0; x < widgets.length(); x++) {

                Widget new_widget = new Widget(context);
                new_widget.id = x;
                new_widget.myTableNames = new ArrayList<String>();
                new_widget.myActionNames = new ArrayList<String>();
                new_widget.myChildren = new ArrayList<Widget>();

                try {
                    JSONObject temp_obj = new JSONObject();
                    temp_obj = widgets.getJSONObject(x);
                    switch (temp_obj.getString("type")){
                        case "list":
                            new_widget.widgetType = 4;
                            break;
                        case "form":
                            new_widget.widgetType = 1;
                            break;
                        // TODO: Implement the rest widget types.
                    }
                    new_widget.titleBar = temp_obj.getString("name");
                    if(temp_obj.length() > 3)
                        new_widget.myParentName = temp_obj.getString("parent");
                    else
                        new_widget.myParentName = "Main Menu";
                    JSONArray action_list = temp_obj.getJSONArray("action");
                    for(int y = 0; y < action_list.length(); y++){
                        JSONObject temp_action_obj = action_list.getJSONObject(y);
                        Iterator<String> the_keys = temp_action_obj.keys();
                        String temp_key = the_keys.next();
                        new_widget.myTableNames.add(temp_key);
                        new_widget.myActionNames.add(temp_action_obj.getString(temp_key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }

                widgetViews.the_widgets.add(new_widget);
            }

            buildMenuAndChildren();
        }

    }

    private void buildMenuAndChildren() {
        Widget menu_widget = new Widget(context);
        menu_widget.id = widgetViews.the_widgets.size();
        menu_widget.widgetType = 0;
        menu_widget.titleBar = "Main Menu";
        menu_widget.myChildren = new ArrayList<Widget>();

        widgetViews.the_widgets.add(menu_widget);

        for(int x = 0; x < widgetViews.the_widgets.size(); x++){
            Widget temp1 = widgetViews.the_widgets.get(x);
            for(int y = 0; y < widgetViews.the_widgets.size()-1; y++){
                Widget temp2 = widgetViews.the_widgets.get(y);
                if(temp1.titleBar.matches(temp2.myParentName))
                    temp1.myChildren.add(temp2);
            }
        }

        my_main.switchWidget(widgetViews.the_widgets.get(widgetViews.the_widgets.size()-1));
    }


}
