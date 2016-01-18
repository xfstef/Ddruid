package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
                initWidgetList();
                break;
            case 1:

                break;
            case 4:
                //initDataSetList();
                break;
            default:

        }
    }

    private void initWidgetList()
    {
        RecyclerView recList = new RecyclerView(this);
        widgetScreen.addView(recList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        //String[] list = {"First element", "Second element"};
        RecycleViewWidgetAdapter adapter = new RecycleViewWidgetAdapter(my_widget.myChildren);
        recList.setAdapter(adapter);
        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Widget selected_widget = my_widget.myChildren.get(position);

                // Intent iResult new Intent();
                // iResult.putExtra()
                // setResult(Activity.RESULT_OK, iResult);
                Toast.makeText(getApplicationContext() ,"Selected element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();
            }
        }));

    }
    /*
    private void initWidgetList()
    {
        RecyclerView recList = new RecyclerView(this);
        widgetScreen.addView(recList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        //String[] list = {"First element", "Second element"};
        RecycleViewWidgetAdapter adapter = new RecycleViewWidgetAdapter(my_widget.myChildren);
        recList.setAdapter(adapter);
        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Widget selected_widget = my_widget.myChildren.get(position);

                // Intent iResult new Intent();
                // iResult.putExtra()
                // setResult(Activity.RESULT_OK, iResult);
                Toast.makeText(getApplicationContext() ,"Selected element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();
            }
        }));

    }*/



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
        widgetScreen = (LinearLayout)findViewById(R.id.mainContent);
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


class RecycleViewWidgetAdapter extends RecyclerView.Adapter<RecycleViewWidgetAdapter.ViewHolder>
{
    private ArrayList<Widget> dataSet;

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(View v)
        {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.txtListAttr);
        }
    }

    public RecycleViewWidgetAdapter(ArrayList<Widget> dataSet)
    {
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(dataSet.get(position).titleBar);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


}

class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
}