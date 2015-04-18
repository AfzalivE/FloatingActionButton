package com.github.clans.fab.sample;

import java.util.Locale;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.fab.sample.R;

/**
 * A very basic example of using FloatingActionButton with RecyclerView
 */
public class RecyclerViewActivity extends ActionBarActivity {

    private int mScrollOffset = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_activity);

        Locale[] availableLocales = Locale.getAvailableLocales();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new LanguageAdapter(availableLocales));

        fab.attachToRecyclerView(recyclerView);
    }

    private class LanguageAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Locale[] mLocales;

        private LanguageAdapter(Locale[] mLocales) {
            this.mLocales = mLocales;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);

            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mLocales[position].getDisplayName());
        }

        @Override
        public int getItemCount() {
            return mLocales.length;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
