package com.frostbytetree.ddruid;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class WidgetActionActivity extends AppCompatActivity {

    Toolbar toolbar;

    FrameLayout widgetScreen;
    AppLogic appLogic;
    Widget my_widget;
    UIBuilder uiBuilder;

    private final static String CLASS_NAME = "Widget Action Activity";
    private static final String EMPTY_ERROR_MSG = "Field is required!";


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch(id)
        {
            case android.R.id.home:
                appLogic.setCurrentWidget(my_widget.myParent);
                onBackPressed();
                break;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_action_activitiy);

        //my_widget = new Widget(this);
        //widgetViews = WidgetViews.getInstance();

        appLogic = AppLogic.getInstance();
        my_widget = appLogic.currentWidget;

        setTheme(appLogic.configFile.custom_color);


        // init the UI Builder
        uiBuilder = UIBuilder.getInstance();

        uiBuilder.setContext(this);
        //uiBuilder.setCallback(this);
        setContentView(R.layout.activity_widget_action_activitiy);
        initScreenItems();

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        widgetScreen.removeAllViews();
        checkWidgetType();
    }

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 3 - Code Scanner;
        Log.d(CLASS_NAME, "Widget Type: " + my_widget.widgetType);
        switch (my_widget.widgetType) {
            case 0:
                //initWidgetList();
                break;
            case 1:
                initFormWidget();
                break;
            case 4:
                //startWidgetListActivity();
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
                            android.widget.Spinner current_spinner = (android.widget.Spinner) uiBuilder.all_view_elements.get(i).second;

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
        ArrayList<Pair<View, Spinner>> spinners = uiBuilder.spinner_data_to_load;
        if(spinners.size() != 0)
        {
            for(int i = 0; i < spinners.size(); i++)
            {
                if(spinners.get(i).second != null)
                {
                    System.out.println("spinners has to load data from table: " + spinners.get(i).second.referenced_table);
                    appLogic.getTableData(spinners.get(i).second.referenced_table, this);
                }
            }

        }
    }
}
