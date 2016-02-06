package com.frostbytetree.ddruid;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tomi on 04/02/16.
 */
public class WidgetListDetail extends Fragment {
    Table current_table;

    //public WidgetListDetail(Table table) {this.current_table = table;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_layout,
                container, false);



        return view;
    }
    
}
