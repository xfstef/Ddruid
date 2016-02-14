package com.frostbytetree.ddruid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * An activity representing a single WidgetListItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link WidgetListItemListActivity}.
 */
public class WidgetListItemDetailActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "ListDetailActivity";
    AppLogic appLogic;
    Widget widget;
    UIBuilder uiBuilder;
    ArrayList<String> set;
    ArrayList<Action> actions;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar

        actions.clear();

        Log.i(CLASS_NAME, "On Create Options Menu");
        Log.i(CLASS_NAME, "Adding Menu elements dynamically");


        for(int i = 0; i < widget.myChildren.size(); i++)
            for(int j = 0; j < widget.myChildren.get(i).myActions.size(); j++)
            {
                // if Action is not create add to menu
                if(widget.myChildren.get(i).myActions.get(j).type != 0) {
                    menu.add(0, i, 0, widget.myChildren.get(i).myActions.get(j).name);
                    actions.add(widget.myChildren.get(i).myActions.get(j));
                }
            }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            appLogic.temporary_dataSet = null;
            NavUtils.navigateUpTo(this, new Intent(this, WidgetListItemListActivity.class));
            return true;
        }
        else {
            //Log.i(CLASS_NAME, "Action : " + widget.myTables.get(0).myActions.get(id).name +" has attribute size: " + widget.myTables.get(0).myActions.get(id).attributes.size());
            //for(int i = 0; i < widget.myTables.get(0).myActions.get(id).attributes.size(); i++)
                //Log.i(CLASS_NAME, "Attribute: " + widget.myTables.get(0).myActions.get(id).attributes.get(i).name);

            //Log.i(CLASS_NAME, "-------------------------------");
            //

            boolean is_simple = true;
            for(int x = 0; x < widget.myTables.get(0).myActions.get(id).attributes.size(); x++)
                if(actions.get(id).attribute_readonly.get(x) == false){
                    is_simple = false;
                    break;
                }

            if(is_simple) {
                appLogic.sendPost(appLogic.temporary_dataSet, widget.myTables.get(0).myActions.get(id), widget.myTables.get(0));
                onBackPressed();
            }
            else
            {

            }


        }
        return super.onOptionsItemSelected(item);
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
        set = intent.getStringArrayListExtra(WidgetListItemDetailFragment.ARG_ITEM_ID);

        setContentView(R.layout.activity_widgetlistitem_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        //TODO: find out which attribute will be set as title
        getSupportActionBar().setTitle(set.toString());

        for(int i  = 0; i < widget.myTables.get(0).myActions.size(); i++)
            Log.i(CLASS_NAME, " the widget Table Action : " + widget.myTables.get(0).myActions.get(i).name);



        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG);

                for(int i  = 0; i < widget.myTables.get(0).myActions.size(); i++)
                {
                    snackbar.setAction(widget.myTables.get(0).myActions.get(i).name, null);
                }

                snackbar.show();
            }
        });
        */


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
            arguments.putStringArrayList(WidgetListItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringArrayListExtra(WidgetListItemDetailFragment.ARG_ITEM_ID));
            WidgetListItemDetailFragment fragment = new WidgetListItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.widgetlistitem_detail_container, fragment)
                    .commit();
        }
    }

}
