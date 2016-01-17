package com.frostbytetree.ddruid;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.zip.Inflater;

/**
 * Created by XfStef on 11/27/2015.
 */

// The UI Builder is a background Thread that uses the Data Models in order to create the inflators
// needed for the various app Widgets.

    //TODO Load the Data Models
    //TODO Interpret the Data Models
    //TODO Build the Widget Inflators ???
    //TODO Signal the AppLogic to start the TemporaryWidget Activity

public class UIBuilder {
    private static UIBuilder ourInstance = new UIBuilder();

    Data data;
    Context context;
    WidgetViews widgetViews;

    public static UIBuilder getInstance() {
        return ourInstance;
    }

    private UIBuilder() {
        this.data = data.getInstance();
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void inflate_model(Table current_model){

        //TODO build View from data model
        Widget test = new Widget(context);
        System.out.println("Widget Test:" + test);
        test.addElement();

        //widgetViews.the_widgets.add(test);

    }
}
