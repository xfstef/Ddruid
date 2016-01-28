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
    //LinearLayout widgetLinearLayout;
    int id;
    ArrayList<Table> myTables;
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
                        // 4 - List with datasets
                        // 31 - Code Scanner + GPS;
                        // ...

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



    /*
    public void addElement(){
        Button a1 = new Button(context);
        a1.setText("@string/app_name");
        a1.setVisibility(View.VISIBLE);
        addView(a1, (new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)));
        System.out.println("Element Should be added!");
        Activity MainActivity = (Activity)context;
        //MainActivity.setContentView(widgetLinearLayout);
    }
    */

}
