package com.frostbytetree.ddruid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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

    private static final String ACTIVITY_NAME = "Item Detail Activity";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appLogic = AppLogic.getInstance();
        myWidget = appLogic.currentWidget;
        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);
        uiBuilder.setCallback(this);
        setContentView(R.layout.activity_widgetlistitem_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert myWidget != null;
        getSupportActionBar().setTitle(myWidget.titleBar);

        initListItems();


        if (findViewById(R.id.widgetlistitem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void initListItems()
    {
        View mainContent = findViewById(R.id.frameLayout);
        final Table myTable = findTableWithinWidget(myWidget);

        RecyclerView recyclerView = uiBuilder.buildTableRecyclerViewer((FrameLayout)mainContent, myTable);
        tableAdapter = new TableListItemRecyclerViewAdapter(myTable.dataSets);
        //tableAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(tableAdapter);

         /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        //View recyclerView = findViewById(R.id.widgetlistitem_list);
        //assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);
        
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
        Log.d(ACTIVITY_NAME, "Table invocation requested for: " + table.table_name);
        appLogic.getTableData(table, this);
    }

    @Override
    public void signalDataArrived(final Table my_table) {
        Log.d(ACTIVITY_NAME, "DATA HAS ARRIVED FOR: " + my_table.table_name);

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
            holder.mTextView.setText(dataSets.get(position).set.toString());
            holder.set = dataSets.get(position).set;
            //holder.mIdView.setText(mValues.get(position).id);
            //holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putStringArrayList(WidgetListItemDetailFragment.ARG_ITEM_ID, holder.set);
                        WidgetListItemDetailFragment fragment = new WidgetListItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.widgetlistitem_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WidgetListItemDetailActivity.class);
                        intent.putExtra(WidgetListItemDetailFragment.ARG_ITEM_ID, holder.set);

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
             public TextView mTextView;
             public ArrayList<String> set;
             public View mView;

             public ViewHolder(View view)
             {
                 super(view);
                 mView = view;
                 mTextView = (TextView)view.findViewById(R.id.txtListAttr);
             }
        }
    }
}
