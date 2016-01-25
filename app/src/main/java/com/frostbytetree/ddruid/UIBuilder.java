package com.frostbytetree.ddruid;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by XfStef on 11/27/2015.
 */

// The UI Builder is a background Thread that uses the Data Models in order to create the inflators
// needed for the various app Widgets.

    //TODO Load the Data Models
    //TODO Interpret the Data Models
    //TODO Build the Widget Inflators ???
    //TODO Signal the AppLogic to start the TemporaryWidget Activity

public class UIBuilder {
    private static UIBuilder ourInstance = new UIBuilder();

    Data data;
    Context context;

    // WidgetViews widgetViews;

    public static UIBuilder getInstance() {
        return ourInstance;
    }

    private UIBuilder() {
        this.data = data.getInstance();
    }

    public void setContext(Context context){
        this.context = context;
    }

    // This function takes a widget and iterates through all the tables
    // and returns the build custom model
    public Widget inflate_model(Widget widget){

        addTestElement(widget);
        return widget;
        /*
        for(int i = 0;  i < widget.myTables.size(); i++)
        {
            if(!buildWidgetsFromTable(widget.myTables.get(i)))
                return null;
        }
        return true;*/
        // test.addElement();

        // widgetViews.the_widgets.add(test);

    }

    private boolean buildWidgetsFromTable(Table currentTable)
    {
        for(int i = 0; i < currentTable.attributes.size(); i++)
            return true;
        return false;
    }

    private void addTestElement(LinearLayout widgetLinearLayout){
        Button a1 = new Button(context);
        a1.setText("@string/app_name");
        a1.setVisibility(View.VISIBLE);
        widgetLinearLayout.addView(a1, (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)));
        System.out.println("Element Should be added!");

        // AppCompatActivity widgetActivity = (AppCompatActivity)context;
        // MainActivity.setContentView(L2);
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