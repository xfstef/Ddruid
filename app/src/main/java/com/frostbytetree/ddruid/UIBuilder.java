package com.frostbytetree.ddruid;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
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

import org.w3c.dom.Attr;

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
    AppLogic appLogic = null;
    IDataInflateListener mCallback;

    //TEMP solution the views will be created in seperated classes
    ArrayList<Pair<Short, View>> all_view_elements = new ArrayList<>();


    Button action_button;
    Action current_action;

    //TODO: find a better way to fill the spinners this is a temporary_solution
    // this is a Pair of <Spinner(View)><TABLE> which to load
    ArrayList<Pair<View, Attribute>> spinner_data_to_load = new ArrayList<>();

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
        loadInitialState(widget);
        // step I: find table within Widget.myTables.TableName which matches to the Widget.myTableActions.first (Table)
        for(int i = 0; i < widget.myTableActions.size(); i++)
        {
            if(!widget.myTables.isEmpty()) {
                Table inflating_table = Utils.findTableToAction(widget.myTables, widget.myTableActions.get(i).first);
                Log.d(CLASS_NAME, "Table name: " + inflating_table.table_name);


                // Step II: find the corresponding action which matches to the found table
                for (int j = 0; j < widget.myTables.size(); j++) {
                    Action inflating_action = Utils.findTableAction(inflating_table.myActions, widget.myTableActions.get(i).second);
                    Log.d(CLASS_NAME, "Inflating action: " + inflating_action.name);
                    widget = inflateFormAndAddUIElements(widget, inflating_action);
                    current_action = inflating_action;

                }
            }
        }
        return widget;
    }


    // This is for now the special hoerbiger inflator
    /*
    public Widget inflateStep(Step current_step, AppLogic appLogic)
    {
        Log.i(CLASS_NAME, "----------------------------------");
        Log.i(CLASS_NAME, "Current Step: " + current_step.name);
        Log.i(CLASS_NAME, "Current Step ui label: " + current_step.ui_label);
        Log.i(CLASS_NAME, "Current Step type: " + current_step.ui_element_type);

        //Log.i(CLASS_NAME, "Current Step referenced table: " + current_step.lookupTable.referenced_table_name);

        loadInitialState(appLogic.currentWidget);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        appLogic.currentWidget.removeAllViews();

        switch(current_step.ui_element_type)
        {
            case 0:
                View element = addTextViewElementStep(current_step);
                appLogic.currentWidget.addView(element, layoutParams);
                break;
            case 1:

                break;
            default:

        }

        Log.i(CLASS_NAME, "Current Widget: " + appLogic.currentWidget.titleBar);
        return appLogic.currentWidget;
    }
    */

    public View addTextViewElementStep(Step step)
    {
        View content = LayoutInflater.from(context).inflate(R.layout.text_input_form, null);
        // TextInputLayout input_item = new TextInputLayout(context);
        TextInputLayout input_item = (TextInputLayout)content.findViewById(R.id.input_layout);
        input_item.setTag(step.ui_label);
        // input_item.setVisibility(View.VISIBLE);

        // EditText input_text = new EditText(context);
        EditText input_text = (EditText)content.findViewById(R.id.input);
        input_text.setTag(step.ui_label);

        input_text.setFocusable(false);
        if(step.ui_label != null)
            input_item.setHint(step.ui_label);

        all_view_elements.add(new Pair<Short, View>(IS_INPUT_TEXT, input_text));
        all_view_elements.add(new Pair<Short, View>(IS_INPUT_TEXT, input_item));

        return input_item;
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
        layoutParams.setMargins(16, 16, 16, 16);
        widget.removeAllViews();
        for(int i = 0; i < action.attributes.size(); i++)
        {

            View element = checkValueUIElement(action.attributes.get(i), action.attribute_required.get(i), action.attribute_readonly.get(i));

            assert element != null;
            widget.addView(element, layoutParams);

            // checkValueUIElement(widget, action.attributes.get(i).name);

        }

        // At last take button from the action
        View content = LayoutInflater.from(context).inflate(R.layout.action_button, null);
        this.action_button = (Button)content.findViewById(R.id.action_button);
        action_button.setText(action.name);

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
            input_text.setEnabled(false);

        all_view_elements.add(new Pair<Short, View>(IS_INPUT_TEXT, input_text));

        // Distinguate if called from WidgetActionActivity
        if(appLogic.temporary_dataSet == null)
            return input_item;
        else {
            for(int i = 0; i < appLogic.currentWidget.myTables.get(0).attributes.size(); i++)
                if(attribute.name.matches(appLogic.currentWidget.myTables.get(0).attributes.get(i).name)) {
                    input_text.setText(appLogic.temporary_dataSet.set.get(i));
                    break;
                }
        }

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
                                editDate.setText(year  + "-"
                                        + (monthOfYear + 1) + "-" + dayOfMonth);

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



        ArrayList<String> spinnerData = new ArrayList<>();

        Table reference_table = attribute.items.referenced_table;

        /*
        if(reference_table != null)
        {
            spinnerData = getReferencedDataFromTable(attribute);
        }
        */

        initSpinnerAdapter(spinner_input, spinnerData);

        // If no data could be loaded
        if(spinnerData.size() == 0)
        {

            spinner_data_to_load.add(new Pair<View, Attribute>(spinner_input, attribute));
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
    public void loadInitialState(Widget widget)
    {
        widget.removeAllViews();
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
    private ArrayList<String> getReferencedDataFromTable(Attribute attribute)
    {
        ArrayList<String> spinnerData = new ArrayList<>();

        StringBuilder tuple = new StringBuilder();
        for(int i = 0; i < attribute.vader.dataSets.size(); i++)
        {
            for(int j = 0; j < attribute.items.referenced_table.dataSets.size(); j++)
            {
                tuple.append("Test");
                tuple.append(" noch mal");
                spinnerData.add(tuple.toString());
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
