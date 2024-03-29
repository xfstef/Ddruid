package com.frostbytetree.ddruid;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

//import com.frostbytetree.ddruid.dummy.DummyContent;

/**
 * A fragment representing a single WidgetListItem detail screen.
 * This fragment is either contained in a {@link ListActivity}
 * in two-pane mode (on tablets) or a {@link ListDetailActivity}
 * on handsets.
 */
public class ListDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_TITLE_ID = "title_id";
    private static final String CLASS_NAME = "ITEM DETAIL FRAGMENT";

    ArrayList<String> dataSet;
    String title;
    AppLogic appLogic;
    Widget widget;
    UIBuilder uiBuilder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(CLASS_NAME, "has been started!");
        appLogic = AppLogic.getInstance();
        widget = appLogic.currentWidget;
        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this.getActivity());

        if (getArguments().containsKey(ARG_TITLE_ID) && getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            dataSet = getArguments().getStringArrayList(ARG_ITEM_ID);
            title = getArguments().getString(ARG_TITLE_ID);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
               appBarLayout.setTitle(title);
            }

        }

    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

        Log.i(CLASS_NAME, "Adding Menu elements dynamically");
        for(int i = 0; i < widget.myTables.get(0).myActions.size(); i++)
            menu.add(0,i,0,widget.myTables.get(0).myActions.get(i).name);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list_detail, container, false);
        LinearLayout mainContent = (LinearLayout)rootView.findViewById(R.id.listItemContent);
        Log.i(CLASS_NAME, dataSet.toString());

        Widget new_ui_widget = uiBuilder.inflateTableDetailModel(mainContent, widget, dataSet);
        //mainContent.addView(new_ui_widget);

        return rootView;
    }
}
