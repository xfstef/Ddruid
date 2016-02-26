package com.frostbytetree.ddruid;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by XfStef on 11/27/2015.
 */

// This container is used to store all the inflators

public class WidgetViews {

    private static WidgetViews ourInstance = new WidgetViews();

    Context context;
    Data data_model;
    ArrayList<Widget> the_widgets;
    Widget default_widget;
    boolean no_default_widget = true;

    public static WidgetViews getInstance() {
        return ourInstance;
    }

    public WidgetViews() {
        this.data_model = Data.getInstance();
        the_widgets = new ArrayList<Widget>();
    }

}

class Widget extends LinearLayout{

    Context context;
    int id;
    ArrayList<Table> myTables = new ArrayList<>();
    ArrayList<Action>myActions = new ArrayList<>();
    ArrayList<Pair<String, String>> myTableActions;   // {["ticket", "create"],["ticket", "forward"]}

    ArrayList<Widget> myChildren;
    Widget myParent; // By default just one parent but there may exist cases with multiple parents.
    ArrayList<String> myParentNames;
    String titleBar;
    short widgetType;   // This is the type of widget that needs to be set according to what it does
                        // 0 - Widget-List;
                        // 1 - Form;
                        // 2 - Detail - could be never used;
                        // 3 - Code Scanner;
                        // 4 - List with datasets;
                        // 5 - Complex for Hoerbiger;
                        // 31 - Code Scanner + GPS;
                        // ...

    LinkedHashMap<Integer, ArrayList<Integer>> list_view_columns; // This variable defines the
    // attributes that should be visible in list widgets.

    ArrayList<Step> steps = new ArrayList<>();  // These are needed for complex widgets.

    public Widget(Context context) {
        super(context);
        this.context = context;
        //widgetLinearLayout = new LinearLayout(context);
        ArrayList<Widget> myChildren = new ArrayList<Widget>();
        myParent = null;
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
    }
}

class Step{
    String name;
    short ui_element_type;  // This defines what type of UI element the step uses.
                            // 0 - Text View;
                            // 1 - Recycler View;
                            // 2 - ...
    String ui_label;
    LookupTable lookupTable;
}
