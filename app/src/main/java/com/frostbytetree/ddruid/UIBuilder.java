package com.frostbytetree.ddruid;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
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
    AppLogic appLogic;

    ArrayList<View> all_view_elements = new ArrayList<>();

    //TODO: find a better way to fill the spinners this is a temporary_solution
    // this is a Pair of <Spinner(View)><TABLE> which to load
    ArrayList<Pair<View, Table>> spinner_data_to_load = new ArrayList<>();

    //WidgetViews widgetViews;

    public static UIBuilder getInstance() {
        return ourInstance;
    }

    private UIBuilder() {
        this.data = Data.getInstance();
        this.appLogic = AppLogic.getInstance();
    }

    public void setContext(Context context){
        this.context = context;
    }

    // This function takes a widget and iterates through all the tables
    // and returns the build custom model
    public Widget inflate_model(Widget widget){

        // step I: find table within Widget.myTables.TableName which matches to the Widget.myTableActions.first (Table)
        for(int i = 0; i < widget.myTableActions.size(); i++)
        {
            Table inflating_table = Utils.findTableToAction(widget.myTables, widget.myTableActions.get(i).first);
            System.out.println("Table name: " + inflating_table.table_name);

            // Step II: find the corresponding action which matches to the found table
            for(int j = 0; j < widget.myTables.size(); j++)
            {
                Action inflating_action = Utils.findTableAction(inflating_table.myActions, widget.myTableActions.get(i).second);
                System.out.println("Inflating action: " + inflating_action.name);
                widget = inflateAndAddUIElements(widget, inflating_action);
            }
        }
        return widget;
    }

    Widget inflateAndAddUIElements(Widget widget, Action action)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16,16,16,16);
        for(int i = 0; i < action.attributes.size(); i++)
        {

            View element = checkValueUIElement(action.attributes.get(i), action.attribute_required.get(i), action.attribute_readonly.get(i));

            //TODO: delete this if statement because there shouldn't be any view unhandled
            if(element != null)
                widget.addView(element, layoutParams);

            // checkValueUIElement(widget, action.attributes.get(i).name);

        }

        // At last take button from the action
        Button action_button = new Button(context);
        action_button.setText(action.name);
        action_button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        action_button.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));

        action_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Not implemented yet!", Toast.LENGTH_LONG).show();
            }
        });

        all_view_elements.add(action_button);
        widget.addView(action_button, layoutParams);

        return widget;
    }

    private View checkValueUIElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        View item = null;
        System.out.println("Attribute TYPE: " + attribute.attribute_type);
        switch(attribute.attribute_type)
        {
            // Normal Text Input
            case 0:
                item = addTextElement(attribute,required,read_only);
                all_view_elements.add(item);
                break;
            // Spinner
            case 2:
                item = addSpinnerElement(attribute, required, read_only);
                all_view_elements.add(item);
                break;
            case 5:
                //TODO: Add Calender
                item = addDateElement(attribute, required, read_only);
                break;
        }

        return item;
    }

    private View addTextElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        TextInputLayout input_item = new TextInputLayout(context);
        input_item.setVisibility(View.VISIBLE);
        EditText input_text = new EditText(context);
        input_text.setVisibility(View.VISIBLE);
        input_text.setHint(attribute.name);
        input_item.addView(input_text, (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)));

        return input_item;
    }

    private View addDateElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        final DatePickerDialog.OnDateSetListener onDateSetListener;
        LinearLayout lin_1 = new LinearLayout(context);
        lin_1.setOrientation(LinearLayout.HORIZONTAL);
        final EditText editDate = new EditText(context);

        editDate.setEms(10);
        editDate.setEnabled(false);
        Button selectDate = new Button(context);
        selectDate.setPadding(10, 10, 10, 10);

        selectDate.setText("Select Date");
        selectDate.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        selectDate.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));

        selectDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of TimePickerDialog and return it
                DatePickerDialog dpd = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                editDate.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, year, month, day);
                dpd.show();
            }
        });

        lin_1.addView(editDate, (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));
        lin_1.addView(selectDate,(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)));

        return lin_1;
    }

    private View addSpinnerElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        Spinner spinner_input = new Spinner(context);
        spinner_input.setVisibility(View.VISIBLE);
        // TODO get the data and initialize
        String spinner_table = attribute.reference_name;
        Table table = null;
        //System.out.println("Spinner table: " + spinner_table);
        for(int i = 0; i < data.tables.size(); i++)
        {
            //Get the coresponding table for the spinner reference
            if(data.tables.get(i).table_name.matches(spinner_table))
            {
                table = data.tables.get(i);
                System.out.println("Spinner table found: " + data.tables.get(i).table_name);
                break;
            }
        }

        ArrayList<DataSet> spinnerData = table.dataSets;
        System.out.println("Spinner data: " + spinnerData.toString());

        if(spinnerData != null)
        {
            if(spinnerData.size() > 0)
                initSpinnerAdapter(spinner_input, spinnerData);
            else {

                spinner_data_to_load.add(new Pair<View, Table>(spinner_input, table));
                System.out.println("Sry data could not be loaded");
            }
        }

        return spinner_input;

    }

    public void initSpinnerAdapter(Spinner spinner, ArrayList<DataSet> spinnerData)
    {
        SpinnerAdapter adapter = new SpinnerAdapter(context,
                android.R.layout.simple_spinner_item,spinnerData);
        spinner.setAdapter(adapter);
    }

}

class SpinnerAdapter extends ArrayAdapter<DataSet>
{
    ArrayList<DataSet> data_sets;
    Context context;

    public SpinnerAdapter(Context context, int textViewResourceId, ArrayList<DataSet> dataSets)
    {
        super(context, textViewResourceId, dataSets);
        this.context = context;
        this.data_sets = dataSets;
    }

    public int getCount(){
        return data_sets.size();
    }

    public DataSet getItem(int position){
        return data_sets.get(position);
    }

    public long getItemId(int position){
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = new TextView(context);

        label.setText(data_sets.get(position).set.toString());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(data_sets.get(position).set.toString());

        return label;
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
        System.out.println("DataSet1: " + dataSets.toString() + "\n");
    }

    public void setNewData(ArrayList<DataSet> data_sets)
    {
        System.out.println("DataSet3: " + dataSets.toString());
        this.dataSets = data_sets;
        this.notifyDataSetChanged();
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
        holder.mTextView.setText(dataSets.get(position).set.toString());
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

class Utils
{
    static Table findTableToAction(ArrayList<Table> tables, String table_name)
    {
        for(int k = 0; k < tables.size(); k++)
        {
            if(tables.get(k).table_name.matches(table_name))
            {
                return tables.get(k);
            }
        }
        return null;
    }


    static Action findTableAction(ArrayList<Action> actions, String action_name)
    {
        for(int i = 0; i < actions.size(); i++)
        {
            if(actions.get(i).name.matches(action_name))
            {
                return actions.get(i);
            }
        }
        return null;
    }
}