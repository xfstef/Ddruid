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
    JSONObject temp_obj = new JSONObject();
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
        }

        buildMenuAndChildren();
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
