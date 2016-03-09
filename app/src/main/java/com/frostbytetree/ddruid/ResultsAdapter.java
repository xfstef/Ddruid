package com.frostbytetree.ddruid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomi on 09/03/16.
 */
public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder>{
    private List<String> results;

    public ResultsAdapter(ArrayList<String> results)
    {
        this.results = results;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        public TextView mTextView;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mTextView = (TextView)view.findViewById(R.id.txtListAttr);
        }

    }
}
