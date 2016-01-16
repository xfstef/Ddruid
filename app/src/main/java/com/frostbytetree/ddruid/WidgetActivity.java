package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
    AppLogic appLogic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_activity);
        if (Build.VERSION.SDK_INT > 21){
            setupWindowAnimations();
        }

        toolbar = (Toolbar)findViewById(R.id.widget_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appLogic = AppLogic.getInstance();

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

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
