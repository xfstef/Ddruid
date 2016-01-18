package com.frostbytetree.ddruid;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by XfStef on 1/16/2016.
 */

    //TODO Load the Config File
    //TODO Interpret the Config File

public class ConfigFileInterpreter {
    ConfigFile configFile;
    WidgetViews widgetViews;
    JSONArray widgets = new JSONArray();
    JSONObject temp_obj = new JSONObject();
    Context context;

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
            short type = 0;
            String name = null;
            String parent = null;
            widgetViews = WidgetViews.getInstance();
            try {
                widgets = configFile.json_form.getJSONArray("widgets");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("The object: " + widgets.toString());
            for(int x = 0; x < widgets.length(); x++) {
                try {
                    temp_obj = widgets.getJSONObject(x);
                    switch (temp_obj.getString("type")){
                        case "list":
                            type = 4;
                            break;
                        case "form":
                            type = 1;
                            break;
                        // TODO: Implement the rest widget types.
                    }
                    name = temp_obj.getString("name");
                    if(temp_obj.length() > 3)
                        parent = temp_obj.getString("parent");
                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }

                Widget new_widget = new Widget(context);
                new_widget.id = x;
                new_widget.widgetType = type;
                new_widget.titleBar = name;

                new_widget.myTables = null;
                new_widget.myActions = null;
            }
        }
    }
}
