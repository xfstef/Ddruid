package com.frostbytetree.ddruid;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

//TODO Check for free memory
//TODO If no free memory then Talk to SQLite Controller

public class WidgetActivity extends AppCompatActivity implements IDataInflateListener{

    private static final String CLASS_NAME = "Widget Activity";
    private static final String EMPTY_ERROR_MSG = "Field is required!";
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

        setTheme(appLogic.configFile.custom_color);


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

        Button action = uiBuilder.action_button;

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSet setPost = new DataSet();
                setPost.set = new ArrayList<String>();
                // first check if all fields are filled correctly
                for (int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
                    switch (uiBuilder.all_view_elements.get(i).first) {
                        case 0:
                            String tag = (String) uiBuilder.all_view_elements.get(i).second.getTag();
                            EditText current_view = (EditText) uiBuilder.all_view_elements.get(i).second;
                            if (tag != null && tag.matches("required") && current_view.getText().toString().isEmpty()) {

                                current_view.setError(EMPTY_ERROR_MSG);
                                return;
                                //Log.i(CLASS_NAME, "Field " + i + " is required!");
                            }
                            setPost.set.add(current_view.getText().toString());
                            break;
                        case 2:
                            Spinner current_spinner = (Spinner) uiBuilder.all_view_elements.get(i).second;

                            String tag1 = (String) uiBuilder.all_view_elements.get(i).second.getTag();
                            if (tag1 != null && tag1.matches("required")) {

                                return;
                                //Log.i(CLASS_NAME, "Field " + i + " is required!");
                            }
                            setPost.set.add(current_spinner.getItemAtPosition(current_spinner.getSelectedItemPosition() - 1).toString());
                            break;
                        case 5:
                            String tag2 = (String) uiBuilder.all_view_elements.get(i).second.getTag();
                            EditText current_date = (EditText) uiBuilder.all_view_elements.get(i).second;
                            if (tag2 != null && tag2.matches("required") && current_date.getText().toString().isEmpty()) {

                                current_date.setError(EMPTY_ERROR_MSG);
                                return;
                                //Log.i(CLASS_NAME, "Field " + i + " is required!");
                            }
                            setPost.set.add(current_date.getText().toString());
                            break;
                    }
                }
                for (int i = 0; i < setPost.set.size(); i++)
                    Log.i(CLASS_NAME, "SET POST : " + setPost.set.get(i));


                for (int i = 0; i < my_widget.myTables.get(0).myActions.size(); i++) {

                    appLogic.sendPost(setPost, uiBuilder.current_action, my_widget.myTables.get(0));
                }
                Toast.makeText(getApplicationContext(), "POST: " + uiBuilder.current_action.name, Toast.LENGTH_LONG).show();
            }
        });
    }

    // This function loads the table datasets for each Spinner on the screen
    private void checkForSpinnerDataLoading()
    {
        ArrayList<Pair<View, Attribute>> spinners = uiBuilder.spinner_data_to_load;
        if(spinners.size() != 0)
        {
            for(int i = 0; i < spinners.size(); i++)
            {
                if(spinners.get(i).second != null)
                {
                    System.out.println("spinners has to load data from table: " + spinners.get(i).second.items.referenced_table);
                    appLogic.getTableData(spinners.get(i).second.items.referenced_table, this);
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
        intent.setClass(getApplicationContext(), ListActivity.class);
        startActivityForResult(intent, LIST_ACTIVITY_START);
    }

    void initScreenItems() {

        widgetScreen = (FrameLayout) findViewById(R.id.mainContent);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        my_widget = appLogic.currentWidget;

        // set the current Interface for ui_endpoint (signalDataArived)
        appLogic.iDataInflateListener = this;

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(my_widget.titleBar);




        // if that is not the root widget then, the back arrow should be displayed
        // else the main should show the hamburger menu
        if(my_widget.myParent != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else {
            initNavigationDrawer();
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


    private void initNavigationDrawer()
    {
        TextView username = (TextView)findViewById(R.id.txtUserName);
        Button logout = (Button)findViewById(R.id.bLogout);

        if(appLogic.configFile.username == null)
            username.setText("Hello " + getIntent().getStringExtra("username"));
        else
            username.setText("Hello " + appLogic.configFile.username);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Functional but second instance built
                Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_NO_CREATE);
                AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);

            }
        });

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
        appLogic.temporary_dataSet = null;
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
                            if(my_table == uiBuilder.spinner_data_to_load.get(i).second.items.referenced_table)
                            {
                                int offset = getReferenceOffsetForSpinner(uiBuilder.spinner_data_to_load.get(i).second.items);
                                ArrayList<String> values = getReferencedAttribute(my_table, offset);
                                uiBuilder.initSpinnerAdapter((Spinner) uiBuilder.spinner_data_to_load.get(i).first, values);
                                uiBuilder.spinner_data_to_load.remove(uiBuilder.spinner_data_to_load.get(i));
                            }
                    }
                });
                break;
        }

    }

    private int getReferenceOffsetForSpinner(com.frostbytetree.ddruid.Spinner spinner)
    {
        int offset = 0;
        for(int i = 0; i < spinner.referenced_table.attributes.size(); i++)
        {
            Log.i(CLASS_NAME, "Table " + spinner.referenced_table.attributes.get(i).name);
            if(spinner.referenced_table.attributes.get(i).name.matches(spinner.dataSetName.get(0)))
            {
                offset = i;
            }

        }
        return offset;
    }

    private ArrayList<String> getReferencedAttribute(Table table, int offset)
    {
        ArrayList<String> attributes = new ArrayList<>();

        for(int i = 0; i < table.dataSets.size(); i++)
            attributes.add(table.dataSets.get(i).set.get(0));

        return attributes;
    }

}