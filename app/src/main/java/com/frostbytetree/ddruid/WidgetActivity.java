package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.appindexing.*;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.api.GoogleApiClient;

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


    RecycleViewWidgetAdapter widgetAdapter;
    RecycleViewDataSetAdapter tableAdapter;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //my_widget = new Widget(this);
        widgetViews = WidgetViews.getInstance();

        my_widget = getCurrentWidget();

        setContentView(R.layout.widget_activity);
        if (Build.VERSION.SDK_INT > 21) {
            setupWindowAnimations();
        }
        initScreenItems();


        if (my_widget != null) {
            checkWidgetType();
        }

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        appLogic = AppLogic.getInstance();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void checkWidgetType() {
        // 0 - List;
        // 1 - Form;
        // 2 - Detail;
        // 3 - Code Scanner;
        switch (my_widget.widgetType) {
            case 0:
                initWidgetList();
                break;
            case 1:
                //initFormWidget();
                break;
            case 4:
                initTableList();
                break;
            default:

        }
    }

     void initWidgetList() {
        RecyclerView recList = new RecyclerView(this);
        widgetScreen.addView(recList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        //String[] list = {"First element", "Second element"};
        widgetAdapter = new RecycleViewWidgetAdapter(this, my_widget.myChildren);
        recList.setAdapter(widgetAdapter);
        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Widget selected_widget = my_widget.myChildren.get(position);
                if(position == 0)
                {
                    Toast.makeText(getApplicationContext(), "<Selected Widget has no data>", Toast.LENGTH_LONG).show();
                    //Intent iResult = new Intent();
                    //setResult(Activity.RESULT_OK, iResult);
                    //finish();
                }
                //Toast.makeText(getApplicationContext(), "Selected Widget element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();
            }
        }));

    }

     void initTableList() {
         RecyclerView recList = new RecyclerView(this);

         final Table my_table = findTableWithinWidget(my_widget);

         if(my_table == null)
         {
             System.out.println("No Table found within Widget!");
             return;
         }
        widgetScreen.addView(recList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        tableAdapter = new RecycleViewDataSetAdapter(this, my_table.dataSets);
        recList.setAdapter(tableAdapter);
        recList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DataSet selectedDataSet = my_table.dataSets.get(position);

                //showDetailsFragment selectedDataSet
                Toast.makeText(getApplicationContext() ,"Selected DataSet element: " + my_widget.myChildren.get(position).titleBar, Toast.LENGTH_LONG).show();
            }
        }));

    }

    private void showDetailsFragment() {


    }


    Table findTableWithinWidget(Widget widget)
    {
        if(widget.myTables.size() != 0)
            return widget.myTables.get(0);
        else
            return null;
    }

    public void addRecycleViewItemList()
    {
        if(widgetAdapter != null)
            widgetAdapter.notifyItemInserted(my_widget.myChildren.size()-1);

        if(widgetAdapter != null)
            widgetAdapter.notifyItemInserted(my_widget.myChildren.size()-1);
    }

    Widget getCurrentWidget() {
        Widget found_widget;
        Intent intent = getIntent();
        for (int x = 0; x < widgetViews.the_widgets.size(); x++) {
            if (widgetViews.the_widgets.get(x).id == intent.getIntExtra("widget", 0)) {
                found_widget = widgetViews.the_widgets.get(x);
                System.out.println("Widget title: " + found_widget.titleBar);
                return found_widget;
            }
        }
        return null;
    }

    void initScreenItems() {
        widgetScreen = (LinearLayout) findViewById(R.id.mainContent);
        toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(my_widget.titleBar);

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

    @Override
    public void onResume() {
        super.onResume();
        appLogic.setCurrentWidget(this);
    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
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
}


class RecycleViewWidgetAdapter extends RecyclerView.Adapter<RecycleViewWidgetAdapter.ViewHolder>
{
    private ArrayList<Widget> child_widgets;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(View v)
        {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.txtListAttr);
        }
    }

    public RecycleViewWidgetAdapter(Context context, ArrayList<Widget> dataSet)
    {
        this.mContext = context;
        this.child_widgets = dataSet;
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
        holder.mTextView.setText(child_widgets.get(position).titleBar);
        if(holder.mTextView.getText().toString().matches("Enter Ticket"))
            holder.mTextView.setTextColor(mContext.getResources().getColor(R.color.disabled_background));
    }

    @Override
    public int getItemCount() {
        return child_widgets.size();
    }


}

class RecycleViewDataSetAdapter extends RecyclerView.Adapter<RecycleViewDataSetAdapter.ViewHolder>
{
    private ArrayList<DataSet> dataSets;
    Context mContext;
    public TextView mTextView;

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(View v)
        {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.txtListAttr);

        }
    }

    public RecycleViewDataSetAdapter(Context context, ArrayList<DataSet> dataSet)
    {
        this.mContext = context;
        this.dataSets = dataSet;
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
        holder.mTextView.setText(dataSets.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return dataSets.size();
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