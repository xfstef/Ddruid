package com.frostbytetree.ddruid;

import android.content.Context;
import android.view.View;

/**
 * Created by XfStef on 11/27/2015.
 */

// This container is used to store all the inflators

public class WidgetInflators extends View{

    Context context;

    public WidgetInflators(Context context) {
        super(context);
        this.context = context;
    }
}
