package com.antoshkaplus.words;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.antoshkaplus.words.model.Stats;
import com.antoshkaplus.words.model.Translation;
import com.j256.ormlite.android.AndroidDatabaseResults;

/**
 * Created by antoshkaplus on 7/26/16.
 */
public class StatsAdapter extends BaseAdapter {

    Context context;
    TranslationRepository repo;
    AndroidDatabaseResults items;
    //TranslationFilter filter = new TranslationFilter(); implements Filterable

    public StatsAdapter(Context context, TranslationRepository repo) {
        this.context = context;
        this.repo = repo;
        try {
            this.items = repo.getStatsRawResults();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.view_translation, null);
        }
        Stats item = (Stats)getItem(position);
        TextView foreignView = (TextView)convertView.findViewById(R.id.foreign_word);
        foreignView.setText(item.foreignWord);
        TextView successView = (TextView)convertView.findViewById(R.id.success_score);
        successView.setText(String.valueOf(item.getSuccessScore()));
        TextView failureView = (TextView)convertView.findViewById(R.id.failure_score);
        failureView.setText(String.valueOf(item.getFailureScore()));
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        Stats stats = null;
        try {
            stats = repo.getStats(items, position);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stats;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return items.getCount();
    }

}
