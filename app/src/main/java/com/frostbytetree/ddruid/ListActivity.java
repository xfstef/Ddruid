package com.frostbytetree.ddruid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

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
    Table myTable = null;
    RecyclerView recList;
    FrameLayout mainContent, loadingScreen;
    Data data;


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_search, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {


                if (myTable != null) {
                    Log.i(CLASS_NAME, "Text searched: " + query);
                    // final ArrayList<DataSet> filteredModelList = filter(myTable.dataSets, newText);
                    // tableAdapter.animateTo(filteredModelList);
                    // recList.scrollToPosition(0);
                    tableAdapter.getFilter().filter(query);
                    return true;
                } else
                    return false;

            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    private ArrayList<DataSet> filter(ArrayList<DataSet> dataSets, String query) {
        query = query.toLowerCase();

        final ArrayList<DataSet> filteredDataSetList = new ArrayList<>();
        for (DataSet dataSet : dataSets) {
            final String text = dataSet.set.toString().toLowerCase();
            if (text.contains(query)) {
                filteredDataSetList.add(dataSet);
            }
        }
        return filteredDataSetList;
    }

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

        appLogic.temporary_dataSet = null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainContent = (FrameLayout)findViewById(R.id.frameLayout);
        loadingScreen = (FrameLayout)findViewById(R.id.loading_circle);

        assert myWidget != null;
        getSupportActionBar().setTitle(myWidget.titleBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Table myTable = findTableWithinWidget(myWidget);
        if(myTable.dataSets.size() == 0) {
            this.myTable = myTable;
            appLogic.getTableData(myTable, this);
        }



        recList = (RecyclerView) findViewById(R.id.tablewidgetitem_list);
        tableAdapter = new TableListItemRecyclerViewAdapter(myTable.dataSets);
        tableAdapter.father = this;
        recList.setAdapter(tableAdapter);

        final SwipeRefreshLayout swipe_content = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeDataRefreshListener swipe_listener = new SwipeDataRefreshListener(this,swipe_content,myTable);

        /*
        swipe_content.post(new Runnable() {
            @Override
            public void run() {
                //while(myTable.attributes.size() == 0)
                    swipe_content.setRefreshing(true);
            }
        });
        */

        final Action defaultAction = findDefaultActionWithinWidget(myWidget);
        if(defaultAction != null)
        {
            FloatingActionButton floatingAction = (FloatingActionButton)findViewById(R.id.fabAction);
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

        if(myTable.dataSets.size() == 0) {
            Log.d(CLASS_NAME, "Time to call the data!");
            mainContent.setVisibility(View.GONE);
            loadingScreen.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        appLogic.temporary_dataSet = null;
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
                        mainContent.setVisibility(View.VISIBLE);
                        loadingScreen.setVisibility(View.GONE);
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
            extends RecyclerView.Adapter<TableListItemRecyclerViewAdapter.ViewHolder> implements Filterable {


        private List<DataSet> dataSets;
        private List<String> data;
        private List<String> filteredData;
        Table table;
        ListActivity father = null;

        public TableListItemRecyclerViewAdapter(ArrayList<DataSet> dataSets) {
            this.dataSets = dataSets;
            this.data = new ArrayList<>();
            this.filteredData = new ArrayList<>();
        }

        @Override
        public Filter getFilter() {
            return new ListFilter(this, data);
        }

        private class ListFilter extends Filter {

            private final TableListItemRecyclerViewAdapter adapter;

            private final List<String> originalList;

            private final List<String> filteredList;

            private ListFilter(TableListItemRecyclerViewAdapter adapter, List<String> originalList)
            {
                super();
                this.adapter = adapter;
                this.originalList = new LinkedList<>(originalList);
                Log.i(CLASS_NAME, "Original List size: " + originalList.size());
                this.filteredList = new ArrayList<>();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredList.clear();
                Log.i(CLASS_NAME, "Filter performing ...");
                final FilterResults results = new FilterResults();

                if (constraint.length() == 0) {
                    Log.i(CLASS_NAME, "Constraint empty, adding all List ...");
                    filteredList.addAll(originalList);
                } else {
                    Log.i(CLASS_NAME, "Filter constraint: " + constraint);
                    final String filterPattern = constraint.toString().toLowerCase().trim();

                    for (final String set : originalList) {
                        if (set.contains(filterPattern)) {
                            Log.i(CLASS_NAME, "Filtered Element: " + set.toString());
                            filteredList.add(set);
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                adapter.filteredData.clear();
                adapter.filteredData.addAll((ArrayList<String>) results.values);
                adapter.notifyDataSetChanged();
            }

        }

        /*

        public void setModels(ArrayList<DataSet> dataSet) {
            dataSets = new ArrayList<>(dataSet);
        }

        public DataSet removeItem(int position) {
            final DataSet model = dataSets.remove(position);
            notifyItemRemoved(position);
            return model;
        }

        public void addItem(int position, DataSet model) {
            dataSets.add(position, model);
            notifyItemInserted(position);
        }

        public void moveItem(int fromPosition, int toPosition) {
            final DataSet model = dataSets.remove(fromPosition);
            dataSets.add(toPosition, model);
            notifyItemMoved(fromPosition, toPosition);
        }

        public void animateTo(ArrayList<DataSet> datasets) {
            applyAndAnimateRemovals(datasets);
            applyAndAnimateAdditions(datasets);
            applyAndAnimateMovedItems(datasets);
        }

        private void applyAndAnimateRemovals(ArrayList<DataSet> datasets) {
            for (int i = datasets.size() - 1; i >= 0; i--) {
                final DataSet dataSet = datasets.get(i);
                if (!datasets.contains(dataSet)) {
                    removeItem(i);
                }
            }
        }

        private void applyAndAnimateAdditions(ArrayList<DataSet> datasets) {
            for (int i = 0, count = datasets.size(); i < count; i++) {
                final DataSet dataSet = datasets.get(i);
                if (!datasets.contains(dataSet)) {
                    addItem(i, dataSet);
                }
            }
        }

        private void applyAndAnimateMovedItems(ArrayList<DataSet> datasets) {
            for (int toPosition = datasets.size() - 1; toPosition >= 0; toPosition--) {
                final DataSet dataSet = datasets.get(toPosition);
                final int fromPosition = dataSets.indexOf(dataSet);
                if (fromPosition >= 0 && fromPosition != toPosition) {
                    moveItem(fromPosition, toPosition);
                }
            }
        }
        */

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {


            String tuple = new String();
            for(LinkedHashMap.Entry<Integer, ArrayList<Integer>> entry : myWidget.list_view_columns.entrySet())
            {
                String temp = new String();
                // Atrritubte type 2 = spinner
                // needed for referenced attribute names
                if(entry.getValue().size() > 0 && myWidget.myTables.get(0).attributes.get(entry.getKey()).attribute_type == 2) {
                    if (!myWidget.myTables.get(0).attributes.get(entry.getKey()).items.referenced_table.dataSets.isEmpty()) {
                        for (int l = 0; l < entry.getValue().size(); l++) {
                            // this adds every referenced attribute for this element
                            temp = temp + " " + getReferencedItem(Integer.valueOf(dataSets.get(position).set.get(entry.getKey())),
                                    entry.getValue().get(l), myWidget.myTables.get(0).attributes.get(entry.getKey()));
                        }
                    } else {
                        temp = dataSets.get(position).set.get(entry.getKey());
                        appLogic.getTableData(myWidget.myTables.get(0).attributes.get(entry.getKey()).items.referenced_table, father);
                    }
                }else {
                    temp = dataSets.get(position).set.get(entry.getKey());
                }
                tuple = tuple + " " + temp;


            }
            data.add(position, tuple);
            holder.mItem = dataSets.get(position).set;
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

    // helper function for onBind
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
