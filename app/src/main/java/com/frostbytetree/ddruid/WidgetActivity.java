package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Activity will be created when a widget or sub-widget is opened.

    //TODO Load the Widget Inflators ???
    //TODO Load the Raw Data
    //TODO Check for free memory
    //TODO If no free memory then Talk to SQLite Controller

public class WidgetActivity extends AppCompatActivity {

    Toolbar toolbar;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;

    AppLogic appLogic;
    WidgetViews widgetViews;
    Widget my_widget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //my_widget = new Widget(this);
        widgetViews = WidgetViews.getInstance();

        my_widget = getCurrentWidget();



        setContentView(R.layout.widget_activity);
        if (Build.VERSION.SDK_INT > 21){
            setupWindowAnimations();
        }
        if(my_widget != null) {
            initScreenItems();
            checkWidgetType();
        }

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        appLogic = AppLogic.getInstance();


        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

    }

    void checkWidgetType()
    {
        // 0 - List;
        // 1 - Form;
        // 2 - Detail;
        // 3 - Code Scanner;

        switch(my_widget.widgetType)
        {
            case 0:

                break;
            case 1:

                break;
            default:

        }
    }

    Widget getCurrentWidget()
    {
        Widget found_widget;
        Intent intent = getIntent();
        for(int x = 0; x < widgetViews.the_widgets.size(); x++) {
            if (widgetViews.the_widgets.get(x).id == intent.getIntExtra("widget", 0)) {
                found_widget = widgetViews.the_widgets.get(x);
                System.out.println("Widget title: " + found_widget.titleBar);
                return found_widget;
            }
        }
        return null;
    }

    void initScreenItems()
    {
        toolbar = (Toolbar)findViewById(R.id.widget_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(my_widget.titleBar);

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

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
    public void onResume(){
        super.onResume();
        appLogic.setCurrentWidget(this);
    }

    @TargetApi(21)
    private void setupWindowAnimations()
    {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

}


class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private String[] dataSet;

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(TextView v)
        {
            super(v);
            mTextView = v;
        }
    }

    public RecycleViewAdapter(String[] dataSet)
    {
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }


}