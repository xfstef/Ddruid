package com.frostbytetree.ddruid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

/**
 * An activity representing a list of WidgetListItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WidgetListItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WidgetListItemListActivity extends AppCompatActivity implements IDataInflateListener{

    private static final String CLASS_NAME = "Item List Activity";

    AppLogic appLogic;
    Widget myWidget;
    UIBuilder uiBuilder;
    TableListItemRecyclerViewAdapter tableAdapter;

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
        setTheme(R.style.AppTheme_Indigo);
        super.onCreate(savedInstanceState);

        appLogic = AppLogic.getInstance();
        myWidget = appLogic.currentWidget;
        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        setContentView(R.layout.activity_tablewidgetitem_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert myWidget != null;
        getSupportActionBar().setTitle(myWidget.titleBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        final Table myTable = findTableWithinWidget(myWidget);
        if(myTable.dataSets.size() == 0)
            appLogic.getTableData(myTable, this);

        RecyclerView recList = (RecyclerView) findViewById(R.id.tablewidgetitem_list);
        tableAdapter = new TableListItemRecyclerViewAdapter(myTable.dataSets);
        recList.setAdapter(tableAdapter);

        final SwipeRefreshLayout swipe_content = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeDataRefreshListener swipe_listener = new SwipeDataRefreshListener(this,swipe_content,myTable);

        
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
    public void invokeLoadingTableData(Table table) {
        Log.d(CLASS_NAME, "Table invocation requested for: " + table.table_name);
        appLogic.getTableData(table, this);
    }

    @Override
    public void signalDataArrived(final Table my_table) {
        Log.d(CLASS_NAME, "DATA HAS ARRIVED FOR: " + my_table.table_name);

        // 0 - Widget-List;
        // 1 - Form;
        // 2 - Detail - could be never used;
        // 3 - Code Scanner;
        // 4 - List with datasets
        // 31 - Code Scanner + GPS;

        switch(myWidget.widgetType)
        {
            case 4:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tableAdapter.updateDataSetList(my_table.dataSets);
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
            holder.mTextView.setText(dataSets.get(position).set.toString());
            holder.currentDataSet = dataSets.get(position);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putStringArrayList(WidgetListItemDetailFragment.ARG_ITEM_ID, holder.mItem);
                        WidgetListItemDetailFragment fragment = new WidgetListItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.tablewidgetitem_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WidgetListItemDetailActivity.class);
                        intent.putExtra(WidgetListItemDetailFragment.ARG_ITEM_ID, holder.mItem);
                        appLogic.temporary_dataSet = holder.currentDataSet;
                        context.startActivity(intent);
                    }
                }
            });
        }

        public void updateDataSetList(ArrayList<DataSet> data_sets)
        {
            this.dataSets = data_sets;
            this.notifyDataSetChanged();
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
}
