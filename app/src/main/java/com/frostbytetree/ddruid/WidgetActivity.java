package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.*;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Activity will be created when a widget or sub-widget is opened.

//TODO Load the Widget Inflators ???
//TODO Load the Raw Data
//TODO Check for free memory
//TODO If no free memory then Talk to SQLite Controller

public class WidgetActivity extends AppCompatActivity {

    LinearLayout widgetScreen;
    Toolbar toolbar;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;

    AppLogic appLogic;
    //WidgetViews widgetViews;
    Widget my_widget;


    UIBuilder the_ui;
    RecycleViewWidgetAdapter widgetAdapter;
    RecycleViewDataSetAdapter tableAdapter;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //my_widget = new Widget(this);
        //widgetViews = WidgetViews.getInstance();
        appLogic = AppLogic.getInstance();
        my_widget = getCurrentWidget();

        setContentView(R.layout.widget_activity);
        if (Build.VERSION.SDK_INT > 21) {
            setupWindowAnimations();
        }
        initScreenItems();


        if (my_widget != null) {
            checkWidgetType();
        }

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 3 - Code Scanner;
        switch (my_widget.widgetType) {
            case 0:
                initWidgetList();
                break;
            case 1:
                initFormWidget();
                break;
            case 4:
                initTableList();
                break;
            default:

        }
    }

    void initFormWidget()
    {
        // init the UI Builder
        the_ui = UIBuilder.getInstance();

        the_ui.setContext(this);

        System.out.println("Widget Screen: " + my_widget.titleBar);
        System.out.println("The UI Object within form: " + the_ui);
        Widget new_ui_widget = the_ui.inflate_model(my_widget);

        widgetScreen.addView(new_ui_widget);
        // widgetScreen.addView(my_widget);
        // widgetScreen = the_ui.inflate_model(my_widget);
        // System.out.println("Enable Form widget!");
    }

    void initWidgetList() {
        RecyclerView recList = new RecyclerView(this);
        widgetScreen.addView(recList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        //String[] list = {"First element", "Second element"};
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

    void initTableList() {
        RecyclerView recList = new RecyclerView(this);

        final Table my_table = findTableWithinWidget(my_widget);

        System.out.println("MY TABLE: " + my_table.table_name);
        appLogic.getTableData(my_table, this);


        if(my_table == null)
        {
            System.out.println("No Table found within Widget!");
            return;
        }

        widgetScreen.addView(recList);

        System.out.println("dataSet: " + my_table.dataSets);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        tableAdapter = new RecycleViewDataSetAdapter(this, my_table.dataSets);
        //tableAdapter.notifyDataSetChanged();
        recList.setAdapter(tableAdapter);

        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DataSet selectedDataSet = my_table.dataSets.get(position);

                //showDetailsFragment selectedDataSet
                //Toast.makeText(getApplicationContext(), "Selected DataSet element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();
            }
        }));

    }

    private void showDetailsFragment() {

    }


    Table findTableWithinWidget(Widget widget)
    {
        if(widget.myTables.size() != 0)
            return widget.myTables.get(0);
        else
            return null;
    }

    public void addRecycleViewItemList()
    {
        if(widgetAdapter != null)
            widgetAdapter.notifyItemInserted(my_widget.myChildren.size()-1);

        if(widgetAdapter != null)
            widgetAdapter.notifyItemInserted(my_widget.myChildren.size()-1);
    }


    Widget getCurrentWidget() {
        if(appLogic.currentWidget != null)
            return appLogic.currentWidget;
        else
            return null;
    }

    void initScreenItems() {
        widgetScreen = (LinearLayout) findViewById(R.id.mainContent);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(my_widget.titleBar);

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

    @Override
    public void onResume() {
        super.onResume();
        appLogic.setCurrentWidget(my_widget);
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
        }
        else
        {
            // this sends the android app to the background
            this.moveTaskToBack(true);
        }

    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
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

    public void signalDataArrived() {
        System.out.println("DATA HAS ARRIVED!");
        widgetAdapter.notifyDataSetChanged();
    }
}


