package com.frostbytetree.ddruid;

import android.content.Context;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by XfStef on 1/16/2016.
 */

    //Loads the Config File
    //Interprets the Config File
    //TODO: Build try catch for every JSON object interpretation in order to catch problems
    // independently of each other.

public class ConfigFileInterpreter {
    ConfigFile configFile;
    WidgetViews widgetViews;
    JSONArray widgets = new JSONArray();
    SclableInterpreter sclableInterpreter;
    AppLogic appLogic;

    Data data;
    JSONArray model_structure = new JSONArray();

    Context context;

    private static ConfigFileInterpreter ourInstance = new ConfigFileInterpreter();

    public static ConfigFileInterpreter getInstance() {
        return ourInstance;
    }

    private ConfigFileInterpreter() {
        configFile = ConfigFile.getInstance();
        sclableInterpreter = SclableInterpreter.getInstance();
    }

    public void startStartupProcess(){
        synchronized (configFile.cfg_file_lock) {

            try {
                String backend = configFile.json_form.getString("backend");
                switch (backend){
                    case "proprietary":
                        appLogic.backend = 0;
                        // TODO: Implement interpreter for our own server backend.
                        break;
                    case "sclable":
                        appLogic.backend = 1;
                        sclableInterpreter.buildWidgets();
                        sclableInterpreter.buildDataModels();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}

class SclableInterpreter {
    ConfigFile configFile;
    WidgetViews widgetViews;
    JSONArray widgets = new JSONArray();

    Data data;
    JSONArray model_structure = new JSONArray();

    Context context;

    private static SclableInterpreter ourInstance = new SclableInterpreter();

    public static SclableInterpreter getInstance() {
        return ourInstance;
    }

    private SclableInterpreter() {
        configFile = ConfigFile.getInstance();
        data = Data.getInstance();
    }

    public void buildDataModels(){

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

                JSONArray actions = temp_table.getJSONArray("actions");
                new_table.myActions = new ArrayList<Action>(actions.length());
                addActions(new_table, actions);

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

    private void addActions(Table table, JSONArray actions) {

        for(int o = 0; o < actions.length(); o++){
            Action new_action = new Action();
            try {
                JSONObject the_action = actions.getJSONObject(o);
                new_action.name = the_action.getString("name");

                JSONObject sclable_transition = the_action.getJSONObject("transition");
                String type = sclable_transition.getString("type");
                switch (type) {
                    case "create":
                        new_action.type = 0;
                        break;
                    case "edit":
                        new_action.type = 1;
                        break;
                    case "delete":
                        new_action.type = 2;
                        break;
                }
                type = sclable_transition.getString("pre_state");
                switch (type){
                    case "incoming":
                        new_action.sclablePreState = 1;
                        break;
                    case "second_level":
                        new_action.sclablePreState = 2;
                        break;
                    case "done":
                        new_action.sclablePreState = 3;
                        break;
                }
                type = sclable_transition.getString("post_state");
                switch (type){
                    case "incoming":
                        new_action.sclablePostState = 1;
                        break;
                    case "second_level":
                        new_action.sclablePostState = 2;
                        break;
                    case "done":
                        new_action.sclablePostState = 3;
                        break;
                }

                JSONArray act_attr = the_action.getJSONArray("action_attributes");
                if(act_attr.length() > 1)
                    new_action.type = 2;
                new_action.attributes = new ArrayList<Attribute>(act_attr.length());
                new_action.attribute_readonly = new ArrayList<Boolean>(act_attr.length());
                new_action.attribute_required = new ArrayList<Boolean>(act_attr.length());
                for(int p = 0; p < act_attr.length(); p++){
                    JSONObject first_act_attr = act_attr.getJSONObject(p);
                    for(int l = 0; l < table.attributes.size(); l++)
                        if(table.attributes.get(l).name.matches(first_act_attr.getString("name"))){
                            new_action.attributes.add(table.attributes.get(l));
                            new_action.attribute_readonly.add(first_act_attr.getBoolean("read_only"));
                            new_action.attribute_required.add(first_act_attr.getBoolean("required"));
                            break;
                        }
                }
                table.myActions.add(new_action);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void linkTablesToWidgets() {
        synchronized (data.data_lock){
            for(int x = 0; x < widgetViews.the_widgets.size()-1; x++)   // Only goes to n-1 because the last Widget is the Menu Widget
                // that doesn't have any tables or actions.
                for(int y = 0; y < widgetViews.the_widgets.get(x).myTableActions.size(); y++) {
                    widgetViews.the_widgets.get(x).myTables = new ArrayList<Table>(widgetViews.the_widgets.get(x).myTableActions.size());
                    for (int z = 0; z < data.tables.size(); z++)
                        if (widgetViews.the_widgets.get(x).myTableActions.get(y).first.matches(data.tables.get(z).table_name))
                            widgetViews.the_widgets.get(x).myTables.add(data.tables.get(z));
                }
        }

        /*for(int u = 0; u < widgetViews.the_widgets.size()-1; u++)
            System.out.println("Table: " + widgetViews.the_widgets.get(u).myTables.get(0).table_name +
            ", action: " + widgetViews.the_widgets.get(u).myActionNames.get(0));*/
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
                    JSONObject ref = new JSONObject();
                    ref = temp_attribute.getJSONObject("reference");
                    new_attribute.spinner_name = temp_attribute.getString("name");
                    new_attribute.reference_name = ref.getString("name");
                    JSONArray reference_names = new JSONArray();
                    reference_names = ref.getJSONArray("reference_lookup");
                    new_attribute.items.dataSetName = new ArrayList<String>(reference_names.length());
                    for(int g = 0; g < reference_names.length(); g++){
                        new_attribute.items.dataSetName.add(reference_names.getString(g));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new_table.attributes.add(new_attribute);
        }

    }

    // Builds the Widget logical structure from the data given in the Config File.
    public void buildWidgets(){

        widgetViews = WidgetViews.getInstance();

        try {
            widgets = configFile.json_form.getJSONArray("widgets");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int x = 0; x < widgets.length(); x++) {

            Widget new_widget = new Widget(context);
            new_widget.id = x;
            //new_widget.myTableNames = new ArrayList<String>();
            //new_widget.myActionNames = new ArrayList<String>();
            new_widget.myTableActions = new ArrayList<>();
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
                if(temp_obj.length() > 3) {
                    JSONArray temp_parents = temp_obj.getJSONArray("parents");
                    new_widget.myParentNames = new ArrayList<String>();
                    for(int y = 0; y < temp_parents.length(); y++)
                        new_widget.myParentNames.add(temp_parents.getString(y));
                }
                else {
                    new_widget.myParentNames = new ArrayList<String>(1);
                    new_widget.myParentNames.add(0, "Main Menu");
                }
                JSONArray action_list = temp_obj.getJSONArray("action");
                for(int y = 0; y < action_list.length(); y++){
                    JSONObject temp_action_obj = action_list.getJSONObject(y);
                    Iterator<String> the_keys = temp_action_obj.keys();
                    String temp_key = the_keys.next();
                    //new_widget.myTableNames.add(temp_key);
                    //new_widget.myActionNames.add(temp_action_obj.getString(temp_key));
                    new_widget.myTableActions.add(new Pair<String, String>(temp_key, temp_action_obj.getString(temp_key)));

                }
            } catch (JSONException e) {
                e.printStackTrace();
                break;
            }

            widgetViews.the_widgets.add(new_widget);
        }

        buildMenuAndChildren();


    }

    // Builds the Widget Menu and sets all parent to child relationships. Afterwards it calls the
    // instancing of the Widget Menu.
    private void buildMenuAndChildren() {
        Widget menu_widget = new Widget(context);
        menu_widget.id = widgetViews.the_widgets.size();
        menu_widget.widgetType = 0;
        menu_widget.titleBar = "Main Menu";
        menu_widget.myChildren = new ArrayList<Widget>();

        widgetViews.the_widgets.add(menu_widget);

        // This structure links widgets together according to their parental relationship.
        for(int x = 0; x < widgetViews.the_widgets.size(); x++){
            Widget temp1 = widgetViews.the_widgets.get(x);
            for(int y = 0; y < widgetViews.the_widgets.size()-1; y++){
                Widget temp2 = widgetViews.the_widgets.get(y);
                for(int z = 0; z < temp2.myParentNames.size(); z++)
                    if(temp1.titleBar.matches(temp2.myParentNames.get(z)))
                        temp1.myChildren.add(temp2);
            }
        }

    }

}
