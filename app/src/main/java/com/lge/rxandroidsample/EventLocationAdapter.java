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

import rx.Observable;
import rx.util.async.Async;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

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
            return new FilterResults();
        }

        private List<String> queryRecentLocations(String filter) {
            List<String> results = new ArrayList<>();
            for (int i = 1; i < 7; i++) {
                results.add(filter + Thread.currentThread().getName() + i);
            }
            return results;
        }

        private List<String> queryContacts(String filter) {
            List<String> results = new ArrayList<>();
            for (int i = 5; i < 10; i++) {
                results.add(filter + Thread.currentThread().getName() + i);
            }
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Observable<List<String>> recentLocationsAsync = Async.start(() -> queryRecentLocations(constraint.toString()));
            Observable<List<String>> contactsAsync = Async.start(() -> queryContacts(constraint.toString()));

            Observable.zip(recentLocationsAsync, contactsAsync, (recentLocations, contacts) -> {
                List<String> values = new ArrayList<>();
                for (String recentLocation : recentLocations) {
                    if (!contacts.contains(recentLocation))
                        values.add(recentLocation);
                }
                values.addAll(contacts);
                return values;
            }).observeOn(mainThread()).subscribe(
                    values -> {
                        clear();
                        addAll(values);
                    },
                    error -> {
                        clear();
                    }
            );
        }
    }
}
