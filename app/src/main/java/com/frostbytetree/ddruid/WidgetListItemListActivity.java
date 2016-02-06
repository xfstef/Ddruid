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

    AppLogic appLogic;
    Widget myWidget;
    UIBuilder uiBuilder;
    WidgetListItemRecyclerViewAdapter tableAdapter;

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
        toolbar.setTitle(getTitle());

        initListItems();
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
        tableAdapter = new WidgetListItemRecyclerViewAdapter(myTable.dataSets);
        //tableAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(tableAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                //showDetailsFragment(my_table);

                // TODO: Init Fragment with selected DataSet and table Actions for the selected item

                //showDetailsFragment selectedDataSet
                //Toast.makeText(getApplicationContext(), "Selected DataSet element: " + my_table.dataSets.get(position).set.toString(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    Table findTableWithinWidget(Widget widget)
    {
        if(widget.myTables.size() != 0)
        {
            return widget.myTables.get(0);
        }
        else
            return null;
    }


    @Override
    public void invokeLoadingTableData(Table table) {
        Log.d("Widget List Activity", "Table invocation requested for: " + table.table_name);
        appLogic.getTableData(table, this);
    }

    @Override
    public void signalDataArrived(final Table my_table) {
        System.out.println("DATA HAS ARRIVED!");

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
                        System.out.println("Data Set2: " + my_table.dataSets.toString());
                        tableAdapter.updateDataSetList(my_table.dataSets);
                    }
                });
                break;

        }


    }

    //  private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
    //    recyclerView.setAdapter(new WidgetListItemRecyclerViewAdapter(DummyContent.ITEMS));
    //  }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(myWidget != null)
            appLogic.currentWidget = myWidget.myParent;
        finish();
    }



    public class WidgetListItemRecyclerViewAdapter
            extends RecyclerView.Adapter<WidgetListItemRecyclerViewAdapter.ViewHolder> {

        private ArrayList<DataSet> dataSets;

        public WidgetListItemRecyclerViewAdapter(ArrayList<DataSet> dataSets) {
            this.dataSets = dataSets;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widgetlistitem_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            System.out.println("Current dataset: " + dataSets.get(position).set.toString());
            holder.mTextView.setText(dataSets.get(position).set.toString());

            holder.set = dataSets.get(position).set;
            //holder.mIdView.setText(mValues.get(position).id);
            //holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(WidgetListItemDetailFragment.ARG_ITEM_ID, holder.set.toString());
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
            System.out.println("DataSet3: " + dataSets.toString());
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
                 mTextView = (TextView)view.findViewById(R.id.content);
             }
        }
        /*
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }*/
    }
}
