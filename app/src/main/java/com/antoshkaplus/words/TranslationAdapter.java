package com.antoshkaplus.words;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antoshkaplus.words.model.Translation;
import com.j256.ormlite.android.AndroidDatabaseResults;

/**
 * Created by antoshkaplus on 7/30/15.
 */
public class TranslationAdapter extends BaseAdapter {

    Context context;
    TranslationRepository repo;
    AndroidDatabaseResults items;

    public TranslationAdapter(Context context, TranslationRepository repo) {
        this.context = context;
        this.repo = repo;
        try {
            this.items = repo.getTranslationsRawResults();
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
        Translation item = (Translation)getItem(position);
        TextView foreignView = (TextView)convertView.findViewById(R.id.word_0);
        foreignView.setText(item.foreignWord);
        TextView nativeView = (TextView)convertView.findViewById(R.id.word_1);
        nativeView.setText(item.nativeWord);
        return convertView;
    }



    @Override
    public Object getItem(int position) {
        Translation translation = null;
        try {
            translation = repo.getTranslation(items, position);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return translation;
    }

    @Override
    public long getItemId(int position) {
        // check for null
        return ((Translation)getItem(position)).id;
    }

    @Override
    public int getCount() {
        return items.getCount();
    }

}
