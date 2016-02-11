package com.frostbytetree.ddruid;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Created by XfStef on 11/27/2015.
 */

// The UI Builder is a background Thread that uses the Data Models in order to create the inflators
// needed for the various app Widgets.

public class UIBuilder {
    private static final String CLASS_NAME = "UIBuilder";


    public static final short IS_INPUT_TEXT = 0;
    //public static final short IS_EDIT_TEXT = 1;
    public static final short IS_SPINNER = 2;
    public static final short IS_DATE_INPUT = 3;
    public static final short IS_ACTION_BUTTON = 4;

    private static UIBuilder ourInstance = new UIBuilder();

    Data data;
    Context context;
    AppLogic appLogic;
    IDataInflateListener mCallback;

    //TEMP solution the views will be created in seperated classes
    ArrayList<Pair<Short, View>> all_view_elements = new ArrayList<>();


    Button action_button;
    Action current_action;

    //TODO: find a better way to fill the spinners this is a temporary_solution
    // this is a Pair of <Spinner(View)><TABLE> which to load
    ArrayList<Pair<View, com.frostbytetree.ddruid.Spinner>> spinner_data_to_load = new ArrayList<>();

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

    public void setCallback(IDataInflateListener callback) { this.mCallback = callback; }

    // This function takes a widget and iterates through all the tables
    // and returns the build custom model
    public Widget inflateModel(Widget widget)
    {
        loadInitialState();
        // step I: find table within Widget.myTables.TableName which matches to the Widget.myTableActions.first (Table)
        for(int i = 0; i < widget.myTableActions.size(); i++)
        {
            Table inflating_table = Utils.findTableToAction(widget.myTables, widget.myTableActions.get(i).first);
            Log.d(CLASS_NAME,"Table name: " + inflating_table.table_name);

            // Step II: find the corresponding action which matches to the found table
            for(int j = 0; j < widget.myTables.size(); j++)
            {
                Action inflating_action = Utils.findTableAction(inflating_table.myActions, widget.myTableActions.get(i).second);
                Log.d(CLASS_NAME, "Inflating action: " + inflating_action.name);
                widget = inflateFormAndAddUIElements(widget, inflating_action);
                current_action = inflating_action;

            }
        }
        return widget;
    }

    Widget inflateTableDetailModel(LinearLayout ui_content, Widget widget, ArrayList<String> data_to_be_displayed)
    {

        // Only check routine
        if(BuildConfig.DEBUG && widget.myTables.get(0).attributes.size() != data_to_be_displayed.size())
            throw new AssertionError(Log.d(CLASS_NAME,"attributes and data to displayed are not the same"));
        addTextViewElements(ui_content, widget, data_to_be_displayed);

        return widget;
    }





