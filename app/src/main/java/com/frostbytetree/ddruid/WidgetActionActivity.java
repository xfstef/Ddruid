package com.frostbytetree.ddruid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

public class WidgetActionActivity extends AppCompatActivity implements IDataInflateListener{

    Toolbar toolbar;

    FrameLayout widgetScreen;
    AppLogic appLogic;
    Widget my_widget;
    UIBuilder uiBuilder;
    // Used for spinner to add the id's
    ArrayList<String> selected_source_columns;

    private final static String CLASS_NAME = "Widget Action Activity";
    private static final String EMPTY_ERROR_MSG = "Field is required!";


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
    public void onBackPressed()
    {
        super.onBackPressed();
        appLogic.setCurrentWidget(my_widget.myParent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_action_activity);
        Log.i(CLASS_NAME, "onCreate!");
        //my_widget = new Widget(this);
        //widgetViews = WidgetViews.getInstance();

        appLogic = AppLogic.getInstance();
        my_widget = appLogic.currentWidget;
        my_widget.removeAllViews();
        Log.i(CLASS_NAME, "My current widget: " + my_widget.titleBar);
        setTheme(appLogic.configFile.custom_color);


        // init the UI Builder
        uiBuilder = UIBuilder.getInstance();

        uiBuilder.setContext(this);
        //uiBuilder.setCallback(this);
        setContentView(R.layout.activity_widget_action_activity);
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

        if((ViewGroup)my_widget.getParent() != null)
        {
            ((ViewGroup)my_widget.getParent()).removeView(my_widget);
        }


        Widget new_ui_widget = uiBuilder.inflateModel(my_widget);

        widgetScreen.addView(new_ui_widget);

        checkForSpinnerDataLoading();

        Button action = uiBuilder.action_button;

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSet setPost = new DataSet();
                setPost.set = new ArrayList<>();
                // first check if all fields are filled correctly
                for (int i = 0; i < uiBuilder.all_view_elements.size(); i++) {
                    switch (uiBuilder.all_view_elements.get(i).first) {
                        case 0: // Text Input
                            String tag = (String) uiBuilder.all_view_elements.get(i).second.getTag();
                            EditText current_view = (EditText) uiBuilder.all_view_elements.get(i).second;
                            if (tag != null && tag.matches("required") && current_view.getText().toString().isEmpty()) {

                                current_view.setError(EMPTY_ERROR_MSG);
                                return;
                                //Log.i(CLASS_NAME, "Field " + i + " is required!");
                            }
                            setPost.set.add(current_view.getText().toString());
                            break;
                        case 2: // Spinner
                            android.widget.Spinner current_spinner = (android.widget.Spinner) uiBuilder.all_view_elements.get(i).second;

                            String tag1 = (String) uiBuilder.all_view_elements.get(i).second.getTag();
                            if (tag1 != null && tag1.matches("required")) {

                                return;
                                //Log.i(CLASS_NAME, "Field " + i + " is required!");
                            }

                            setPost.set.add(selected_source_columns.get(current_spinner.getSelectedItemPosition() - 1));
                            break;
                        case 5: // Date Input
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


                // Print the whole dataset
                for (int i = 0; i < setPost.set.size(); i++)
                    Log.i(CLASS_NAME, "SET POST : " + setPost.set.get(i));

                for (int i = 0; i < my_widget.myActions.size(); i++) {
                    // Only create statement in this case
                    if (my_widget.myActions.get(i).type == 0) {
                        appLogic.sendPost(setPost, uiBuilder.current_action, my_widget.myTables.get(0));
                        break;
                    }

                }

                Toast.makeText(getApplicationContext(), "POST: " + uiBuilder.current_action.name, Toast.LENGTH_LONG).show();
                appLogic.setCurrentWidget(my_widget.myParent);
                finish();

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
                    System.out.println("spinners has to load data from table: " + spinners.get(i).second.items.referenced_table.table_name);
                    appLogic.getTableData(spinners.get(i).second.items.referenced_table, this);
                    //appLogic.getTableData();
                }
            }

        }
    }

    @Override
    public void invokeLoadingTableData(Table table) {

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


                        for(int i = 0; i < uiBuilder.spinner_data_to_load.size(); i++)
                            if(my_table == uiBuilder.spinner_data_to_load.get(i).second.items.referenced_table)
                            {

                                ArrayList<String> values = getReferencedAttributesForSpinner(uiBuilder.spinner_data_to_load.get(i).second);


                                uiBuilder.initSpinnerAdapter((android.widget.Spinner)
                                        uiBuilder.spinner_data_to_load.get(i).first, values);
                                uiBuilder.spinner_data_to_load.remove(uiBuilder.spinner_data_to_load.get(i));
                            }
                    }
                });
                break;
        }
    }


    private ArrayList<String> getReferencedAttributesForSpinner(Attribute spinnerAttribute)
    {
        ArrayList<String> attributes = new ArrayList<>();
        selected_source_columns = new ArrayList<>(spinnerAttribute.items.referenced_table.dataSets.size());

        for(int i = 0; i < spinnerAttribute.items.referenced_table.dataSets.size(); i++) {
            StringBuilder temp_string = new StringBuilder();
            for(int j = 0; j < spinnerAttribute.items.target_columns.size(); j++) {

                temp_string.append(spinnerAttribute.items.referenced_table.dataSets.get(i).set.get(
                        spinnerAttribute.items.target_columns.get(j)));

                if(j+1 != spinnerAttribute.items.target_columns.size())
                    temp_string.append(" | ");


            }
            attributes.add(temp_string.toString());
            selected_source_columns.add(spinnerAttribute.items.referenced_table.dataSets.get(i).set.
                    get(spinnerAttribute.items.source_column));
        }

        return attributes;
    }
}
