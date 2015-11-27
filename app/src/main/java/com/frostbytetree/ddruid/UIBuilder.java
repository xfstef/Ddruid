package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

// The UI Builder is a background Thread that uses the Data Models in order to create the inflators
// needed for the various app Widgets.

    //TODO Load the Data Models
    //TODO Interpret the Data Models
    //TODO Build the Widget Inflators ???
    //TODO Signal and start the TemporaryWidget Activity

public class UIBuilder {
    private static UIBuilder ourInstance = new UIBuilder();

    public static UIBuilder getInstance() {
        return ourInstance;
    }

    private UIBuilder() {
    }
}
