package com.antoshkaplus.words;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.antoshkaplus.words.model.Translation;
import com.j256.ormlite.android.AndroidDatabaseResults;

import java.util.ArrayList;

/**
 * Created by antoshkaplus on 7/30/15.
 */
public class TranslationAdapter extends BaseAdapter {

    Context context;
    TranslationRepository repo;
    AndroidDatabaseResults items;
    //TranslationFilter filter = new TranslationFilter(); implements Filterable

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

// TODO
//    @Override
//    public Filter getFilter() {
//        return null;
//    }
//
//    // should probably override to get indices too
//    private class TranslationFilter extends Filter {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            String filterString = constraint.toString().toLowerCase();
//            FilterResults results = new FilterResults();
//
//            int count = getCount();
//            final ArrayList<String> nlist = new ArrayList<String>(count);
//
//            String filterableString ;
//
//            for (int i = 0; i < count; i++) {
//                filterableString = getItem(i);
//                if (filterableString.toLowerCase().contains(filterString)) {
//                    nlist.add(filterableString);
//                }
//            }
//
//            results.values = nlist;
//            results.count = nlist.size();
//
//            return results;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            filteredData = (ArrayList<String>) results.values;
//            notifyDataSetChanged();
//        }
//    }
}
