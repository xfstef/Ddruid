package com.frostbytetree.ddruid;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

class Widget extends LinearLayout{

    Context context;
    LinearLayout L2;

    public Widget(Context context) {
        super(context);
        this.context = context;
        L2 = new LinearLayout(context);
        L2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        L2.setGravity(Gravity.CENTER);
        L2.setOrientation(LinearLayout.VERTICAL);
    }

    public void addElement(){
        Button a1 = new Button(context);
        a1.setText("@string/app_name");
        a1.setVisibility(View.VISIBLE);
        L2.addView(a1, (new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)));
        System.out.println("Element Should be added!");
        Activity MainActivity = (Activity)context;
        MainActivity.setContentView(L2);
    }
}
