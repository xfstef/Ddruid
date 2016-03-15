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
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.*;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
    private static final int SCAN_ACTIVITY_START = 3;

    FrameLayout widgetScreen = null;
    //FrameLayout scannerScreen;
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
        initInstances();
        setTheme(appLogic.configFile.custom_color);
        initScreenItems();
        checkWidgetType();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initInstances()
    {
        //my_widget = new Widget(this);
        //widgetViews = WidgetViews.getInstance();
        appLogic = AppLogic.getInstance();
        my_widget = appLogic.currentWidget;

        uiBuilder = UIBuilder.getInstance();
        data = Data.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        uiBuilder.loadInitialState(my_widget);
    }

    private void initScreenItems() {

        setContentView(R.layout.widget_activity);
        widgetScreen = (FrameLayout) findViewById(R.id.mainContent);
        loadingScreen = (FrameLayout)findViewById(R.id.loading_circle);
        //scannerScreen = (FrameLayout) findViewById(R.id.scanner);
        //widgetScreen.setVisibility(View.GONE);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
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

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 3 - Code Scanner;
        Log.i(CLASS_NAME, "Widget Type: " + my_widget.widgetType);


        switch (my_widget.widgetType) {
            case 0:
                initWidgetList();
                widgetScreen.setVisibility(View.VISIBLE);
                loadingScreen.setVisibility(View.GONE);
                break;
            case 1:
                initFormWidget();
                widgetScreen.setVisibility(View.VISIBLE);
                loadingScreen.setVisibility(View.GONE);
                break;
            case 4:
                startWidgetListActivity();
                widgetScreen.setVisibility(View.VISIBLE);
                loadingScreen.setVisibility(View.GONE);
                break;
            case 5: // send the first step for complex widget
                appLogic.iDataInflateListener = this;

                //appLogic.setCurrentWidget(selected_widget);
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
            // Action button
            case 2:
                if(step.action_table_name != null)
                {
                    Step step1 = my_widget.getStep(step.action_attributes.get(0));
                    Pair<String, String> table_attr = data.splitTableFromAttribute(step.action_attributes.get(1));
                    Table table = data.getTable(table_attr.first);
                    // we assume that we will only have one result
                    step.action = data.getStepAction(step1.lookupTable.results.get(0), table, table_attr.second);
                    View actionElement = uiBuilder.actionElementStep(step);
                    my_widget.addView(actionElement, layoutParams);
                    for(int i = 0; i < uiBuilder.all_view_elements.size(); i++)
                        if(uiBuilder.all_view_elements.get(i).second.getTag().toString().contains(".action"))
                        {
                            Button action = (Button)uiBuilder.all_view_elements.get(i).second;
                            action.setOnClickListener(this);
                            /*
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appLogic.sendPost(step1.lookupTable.results.get(0),step.action, table);
                                }
                            });
                            */
                        }

                }
                break;
            // no UI (for action steps)
            case 99:
                if(step.lookupTable != null) {
                    ArrayList<String> lookupResults1 = lookupResultsForRecyclerViewer(step);
                }
                if(step.action_table_name == null)
                {
                    setNextStep(step.next_if_success);
                }
                else
                {
                    //Action execute
                    //data.getStepAction(lookupResults1, data.getTable(step.action_table_name));
                    //System.out.println("^^^$^$^$^$^^$ Trying to " + step.action.name);
                    setNextStep(step.next_if_success);
                }

                break;
        }
        widgetScreen.setVisibility(View.VISIBLE);
        loadingScreen.setVisibility(View.GONE);
    }

    private ArrayList<String> lookupResultsForRecyclerViewer(Step step)
    {

        ArrayList<String> parameters = new ArrayList<>();
        data.executeLookup(appLogic.currentStep.lookupTable, parameters);

        if(!appLogic.currentStep.lookupTable.results.isEmpty())
        {
            //Log.i(CLASS_NAME, "Lookup Results: " + appLogic.currentStep.lookupTable.results.toString());
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
        llm.setSmoothScrollbarEnabled(true);
        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
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
    }

    private void updateStepSuccessUI(ArrayList<String> result)
    {
        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
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
            if(current_view_type == uiBuilder.IS_SCAN_BUTTON && current_view.getTag().toString().matches(current_step_reset_button_tag))
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
            if(current_view_type == uiBuilder.IS_SCAN_BUTTON && current_view.getTag().toString().matches(current_step_scan_button_tag)) {
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
    }


    @Override
    public void onClick(View view) {

        if(view.getTag().toString().contains(".reset")) {
            // First: find the current step and set it
            for(int i = 0; i < my_widget.steps.size(); i++)
            {
                if(view.getTag().toString().contains(my_widget.steps.get(i).name))
                {
                    appLogic.currentStep = my_widget.steps.get(i);
                    Log.i(CLASS_NAME, "Current step: " + appLogic.currentStep.name);
                    break;
                }
            }

            Log.i(CLASS_NAME, "all_view_elements.size: " + uiBuilder.all_view_elements.size());
            uiBuilder.checkExistingUIAndremoveNextUIElements(appLogic.currentStep.name);
            checkStepType(appLogic.currentStep);
            Log.i(CLASS_NAME, "At last the all_view_elements size is: " + uiBuilder.all_view_elements.size());
        }
        if(view.getTag().toString().contains(".action")){
            Log.i(CLASS_NAME, "Current step: " + appLogic.currentStep.name);

            Step step1 = my_widget.getStep(appLogic.currentStep.action_attributes.get(0));
            Pair<String, String> table_attr = data.splitTableFromAttribute(appLogic.currentStep.action_attributes.get(1));
            Table table = data.getTable(table_attr.first);
            // we assume that we will only have one result
            checkHiddenRequiredAttributes(step1.lookupTable.results.get(0), appLogic.currentStep.action);
            appLogic.currentStep.action.type = 5;
            appLogic.sendPost(step1.lookupTable.results.get(0), appLogic.currentStep.action, table);
            setNextStep(appLogic.currentStep.next_if_success);
        }

    }

    // When no UI should be shown but you need to post also something like timestamp
    private void checkHiddenRequiredAttributes(DataSet dataSet, com.frostbytetree.ddruid.Action action)
    {
        for(int i = 0; i < action.attributes.size(); i++)
        {
            if(action.attribute_required.get(i))
            {
                Log.i(CLASS_NAME, "DataSet.set[" + dataSet.set.get(i) + "]");
                if(dataSet.set.get(i).matches("null"))
                {
                    switch(action.attributes.get(i).attribute_type)
                    {
                        // date
                        case 5:
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            String date = df.format(Calendar.getInstance().getTime());
                            Log.i(CLASS_NAME, "The current date is : " + date);
                            dataSet.set.set(i, date);
                            break;
                        // timestamp
                        case 6:

                            break;
                    }
                }
            }
        }
    }


    // Helper function: set the parent step as current step and the child steps parent to null
    private void setPreviousStep(Step current)
    {
        appLogic.currentStep = current.parent_step;
        current.parent_step = null;
    }

    // Helper function: The child step gets his parent and the child is declaired as current now
    private void setNextStep(Step child_step) {
        uiBuilder.checkExistingUIAndremoveNextUIElements(child_step.name);
        child_step.parent_step = appLogic.currentStep;
        appLogic.currentStep = child_step;
        checkStepType(appLogic.currentStep);
    }


    private void initScan()
    {
        for(int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
            Short current_view_type = uiBuilder.all_view_elements.get(i).first;
            View current_view = uiBuilder.all_view_elements.get(i).second;
            String scan_tag = appLogic.currentStep.name + ".scan";

            if (current_view_type == uiBuilder.IS_SCAN_BUTTON && current_view.getTag().toString().matches(scan_tag)){
                final Button bScan = (Button)current_view;
                bScan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getApplicationContext(), Scanner.class);
                        intent.putExtra("scan_label", bScan.getText());
                        startActivityForResult(intent, SCAN_ACTIVITY_START);
                        /* OLD FRAGMENT SHIT
                        widgetScreen.setVisibility(View.GONE);
                        scannerScreen.setVisibility(View.VISIBLE);

                        // Create a new Fragment to be placed in the activity layout
                        scanner = new Scanner();

                        // In case this activity was started with special instructions from an
                        // Intent, pass the Intent's extras to the fragment as arguments
                        //scanner.setArguments(getIntent().getExtras());

                        // Add the fragment to the 'fragment_container' FrameLayout
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.scanner, scanner, "TVOJA MAMA")
                                .addToBackStack(null).commit();
                                */
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



    private void initNavigationDrawer()
    {
        TextView username = (TextView) findViewById(R.id.txtUserName);
        Button about = (Button) findViewById(R.id.bAbout);
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


        /*
        uiBuilder = UIBuilder.getInstance();
        data = Data.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        my_widget = appLogic.currentWidget;
        appLogic.iDataInflateListener = this;
        */

    }

    @Override
    public void onPause() {
        // if camera fragment was activated
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TVOJA MAMA");
        if (fragment != null) {
            Log.i(CLASS_NAME, "Fragment will be removed!");
            scanner.releaseCamera();
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

            //scannerScreen.setVisibility(View.GONE);
            widgetScreen.setVisibility(View.VISIBLE);
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
        Log.i(CLASS_NAME, "OnActivityResult called resultCode " + requestCode);
        Log.i(CLASS_NAME, "Widget " + my_widget.titleBar);
        appLogic.temporary_dataSet = null;
        if (requestCode == LIST_ACTIVITY_START) {
            Log.i(CLASS_NAME, "Init Screen Items invoked");
            initInstances();
            initScreenItems();
            checkWidgetType();

        }
        if(requestCode == SCAN_ACTIVITY_START && data != null)
        {
            codeScanned(data.getStringExtra("scanned_code"));
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

    public void codeScanned(String code) {
        /*
        scanner.releaseCamera();
        scannerScreen.setVisibility(View.GONE);
        widgetScreen.setVisibility(View.VISIBLE);
        // close the camera
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TVOJA MAMA");
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        */
        Log.i(CLASS_NAME, "Code scanned: " + code);

        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(code);
        data.executeLookup(appLogic.currentStep.lookupTable, parameters);
        //Log.i(CLASS_NAME, "DataSet found for scanned item: " + appLogic.currentStep.lookupTable.results.set.toString());

        if (!appLogic.currentStep.lookupTable.results.isEmpty()) {
            ArrayList<String> ui_results = new ArrayList<>();
            ui_results = appLogic.widgetViews.prepareStepSuccessUI(appLogic.currentStep);
            if(appLogic.currentStep.ui_element_type != 99) {
                Log.i(CLASS_NAME, "UI ELEMENT Not 99!");
                updateStepSuccessUI(ui_results);
            } else {
                Log.i(CLASS_NAME, "UI ELEMENT IS 99!");
                setNextStep(appLogic.currentStep.next_if_success);
            }
        }
        else{
            // TODO: Handle Error state.

            for(int i = 0; i < uiBuilder.all_view_elements.size(); i++)
            {
                //Log.i(CLASS_NAME, )
                if(uiBuilder.all_view_elements.get(i).first == uiBuilder.IS_ERROR_TEXT &&
                        uiBuilder.all_view_elements.get(i).second.toString().contains(appLogic.currentStep.name))
                {
                    TextView error_text = (TextView)uiBuilder.all_view_elements.get(i).second;
                    error_text.setVisibility(View.VISIBLE);
                    error_text.setText(appLogic.currentStep.error_message);
                }
            }

            /*
            for(int i = 0; i < 3; i++)
                Toast.makeText(getApplicationContext(),"Sry, no match for: " + code, Toast.LENGTH_SHORT).show();
            */

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

    @Override
    public void signalOffline(String could_not_send_now)
    {

    }

    @Override
    public void signalOnline(String operation_finished)
    {

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