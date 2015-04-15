package com.lge.rxandroidsample;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
            if (constraint == null) return new FilterResults();
            String filter = constraint.toString();


            AsyncTask<Void, Void, List<String>> task = new AsyncTask<Void, Void, List<String>>() {
                @Override
                protected List<String> doInBackground(Void[] params) {
                    return queryRecentLocations(filter);
                }
            }.execute();

            List<String> contacts = queryContacts(filter);

            List<String> values = new ArrayList<>();

            try {
                for (String recentLocation: task.get()) {
                    if (!contacts.contains(recentLocation))
                        values.add(recentLocation);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
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
                results.add(filter + Thread.currentThread().getName() + i);
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
