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

    // TODO: This function should be split in half. The first half should be ran the first time the
    // step is initialized. Additional attribute definitions will be required in the Step class.
    public ArrayList<String> prepareStepSuccessUI(Step currentStep) {
        ArrayList<String> result = new ArrayList<>();

        for(int d = 0; d < currentStep.lookupTable.results.size(); d++) {
            String partial_result = new String();
            for (int x = 0; x < currentStep.result_definitions.size(); x++) {
                switch (currentStep.result_definitions.get(x)) {
                    case "string":
                        partial_result += currentStep.result_attributes.get(x);
                        break;
                    case "result":
                        Pair<String, String> table_attr = data_model.splitTableFromAttribute(currentStep.result_attributes.get(x));
                        partial_result += data_model.filterColumn(currentStep.lookupTable, table_attr.second).get(d);
                        break;
                    // TODO: See if you can get a better definition for this lookup, at the moment it is very suspicious!
                    default:
                        Pair<String, String> left_side = data_model.splitTableFromAttribute(currentStep.result_definitions.get(x));
                        Pair<String, String> right_side = data_model.splitTableFromAttribute(currentStep.result_attributes.get(x));
                        ArrayList<String> left_lookup = new ArrayList<>();
                        left_lookup.add(data_model.filterColumn(currentStep.lookupTable, left_side.second).get(d));
                        for(int e = 0; e < left_lookup.size(); e++)
                            partial_result += data_model.getXFromYWhereZ(right_side.first, right_side.second, left_lookup.get(e));
                        break;
                }
            }
            result.add(partial_result);
        }

        return result;
    }

}

class Widget extends LinearLayout{

    Context context;
    int id;
    ArrayList<Table> myTables = new ArrayList<>();
    ArrayList<Action>myActions = new ArrayList<>();
    ArrayList<Pair<String, String>> myTableActions = new ArrayList<>();   // {["ticket", "create"],["ticket", "forward"]}

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
        //ArrayList<Widget> myChildren = new ArrayList<Widget>();
        myParent = null;
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
    }

    public Step getStep(String step){
        Step result = new Step();

        for(int x = 0; x < steps.size(); x++)
            if(steps.get(x).name.matches(step))
                return steps.get(x);

        return result;
    }
}

class Step{
    String name;
    short ui_element_type = 99; // This defines what type of UI element the step uses.
                                // 0 - Text View;
                                // 1 - Recycler View;
                                // 2 - Button;
                                // 3 - ...;
                                // 99 - NO UI.
    Step parent_step = null;

    String ui_label;
    String label_definition;
    LookupTable lookupTable;
    String action_table_name = null;
    Action action;
    ArrayList<String> action_attributes;

    String next_step_if_success = null;
    Step next_if_success = null;
    String success_label = null;
    ArrayList<String> result_definitions = new ArrayList<>();
    ArrayList<String> result_attributes = new ArrayList<>();

    String next_step_if_error = null;
    Step next_if_error = null;
    String error_message = null;

}
