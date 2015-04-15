package com.lge.rxandroidsample;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soohyun.baik on 2015-04-15.
 */
public class EventLocationAdapter extends ArrayAdapter<String> implements Filterable {
    private final Context mContext;
    private MyFilter mFilter;

    public EventLocationAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        this.mContext = context;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }

    class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filter = constraint.toString();
            List<String> recentLocations = queryRecentLocations(filter);
            List<String> contacts = queryContacts(filter);

            List<String> values = new ArrayList<>();

            for (String recentLocation: recentLocations) {
                if (!contacts.contains(recentLocation))
                    values.add(recentLocation);
            }
            values.addAll(contacts);

            FilterResults results = new FilterResults();
            results.values = values;
            results.count = values.size();
            return results;
        }

        private List<String> queryRecentLocations(String filter) {
            List<String> results = new ArrayList<>();
            for (int i=1; i<7; i++) {
                results.add(filter + i);
            }
            return results;
        }

        private List<String> queryContacts(String filter) {
            List<String> results = new ArrayList<>();
            for (int i=5; i<10; i++) {
                results.add(filter + i);
            }
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.count > 0) {
                addAll((java.util.Collection<? extends String>) results.values);
            }
        }
    }
}
