package com.frostbytetree.ddruid;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by XfStef on 11/27/2015.
 */

// This container is used to store all the inflators

public class WidgetViews {

    private static WidgetViews ourInstance = new WidgetViews();

    Context context;
    DataModels data_models;
    ArrayList<Widget> the_widgets;

    public static WidgetViews getInstance() {
        return ourInstance;
    }

    public WidgetViews() {
        this.data_models = DataModels.getInstance();

    }

}

class Widget extends View{

    Context context;
    LinearLayout L2;

    public Widget(Context context) {
        super(context);
        this.context = context;
        L2 = new LinearLayout(context);
        L2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        L2.setGravity(Gravity.CENTER);
    }

    public void addElement(){
        TextView a1 = new TextView(context);
        a1.setText("Dynamic layouts ftw!");
        a1.setVisibility(View.VISIBLE);
        L2.addView(a1, (new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)));
    }
}
