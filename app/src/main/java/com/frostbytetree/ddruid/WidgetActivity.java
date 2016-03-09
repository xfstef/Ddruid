package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

//TODO Check for free memory
//TODO If no free memory then Talk to SQLite Controller

public class WidgetActivity extends AppCompatActivity implements IDataInflateListener, View.OnClickListener{

    private static final String CLASS_NAME = "Widget Activity";
    private static final String EMPTY_ERROR_MSG = "Field is required!";
    private static final int LIST_ACTIVITY_START = 2;

    FrameLayout widgetScreen = null;
    FrameLayout scannerScreen;
    FrameLayout loadingScreen;
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle mDrawerToggle;

    AppLogic appLogic;
    Widget my_widget;
    UIBuilder uiBuilder;

    RecycleViewWidgetAdapter widgetAdapter;
    // RecycleViewDataSetAdapter tableAdapter;
    LinearLayout.LayoutParams layoutParams;
    Scanner scanner;
    Data data;

    int requested_tables = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(CLASS_NAME, "Back pressed!!!!!");
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 3 - Code Scanner;
        Log.i(CLASS_NAME, "Widget Type: " + my_widget.widgetType);


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
            //TODO: this case should be called when all the tables are downloaded
            case 5: // send the first step for complex widget
                Log.i(CLASS_NAME, "My Widget tables size: " + my_widget.myTables.size());
                uiBuilder.loadInitialState(my_widget);
                for(int i = 0; i < my_widget.myTables.size(); i++) {
                    if (my_widget.myTables.get(i).dataSets.isEmpty()) {
                        loadTablesRegardingStepWidget();
                        break;
                    }
                    else {
                        Log.i(CLASS_NAME, "Only init step widget without reloading tables");
                        initStepWidget(my_widget.steps.get(0));
                        break;
                    }
                }
        }

    }

    private void loadTablesRegardingStepWidget()
    {
        requested_tables = my_widget.myTables.size();
        for(int i = 0; i < my_widget.myTables.size(); i++) {
            appLogic.getTableData(my_widget.myTables.get(i), this);
        }
    }

    private void initStepWidget(Step step)
    {
        // Problem when the child has already a parent defined
        if((ViewGroup)my_widget.getParent() != null)
        {
            ((ViewGroup)my_widget.getParent()).removeView(my_widget);
        }
        widgetScreen.addView(my_widget);
        appLogic.currentStep = step;
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        my_widget.removeAllViews();

        checkStepType(step);

    }

    private void checkStepType(Step step)
    {
        Log.i(CLASS_NAME, "Current Step: " + step.name);
        Log.i(CLASS_NAME, "Step Type: " + step.ui_element_type);

        switch(step.ui_element_type)
        {
            // TextView
            case 0:
                View element = uiBuilder.inputElementStep(step);
                my_widget.addView(element, layoutParams);
                // Which kind of input is used for this step
                switch(step.lookupTable.uses)
                {
                    case 0:
                        //standard input
                        break;
                    case 1:
                        initScan();
                        break;
                    case 2:
                        // GPS
                        break;
                    case 3:
                        // Spinner dropdown
                        break;
                    case 99:
                        // no input just do lookup
                        break;
                }
                break;
            // RecyclerView
            case 1:
                ArrayList<String> lookupResults = lookupResultsForRecyclerViewer(step);
                // Success
                if(!lookupResults.isEmpty()) {
                    displayRecyclerViewerResults(step, lookupResults);
                }

                break;
            // no UI (for action steps)
            case 99:

                break;
        }
    }

    private ArrayList<String> lookupResultsForRecyclerViewer(Step step)
    {

        ArrayList<String> parameters = new ArrayList<>();
        data.executeLookup(appLogic.currentStep.lookupTable, parameters);

        if(!appLogic.currentStep.lookupTable.results.isEmpty())
        {
            Log.i(CLASS_NAME, "Lookup Results: " + appLogic.currentStep.lookupTable.results.toString());
            ArrayList<String> ui_results = new ArrayList<>();
            ui_results = appLogic.widgetViews.prepareStepSuccessUI(appLogic.currentStep);
            return ui_results;
        }
        else
            return null;

    }


    private void displayRecyclerViewerResults(Step step, ArrayList<String> lookupData)
    {
        View element2 = uiBuilder.recyclerViewStep(step);
        my_widget.addView(element2, layoutParams);

        ResultsAdapter tableAdapter = new ResultsAdapter(lookupData);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
            Log.i(CLASS_NAME, "Current View Type " + current_view_type);
            Log.i(CLASS_NAME, "Current View " + current_view);
            String current_step_recycler_header_tag = appLogic.currentStep.name + ".label";
            String current_step_recycler_view_tag = appLogic.currentStep.name + ".recyclerview";

            if (current_view_type == uiBuilder.IS_RECYCLER_HEADER && current_view.getTag().toString().matches(current_step_recycler_header_tag)){
                TextView current_label = (TextView)current_view;
                current_label.setText(appLogic.currentStep.ui_label);
            }

            if (current_view_type == uiBuilder.IS_RECYCLER_VIEW && current_view.getTag().toString().matches(current_step_recycler_view_tag)) {

                Log.i(CLASS_NAME, "Label Text should have been updated/Tag = " + current_view.getTag());
                RecyclerView recList = (RecyclerView) current_view;
                recList.setAdapter(tableAdapter);
                recList.setLayoutManager(llm);
            }
        }
        setNextStep(step.next_if_success);
        checkStepType(appLogic.currentStep);
    }

    private void updateStepSuccessUI(ArrayList<String> result)
    {
        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++)
        {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
            Log.i(CLASS_NAME, "Current View Type " + current_view_type);
            Log.i(CLASS_NAME, "Current View " + current_view);
            String current_step_label_tag = appLogic.currentStep.name + ".label";
            String current_step_text_tag = appLogic.currentStep.name + ".text";
            String current_step_reset_button_tag = appLogic.currentStep.name + ".reset";
            String current_step_scan_button_tag = appLogic.currentStep.name + ".scan";

            if(current_view_type == uiBuilder.IS_INPUT_LABEL && current_view.getTag().toString().matches(current_step_label_tag))
            {
                Log.i(CLASS_NAME, "Label Text should have been updated/Tag = " + current_view.getTag());
                TextInputLayout current_label = (TextInputLayout) current_view;
                current_label.setVisibility(View.VISIBLE);
                current_label.setHint(appLogic.currentStep.success_label);
            }
            if(current_view_type == uiBuilder.IS_INPUT_TEXT && current_view.getTag().toString().matches(current_step_text_tag))
            {
                Log.i(CLASS_NAME, "Edit Text should have been updated/Tag = " + current_view.getTag());
                TextInputEditText current_text = (TextInputEditText) current_view;
                current_text.setText(result.get(0));
                //break;
            }
            if(current_view_type == uiBuilder.IS_ACTION_BUTTON && current_view.getTag().toString().matches(current_step_reset_button_tag))
            {
                Log.i(CLASS_NAME, "Edit Text should have been updated/Tag = " + current_view.getTag());
                final Button current_button = (Button)current_view;

                current_button.setOnClickListener(this);

                        /*new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(CLASS_NAME, "button reset clicked in step: " + appLogic.currentStep.name);
                        Log.i(CLASS_NAME, "button tag: " + current_button.getTag());
                        //TODO: remove all views with all the logic for the proceeding steps
                        //setPreviousStep(appLogic.currentStep);
                        //initStepWidget(appLogic.currentStep);
                    }
                });*/
                current_button.setVisibility(View.VISIBLE);
            }
            if(current_view_type == uiBuilder.IS_ACTION_BUTTON && current_view.getTag().toString().matches(current_step_scan_button_tag))
            {
                Log.i(CLASS_NAME, "Edit Text should have been updated/Tag = " + current_view.getTag());
                Button current_button = (Button)current_view;
                current_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(CLASS_NAME, "button scan clicked!");
                        //setPreviousStep(appLogic.currentStep);
                        //initStepWidget(appLogic.currentStep);
                    }
                });
                current_button.setVisibility(View.GONE);
                //break;
            }

        }
        setNextStep(appLogic.currentStep.next_if_success);
        checkStepType(appLogic.currentStep);
    }


    @Override
    public void onClick(View view) {



        if(view.getTag().toString().contains(".reset")) {
            // First: find the current step and set
            for(int i = 0; i < my_widget.steps.size(); i++)
            {
                if(view.getTag().toString().contains(my_widget.steps.get(i).name))
                {
                    appLogic.currentStep = my_widget.steps.get(i);
                    Log.i(CLASS_NAME, "Current step: " + appLogic.currentStep.name);
                }
            }

            // Second: Remove every ui_element which is after the ui selected
            for (int i = 0; i < uiBuilder.all_view_elements.size(); i++)
            {
                if(uiBuilder.all_view_elements.get(i).first == uiBuilder.IS_ACTION_BUTTON &&
                        view == uiBuilder.all_view_elements.get(i).second)
                {
                    // remove every view including myself
                    for(int j = i; j < uiBuilder.all_view_elements.size(); j++)
                    {
                        Log.i(CLASS_NAME, "Item to be removed: " + uiBuilder.all_view_elements.get(j).second.getTag());
                        View currentViewToRemove = uiBuilder.all_view_elements.get(j).second;
                        //uiBuilder.all_view_elements.remove(j);
                        ((ViewGroup) currentViewToRemove.getParent()).removeView(currentViewToRemove);

                    }
                }

            }
            // Third: remove every ui_element from the selected step
            //for(int i = 0; i < uiBuilder.all_view_elements)
            //checkStepType(appLogic.currentStep);

        }

    }


    // Helper function: set the parent step as current step and the child steps parent to null
    private void setPreviousStep(Step current)
    {
        appLogic.currentStep = current.parent_step;
        current.parent_step = null;
    }

    // Helper function: The child step gets his parent and the child is declaired as current now
    private void setNextStep(Step child_step)
    {
        child_step.parent_step = appLogic.currentStep;
        appLogic.currentStep = child_step;
    }


    private void initScan()
    {

        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
            Log.i(CLASS_NAME, "Current View Type " + current_view_type);
            Log.i(CLASS_NAME, "Current View " + current_view);
            String scan_tag = appLogic.currentStep.name + ".scan";

            if (current_view_type == uiBuilder.IS_ACTION_BUTTON && current_view.getTag().toString().matches(scan_tag)){
                Button bScan = (Button)current_view;
                bScan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        widgetScreen.setVisibility(View.GONE);
                        scannerScreen.setVisibility(View.VISIBLE);

                        // Create a new Fragment to be placed in the activity layout
                        scanner = new Scanner();

                        // In case this activity was started with special instructions from an
                        // Intent, pass the Intent's extras to the fragment as arguments
                        scanner.setArguments(getIntent().getExtras());

                        // Add the fragment to the 'fragment_container' FrameLayout
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.scanner, scanner, "TVOJA MAMA")
                                .addToBackStack(null).commit();
                    }
                });
                break;
            }

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

        setContentView(R.layout.widget_activity);
        widgetScreen = (FrameLayout) findViewById(R.id.mainContent);
        scannerScreen = (FrameLayout) findViewById(R.id.scanner);
        //widgetScreen.setVisibility(View.GONE);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        loadingScreen = (FrameLayout)findViewById(R.id.loading_circle);
        //loadingScreen.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(my_widget.titleBar);




        // if that is not the root widget then, the back arrow should be displayed
        // else the main should show the hamburger menu
        if(my_widget.myParent != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else {
            initNavigationDrawer();
            drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // drawer object Assigned to the view
            mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

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


            }; // drawer Toggle Object Made
            drawer.setDrawerListener(mDrawerToggle); // drawer Listener set to the drawer toggle
            mDrawerToggle.syncState();
        }
        widgetScreen.removeAllViews();
    }


    private void initNavigationDrawer()
    {
        TextView username = (TextView)findViewById(R.id.txtUserName);
        Button about = (Button)findViewById(R.id.bAbout);
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

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(CLASS_NAME, "On Resume called!");
        // close the camera
        //setStatusBarTheme();
        // This section is needed  for the
        // init the UI Builder
        uiBuilder = UIBuilder.getInstance();
        data = Data.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);


        my_widget = appLogic.currentWidget;

        // set the current Interface for ui_endpoint (signalDataArived)
        appLogic.iDataInflateListener = this;

        /*
        if (my_widget.myTables.get(0).dataSets.size() == 0)
        {
            Log.i(CLASS_NAME, "Getting Table: " + my_widget.myTables.get(0).table_name);
            appLogic.getTableData(my_widget.myTables.get(0), this);
        }*/

        initScreenItems();
        checkWidgetType();
    }

    @Override
    public void onPause() {
        // if camera fragment was activated
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TVOJA MAMA");
        if (fragment != null) {
            Log.i(CLASS_NAME, "Fragment will be removed!");
            scanner.releaseCamera();
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (this.drawer.isDrawerOpen(GravityCompat.START))
        {
            this.drawer.closeDrawer(GravityCompat.START);
            return;
        }
        /*
        if(scanner != null && scanner.isResumed()) {
            Log.i(CLASS_NAME, "Scanner should be destroyed!");
            scanner.onDestroy();
        }*/
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TVOJA MAMA");
        if (fragment != null) {
            Log.i(CLASS_NAME, "Fragment will be removed!");
            scanner.releaseCamera();
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

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
        super.onBackPressed();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(CLASS_NAME, "OnActivityResult called resultCode " + requestCode);
        Log.d(CLASS_NAME, "Widget " + my_widget.titleBar);
        appLogic.temporary_dataSet = null;
        if(requestCode == LIST_ACTIVITY_START)
        {
            Log.d(CLASS_NAME, "Init Screen Items invoked");
            //initScreenItems();

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
    public void codeScanned(String code) {
        scanner.releaseCamera();
        scannerScreen.setVisibility(View.GONE);
        widgetScreen.setVisibility(View.VISIBLE);
        // close the camera
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TVOJA MAMA");
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        Log.i(CLASS_NAME, "Code scanned: " + code);

        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(code);
        data.executeLookup(appLogic.currentStep.lookupTable, parameters);
        //Log.i(CLASS_NAME, "DataSet found for scanned item: " + appLogic.currentStep.lookupTable.results.set.toString());

        if(!appLogic.currentStep.lookupTable.results.isEmpty()) {
            ArrayList<String> ui_results = new ArrayList<>();
            ui_results = appLogic.widgetViews.prepareStepSuccessUI(appLogic.currentStep);
            updateStepSuccessUI(ui_results);
        }
        else{
            // TODO: Handle Error state.
        }

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
            case 5:
                requested_tables--;
                if(requested_tables == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initStepWidget(my_widget.steps.get(0));
                        }
                    });
                }
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