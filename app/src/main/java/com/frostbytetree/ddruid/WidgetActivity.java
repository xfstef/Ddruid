package com.frostbytetree.ddruid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.google.android.gms.appindexing.*;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Activity will be created when a widget or sub-widget is opened.

//TODO Check for free memory
//TODO If no free memory then Talk to SQLite Controller

public class WidgetActivity extends AppCompatActivity implements IDataInflateListener{

    private static final String CLASS_NAME = "Widget Activity";
    private static final int LIST_ACTIVITY_START = 2;

    FrameLayout widgetScreen;
    Toolbar toolbar;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;

    AppLogic appLogic;
    Widget my_widget;
    UIBuilder uiBuilder;

    RecycleViewWidgetAdapter widgetAdapter;
    // RecycleViewDataSetAdapter tableAdapter;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch(id)
        {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //my_widget = new Widget(this);
        //widgetViews = WidgetViews.getInstance();

        appLogic = AppLogic.getInstance();
        my_widget = appLogic.currentWidget;

        // init the UI Builder
        uiBuilder = UIBuilder.getInstance();

        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        setContentView(R.layout.widget_activity);
        initScreenItems();
        //System.out.println("Current widget on Resume: " + my_widget.titleBar);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 3 - Code Scanner;
        Log.d(CLASS_NAME, "Widget Type: " + my_widget.widgetType);
        switch (my_widget.widgetType) {
            case 0:
                initWidgetList();
                break;
            case 1:
                initFormWidget();
                break;
            case 4:
                startWidgetListActivity();
                //initTableList();
                break;
            default:

        }
    }

    void initFormWidget()
    {

        Widget new_ui_widget = uiBuilder.inflateModel(my_widget);

        widgetScreen.addView(new_ui_widget);

        checkForSpinnerDataLoading();
    }

    // This function loads the table datasets for each Spinner on the screen
    private void checkForSpinnerDataLoading()
    {
        ArrayList<Pair<View, Table>> spinners = uiBuilder.spinner_data_to_load;
        if(spinners.size() != 0)
        {
            for(int i = 0; i < spinners.size(); i++)
            {
                if(spinners.get(i).second != null)
                {
                    System.out.println("spinners has to load data from table: " + spinners.get(i).second.table_name);
                    appLogic.getTableData(spinners.get(i).second, this);
                }
            }

        }
    }

    void initWidgetList() {

        RecyclerView recList = uiBuilder.buildWidgetRecyclerViewer(widgetScreen);

        widgetAdapter = new RecycleViewWidgetAdapter(this, my_widget.myChildren);
        recList.setAdapter(widgetAdapter);

        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Widget selected_widget = my_widget.myChildren.get(position);
                // set parent widget
                selected_widget.myParent = my_widget;


                // set current widget
                appLogic.setCurrentWidget(selected_widget);

                // Intent iResult = new Intent();
                // setResult(Activity.RESULT_OK, iResult);
                finish();
            }
            //Toast.makeText(getApplicationContext(), "Selected Widget element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();

        }));
    }


    void startWidgetListActivity()
    {
        Intent intent = getIntent();
        intent.setClass(getApplicationContext(), WidgetListItemListActivity.class);
        startActivityForResult(intent, LIST_ACTIVITY_START);
    }

    void initScreenItems() {

        widgetScreen = (FrameLayout) findViewById(R.id.mainContent);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        my_widget = appLogic.currentWidget;
        setSupportActionBar(toolbar);
        try
        {
            getSupportActionBar().setTitle(my_widget.titleBar);
        }
        catch(Exception exception)
        {
            Log.e(CLASS_NAME, "Could not find action bar", exception);
        }

        // if that is not the root widget then, the back arrow should be displayed
        // else the main should show the hamburger menu
        if(my_widget.myParent != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else {
            Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
            mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                    // open I am not going to put anything here)
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    // Code here will execute once drawer is closed
                }


            }; // Drawer Toggle Object Made
            Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
            mDrawerToggle.syncState();
        }
        widgetScreen.removeAllViews();
        checkWidgetType();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // when adding dynamically views then they should be destroyed when going back
        my_widget.removeAllViews();
        widgetScreen.removeAllViews();
        Intent iResult = new Intent();
        setResult(Activity.RESULT_CANCELED, iResult);

        // check if this widget has parent widgets, if so then set the coresponding parent widget
        if(my_widget.myParent != null) {
            appLogic.setCurrentWidget(my_widget.myParent);
            finish();
        }
        else
        {
            // this sends the android app to the background
            this.moveTaskToBack(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(CLASS_NAME, "OnActivityResult called resultCode " + requestCode);
        Log.d(CLASS_NAME, "Widget " + my_widget.titleBar);

        if(requestCode == LIST_ACTIVITY_START)
        {
            Log.d(CLASS_NAME, "Init Screen Items invoked");
            initScreenItems();

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        com.google.android.gms.appindexing.Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Widget Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.frostbytetree.ddruid/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Widget Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.frostbytetree.ddruid/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void signalDataArrived(final Table my_table) {
        System.out.println("DATA HAS ARRIVED!");

        // 0 - Widget-List;
        // 1 - Form;
        // 2 - Detail - could be never used;
        // 3 - Code Scanner;
        // 4 - List with datasets
        // 31 - Code Scanner + GPS;

        switch(my_widget.widgetType)
        {
            case 1:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("my_table:        " + my_table.toString());
                        System.out.println("my_table_spinner:" + uiBuilder.spinner_data_to_load.get(0).second);
                        // If table data has recieved and spinners should be filled first check the size
                        // then see if the requested table matches with the incomming
                        for(int i = 0; i < uiBuilder.spinner_data_to_load.size(); i++)
                            if(my_table == uiBuilder.spinner_data_to_load.get(i).second)
                            {
                                uiBuilder.initSpinnerAdapter((Spinner) uiBuilder.spinner_data_to_load.get(i).first, my_table.dataSets);
                                uiBuilder.spinner_data_to_load.remove(uiBuilder.spinner_data_to_load.get(i));
                            }
                    }
                });
                break;

        }


    }

    @Override
    public void invokeLoadingTableData(Table table) {
        //Log.d("Widget Activity", "Table invocation requested for: " + table.table_name);
    }
}