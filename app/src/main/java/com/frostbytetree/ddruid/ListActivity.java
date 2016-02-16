package com.frostbytetree.ddruid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * An activity representing a list of WidgetListItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ListDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ListActivity extends AppCompatActivity implements IDataInflateListener{

    private static final String CLASS_NAME = "List Activity";

    AppLogic appLogic;
    Widget myWidget;
    UIBuilder uiBuilder;
    TableListItemRecyclerViewAdapter tableAdapter;
    FloatingActionButton floatingAction;
    Data data;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch(id)
        {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        data = Data.getInstance();
        appLogic = AppLogic.getInstance();
        setTheme(appLogic.configFile.custom_color);
        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        setContentView(R.layout.activity_list);

        initListItems();

        if (findViewById(R.id.tablewidgetitem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.

            mTwoPane = true;
        }
    }

    private void initListItems()
    {
        myWidget = appLogic.currentWidget;

        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);

        appLogic.iDataInflateListener = this;


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert myWidget != null;
        getSupportActionBar().setTitle(myWidget.titleBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Table myTable = findTableWithinWidget(myWidget);
        if(myTable.dataSets.size() == 0)
            appLogic.getTableData(myTable, this);

        RecyclerView recList = (RecyclerView) findViewById(R.id.tablewidgetitem_list);
        tableAdapter = new TableListItemRecyclerViewAdapter(myTable.dataSets);
        tableAdapter.father = this;
        recList.setAdapter(tableAdapter);

        final SwipeRefreshLayout swipe_content = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeDataRefreshListener swipe_listener = new SwipeDataRefreshListener(this,swipe_content,myTable);


        final Action defaultAction = findDefaultActionWithinWidget(myWidget);
        if(defaultAction != null)
        {
            floatingAction = (FloatingActionButton)findViewById(R.id.fabAction);
            floatingAction.setVisibility(View.VISIBLE);
            floatingAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for(int i = 0; i < myWidget.myChildren.size(); i++) {
                        for (int j = 0; j < myWidget.myChildren.get(i).myActions.size(); j++)
                            if (myWidget.myChildren.get(i).myActions.get(j) == defaultAction) {

                                // set child widget as current
                                Widget current_widget = myWidget.myChildren.get(i);
                                appLogic.setCurrentWidget(current_widget);

                                // set this widget as parent widget
                                appLogic.currentWidget.myParent = myWidget;

                                Intent intent = new Intent(getApplicationContext(), WidgetActionActivity.class);
                                // Started from floating button
                                startActivityForResult(intent, 5);
                                break;
                            }
                    }
                }
            });
        }
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        initListItems();
    }


    private Action findDefaultActionWithinWidget(Widget widget)
    {
        for(int i = 0; i < widget.myChildren.size(); i++)
            for(int j = 0;j < widget.myChildren.get(i).myActions.size(); j++)
                if(widget.myChildren.get(i).myActions.get(j).type == 0) // 0 is the defined magic number for default Action
                    return widget.myChildren.get(i).myActions.get(j);

        return null;
    }

    //TODO: not only first table, in the future maybe more tables within one widget
    Table findTableWithinWidget(Widget widget)
    {
        if(widget.myTables.size() != 0)
        {
            return widget.myTables.get(0);
        }
        else
            return null;
    }

    /*
    INTERFACE METHOD IMPLEMENTATIONS
     */
    @Override
    public void signalDataArrived(Table my_table) {
        Log.d(CLASS_NAME, "DATA HAS ARRIVED FOR: " + my_table.table_name);
        Log.d(CLASS_NAME, "Widget type: " + myWidget.widgetType);

        // 0 - Widget-List;
        // 1 - Form;
        // 2 - Detail - could be never used;
        // 3 - Code Scanner;
        // 4 - List with datasets
        // 31 - Code Scanner + GPS;

        if(my_table != myWidget.myTables.get(0))
            my_table = myWidget.myTables.get(0);

        switch (myWidget.widgetType) {
            case 4:
                final Table finalMy_table = my_table;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tableAdapter.updateDataSetList(finalMy_table);
                    }
                });
                break;

        }

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(myWidget != null)
            appLogic.currentWidget = myWidget.myParent;
        Log.d(CLASS_NAME, appLogic.currentWidget.titleBar);
        finish();
    }



    public class TableListItemRecyclerViewAdapter
            extends RecyclerView.Adapter<TableListItemRecyclerViewAdapter.ViewHolder> {

        private ArrayList<DataSet> dataSets;
        Table table;
        ListActivity father = null;

        public TableListItemRecyclerViewAdapter(ArrayList<DataSet> dataSets) {
            this.dataSets = dataSets;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = dataSets.get(position).set;

            String tuple = new String();
            for(LinkedHashMap.Entry<Integer, ArrayList<Integer>> entry : myWidget.list_view_columns.entrySet())
            {
                String temp = new String();
                // Atrritubte type 2 = spinner
                if(entry.getValue().size() > 0 && myWidget.myTables.get(0).attributes.get(entry.getKey()).attribute_type == 2) {
                    if (!myWidget.myTables.get(0).attributes.get(entry.getKey()).items.referenced_table.dataSets.isEmpty()) {
                        for (int l = 0; l < entry.getValue().size(); l++)
                            temp = temp + " " + getReferencedItem(Integer.valueOf(dataSets.get(position).set.get(entry.getKey())),
                                    entry.getValue().get(l), myWidget.myTables.get(0).attributes.get(entry.getKey()));
                    } else {
                        temp = dataSets.get(position).set.get(entry.getKey());
                        appLogic.getTableData(myWidget.myTables.get(0).attributes.get(entry.getKey()).items.referenced_table, father);
                    }
                }else {
                    temp = dataSets.get(position).set.get(entry.getKey());
                }
                tuple = tuple + " " + temp;

            }
            holder.mTextView.setText(tuple);
            holder.currentDataSet = dataSets.get(position);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putStringArrayList(ListDetailFragment.ARG_ITEM_ID, holder.mItem);
                        arguments.putString(ListDetailFragment.ARG_TITLE_ID, holder.mTextView.getText().toString());
                        ListDetailFragment fragment = new ListDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.tablewidgetitem_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ListDetailActivity.class);
                        intent.putExtra(ListDetailFragment.ARG_ITEM_ID, holder.mItem);
                        intent.putExtra(ListDetailFragment.ARG_TITLE_ID, holder.mTextView.getText().toString());
                        appLogic.temporary_dataSet = holder.currentDataSet;
                        context.startActivity(intent);
                    }
                }
            });
        }

        public void updateDataSetList(Table data_sets)
        {
            this.dataSets = data_sets.dataSets;
            this.notifyDataSetChanged();
            table = data_sets;
        }

        @Override
        public int getItemCount() {
            return dataSets.size();
        }


         class ViewHolder extends RecyclerView.ViewHolder{
             public View mView;
             public TextView mTextView;
             public ArrayList<String> mItem;
             public DataSet currentDataSet;

             public ViewHolder(View view)
             {
                 super(view);
                 mView = view;
                 mTextView = (TextView)view.findViewById(R.id.txtListAttr);
             }

        }
    }

    private String getReferencedItem(Integer source_data, Integer target_pos, Attribute attribute) {
        String item = null;

        for(int y = 0; y < attribute.items.referenced_table.dataSets.size(); y++)
            if(Integer.valueOf(attribute.items.referenced_table.dataSets.get(y).set.get(attribute.items.source_column)) == source_data) {
                item = attribute.items.referenced_table.dataSets.get(y).set.get(target_pos);
                return item;
            }
        return item;
    }
}