    Widget inflateFormAndAddUIElements(Widget widget, Action action)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16,16,16,16);
        for(int i = 0; i < action.attributes.size(); i++)
        {

            View element = checkValueUIElement(action.attributes.get(i), action.attribute_required.get(i), action.attribute_readonly.get(i));

            assert element != null;
            widget.addView(element, layoutParams);

            // checkValueUIElement(widget, action.attributes.get(i).name);

        }

        // At last take button from the action
        this.action_button = new Button(context);
        action_button.setText(action.name);
        action_button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        action_button.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));

        all_view_elements.add(new Pair <Short, View>(IS_ACTION_BUTTON,action_button));
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
                item = addEditTextElement(attribute, required, read_only);
                //all_view_elements.add(item);
                break;
            // Spinner
            case 2:
                item = addSpinnerElement(attribute, required, read_only);
                //all_view_elements.add(item);
                break;
            case 5:
                // Calender Object
                item = addDateElement(attribute, required, read_only);
                break;
        }

        return item;
    }

    public RecyclerView buildWidgetRecyclerViewer(FrameLayout mainLayout) {
        View content = LayoutInflater.from(context).inflate(R.layout.recycler_view_menu, null);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        RecyclerView recList = (RecyclerView) content.findViewById(R.id.itemsRecyclerView);
        final SwipeRefreshLayout swipe_content = (SwipeRefreshLayout) content.findViewById(R.id.swipeRefreshLayout);

        recList.setLayoutManager(llm);
        mainLayout.addView(swipe_content);

        swipe_content.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                // Load items
                // ...

                // Load complete
                // Update the adapter and notify data set changed
                // ...

                // Stop refresh animation
                swipe_content.setRefreshing(false);
            }
        });

        return recList;
    }

    private void addTextViewElements(LinearLayout ui_content, Widget widget, ArrayList<String> data_to_be_displayed)
    {
        for(int i = 0; i < data_to_be_displayed.size(); i++)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.text_view_form, null);
            TextView textViewDescription = (TextView)view.findViewById(R.id.widget_item_text_description);
            Log.d(CLASS_NAME,"Attribute: " + widget.myTables.get(0).attributes.get(i).name);
            textViewDescription.setText(widget.myTables.get(0).attributes.get(i).name);
            TextView textViewData = (TextView)view.findViewById(R.id.widget_item_text_detail);
            textViewData.setText(data_to_be_displayed.get(i));
            ui_content.addView(view);
        }
    }

    private View addEditTextElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        View content = LayoutInflater.from(context).inflate(R.layout.text_input_form, null);
        // TextInputLayout input_item = new TextInputLayout(context);
        TextInputLayout input_item = (TextInputLayout)content.findViewById(R.id.input_layout);
        // input_item.setVisibility(View.VISIBLE);

        // EditText input_text = new EditText(context);
        EditText input_text = (EditText)content.findViewById(R.id.input);

        // if field is required a teg will be set key 0 -> 1(true)
        if(required) {
            input_item.setHint(attribute.name + " (required)");
            input_text.setTag("required");
        }
        else
            input_item.setHint(attribute.name);

        if(read_only)
            input_item.setEnabled(false);

        all_view_elements.add(new Pair<Short, View>(IS_INPUT_TEXT, input_text));
        return input_item;
    }

    private View addDateElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        final DatePickerDialog.OnDateSetListener onDateSetListener;
        View content = LayoutInflater.from(context).inflate(R.layout.date_input_form, null);
        LinearLayout lin_1 = (LinearLayout)content.findViewById(R.id.lin_layout_date_input);

        TextInputLayout input_item = (TextInputLayout)content.findViewById(R.id.input_layout);
        final EditText editDate = (EditText)content.findViewById(R.id.input);
        Button selectDate = (Button)content.findViewById(R.id.select_date);

        if(required) {
            input_item.setHint(attribute.name + " (required)");
            editDate.setTag("required");
        }
        else
            input_item.setHint(attribute.name);


        if(read_only)
            selectDate.setEnabled(false);

        selectDate.setText("SELECT");
        //selectDate.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //selectDate.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));

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

        all_view_elements.add(new Pair<Short, View>((short)5, editDate));
        return lin_1;
    }

    private View addSpinnerElement(Attribute attribute, Boolean required, Boolean read_only)
    {
        View content = LayoutInflater.from(context).inflate(R.layout.spinner_input_form, null);
        // TextInputLayout input_item = new TextInputLayout(context);
        MaterialSpinner spinner_input = (MaterialSpinner)content.findViewById(R.id.input_spinner);
        System.out.println("Attribute name is: " + attribute.name);

        if(required) {
            spinner_input.setTag("required");
            spinner_input.setHint(attribute.name + " (required)");
        }
        else
            spinner_input.setHint(attribute.name);

        if(read_only)
            spinner_input.setEnabled(false);

        Log.i(CLASS_NAME, "----------- ENTERING --------------");
        Log.i(CLASS_NAME, attribute.name.toString());


        Log.i(CLASS_NAME, attribute.items.dataSetName.get(0));

        ArrayList<String> referenced_attributes = attribute.items.dataSetName;
        ArrayList<String> spinnerData = new ArrayList<>();

        Table reference_table = getTableFromReferenceName(attribute.reference_name);
        if(reference_table != null)
        {
            spinnerData = getReferencedDataFromTable(reference_table, referenced_attributes);
        }

        /*
        String spinner_table = attribute.reference_name;
        Table table = null;
        Log.d(CLASS_NAME, "Spinner table for the attribute reference: " + spinner_table);
        ArrayList<String> spinnerData = new ArrayList<>();
        for(int i = 0; i < data.tables.size(); i++)
        {
            //Get the coresponding table for the spinner reference
            if(data.tables.get(i).table_name.matches(spinner_table))
            {
                table = data.tables.get(i);
                System.out.println("Spinner table found: " + data.tables.get(i).table_name);


                if(table.dataSets != null) {
                    spinnerData = table.attributes.get(0).items.dataSetName;
                    //spinnerData = table.dataSets;
                }
                System.out.println("Spinner data: " + spinnerData.toString());
                break;
            }
        } */

        initSpinnerAdapter(spinner_input, spinnerData);

        // If no data could be loaded
        if(spinnerData.size() == 0)
        {

            spinner_data_to_load.add(new Pair<View, com.frostbytetree.ddruid.Spinner>(spinner_input, attribute.items));
        }
        all_view_elements.add(new Pair<Short, View>(IS_SPINNER, spinner_input));
        //referenced_attributes.add(new Pair<Attribute, String>(attribute,attribute.items.items));

        return spinner_input;

    }

    public void initSpinnerAdapter(Spinner spinner, ArrayList<String> spinnerData)
    {
        SpinnerAdapter adapter = new SpinnerAdapter(context,
                android.R.layout.simple_spinner_dropdown_item,spinnerData);
        spinner.setAdapter(adapter);
    }

    // Set every variable to it's initial form
    private void loadInitialState()
    {
        all_view_elements.clear();
        //referenced_attributes.clear();
        this.action_button = null;
        this.current_action = null;

    }


    private Table getTableFromReferenceName(String referenced_table_name)
    {
        for(int i = 0; i < data.tables.size(); i++)
            if(data.tables.get(i).table_name.matches(referenced_table_name))
                return data.tables.get(i);

        return null;
    }


    // Referenced Data from the spinner
    private ArrayList<String> getReferencedDataFromTable(Table referenced_table, ArrayList<String> referenced_attributes)
    {
        ArrayList<String> spinnerData = new ArrayList<>();

        StringBuilder currentDataSet;

        for(int i = 0; i < referenced_attributes.size(); i++)
            Log.i(CLASS_NAME,"Referenced attribute   :::" + referenced_attributes.get(i));


        for (int i = 0; i < referenced_attributes.size(); i++)
        {
            for(int j = 0; j < referenced_table.attributes.size(); j++)
            {
                if(referenced_attributes.get(i).matches(referenced_table.attributes.get(j).name)) {
                    Log.i(CLASS_NAME, referenced_table.dataSets.toString());
                    //currentDataSet = new StringBuilder();
                    //currentDataSet.append(referenced_table.dataSets.get(0).set.get(i));
                    Log.i(CLASS_NAME, "Referenced Attribute> " + referenced_table.attributes.get(j).name);

                    //Log.i(CLASS_NAME, "Referenced DataSet: " + currentDataSet);
                }
            }
        }

        return spinnerData;
    }
}

