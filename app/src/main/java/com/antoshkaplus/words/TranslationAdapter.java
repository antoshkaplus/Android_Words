package com.antoshkaplus.words;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antoshkaplus.words.model.Translation;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 7/30/15.
 */
public class TranslationAdapter extends BaseAdapter {

    Context context;
    List<Translation> items = new ArrayList<>();

    public TranslationAdapter(Context context, List<Translation> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }
        Translation item = (Translation)getItem(position);
        TextView foreignView = (TextView)convertView.findViewById(android.R.id.text1);
        foreignView.setText(item.foreignWord.word);
        TextView nativeView = (TextView)convertView.findViewById(android.R.id.text2);
        nativeView.setText(item.nativeWord.word);
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getCount() {
        return items.size();
    }

}
