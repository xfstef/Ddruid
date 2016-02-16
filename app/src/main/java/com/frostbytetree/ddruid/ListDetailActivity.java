package com.frostbytetree.ddruid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * An activity representing a single WidgetListItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ListActivity}.
 */
public class ListDetailActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "ListDetailActivity";
    AppLogic appLogic;
    Widget widget;
    UIBuilder uiBuilder;
    ArrayList<String> set;
    String title;
    ArrayList<Action> actions;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar

        actions.clear();

        Log.i(CLASS_NAME, "On Create Options Menu");
        Log.i(CLASS_NAME, "Adding Menu elements dynamically");


        int index = 0;
        for(int i = 0; i < widget.myChildren.size(); i++) {
            for (int j = 0; j < widget.myChildren.get(i).myActions.size(); j++) {
                // if Action is not create add to menu
                if (widget.myChildren.get(i).myActions.get(j).type != 0) {
                    Log.i(CLASS_NAME, "Created Item ID: " + i + "MenuItem: " + widget.myChildren.get(i).myActions.get(j).name);
                    menu.add(0, index, 0, widget.myChildren.get(i).myActions.get(j).name);
                    index++;
                    actions.add(widget.myChildren.get(i).myActions.get(j));
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            appLogic.temporary_dataSet = null;
            NavUtils.navigateUpTo(this, new Intent(this, ListActivity.class));
            return true;
        }
        else {
            boolean is_simple = true;
            // Found the coresponding action which have been selected
            // Iterate through the attributes and see if readonly is available within
            for(int x = 0; x < widget.myTables.get(0).myActions.size(); x++) {
                if(widget.myTables.get(0).myActions.get(x) == actions.get(id))
                {
                    for(int y = 0; y < widget.myTables.get(0).myActions.get(x).attribute_readonly.size(); y++)
                    {
                        if(widget.myTables.get(0).myActions.get(x).attribute_readonly.get(y) == false) {
                            is_simple = false;
                            break;
                        }
                    }
                }

            }

            if(is_simple) {
                appLogic.sendPost(appLogic.temporary_dataSet, widget.myTables.get(0).myActions.get(id), widget.myTables.get(0));
                onBackPressed();
            }
            else
            {

                Log.i(CLASS_NAME, "Action selected: " + actions.get(id).name);
                // set child widget as current
                for(int i = 0; i < widget.myChildren.size(); i++) {
                    for (int j = 0; j < widget.myChildren.get(i).myActions.size(); j++)
                        if (widget.myChildren.get(i).myActions.get(j) == actions.get(id)) {

                            // set child widget as current
                            Widget current_widget = widget.myChildren.get(i);
                            appLogic.setCurrentWidget(current_widget);

                            // set this widget as parent widget
                            appLogic.currentWidget.myParent = widget;

                            Intent intent = new Intent(getApplicationContext(), WidgetActionActivity.class);
                            // Started from floating button
                            startActivityForResult(intent, 5);
                            break;
                        }
                }

            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // go back to list if action was made
        appLogic.temporary_dataSet = null;
        NavUtils.navigateUpTo(this, new Intent(this, ListActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(CLASS_NAME, " has been created!");
        appLogic = AppLogic.getInstance();
        widget = appLogic.currentWidget;
        setTheme(appLogic.configFile.custom_color);
        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);
        actions = new ArrayList<>();

        Intent intent = getIntent();
        set = intent.getStringArrayListExtra(ListDetailFragment.ARG_ITEM_ID);
        title = intent.getStringExtra(ListDetailFragment.ARG_TITLE_ID);
        setContentView(R.layout.activity_list_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        Log.i(CLASS_NAME, "title: " + title);
        getSupportActionBar().setTitle(title);


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putStringArrayList(ListDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringArrayListExtra(ListDetailFragment.ARG_ITEM_ID));
            arguments.putString(ListDetailFragment.ARG_TITLE_ID,
                    getIntent().getStringExtra(ListDetailFragment.ARG_TITLE_ID));
            ListDetailFragment fragment = new ListDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.list_detail_container, fragment)
                    .commit();
        }
    }

}