class SpinnerAdapter extends ArrayAdapter<String>
{
    ArrayList<String> data_sets;
    Context context;

    public SpinnerAdapter(Context context, int textViewResourceId, ArrayList<String> dataSets)
    {
        super(context, textViewResourceId, dataSets);
        this.context = context;
        this.data_sets = dataSets;
    }

    public int getCount(){
        return data_sets.size();
    }

    public String getItem(int position){
        return data_sets.get(position);
    }

    public long getItemId(int position){
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = new TextView(context);

        label.setText(data_sets.get(position).toString());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(data_sets.get(position).toString());

        return label;
    }
}

class SwipeDataRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

    Context context;
    Table table_to_reload;
    SwipeRefreshLayout swipe_content;
    AppLogic appLogic;

    public SwipeDataRefreshListener(Context context, SwipeRefreshLayout swiper, Table table)
    {
        appLogic = AppLogic.getInstance();
        this.swipe_content = swiper;
        this.context = context;
        this.table_to_reload = table;
        this.swipe_content.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        // Refresh items
        // Load items
        // ...
        try {
            appLogic.getTableData(table_to_reload, (AppCompatActivity)context);
        }
        catch (Exception e){
            System.out.println("Sry casting activity from context not possible");
        }
        // Load complete
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        swipe_content.setRefreshing(false);
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
