package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// This container is used to store all the inflators

public class WidgetInflators {
    private static WidgetInflators ourInstance = new WidgetInflators();

    public static WidgetInflators getInstance() {
        return ourInstance;
    }

    private WidgetInflators() {
    }
}
