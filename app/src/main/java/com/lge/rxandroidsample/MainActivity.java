package com.lge.rxandroidsample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        textView.setAdapter(new EventLocationAdapter(this));


        Button button = (Button) findViewById(R.id.button);
        // retrolambda (with type: functional interface)
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Hello Retrolambda", Toast.LENGTH_SHORT).show();
        });

        // rxandroid - ViewObservable
        ViewObservable.clicks(button).subscribe(v -> {
            Toast.makeText(this, "Hello RxAndroid", Toast.LENGTH_SHORT).show();
        });

        // Event stream with drag & drop
        Observable<MotionEvent> touches = touches(button);

        Observable<MotionEvent> downs = touches.filter(e -> e.getAction() == MotionEvent.ACTION_DOWN);
        Observable<MotionEvent> ups = touches.filter(e -> e.getAction() == MotionEvent.ACTION_UP);
        Observable<MotionEvent> moves = touches.filter(e -> e.getAction() == MotionEvent.ACTION_MOVE);

        downs.flatMap(d -> {
            float x = button.getX() - d.getRawX();
            float y = button.getY() - d.getRawY();
            return moves.map(m -> Pair.create(x + m.getRawX(), y + m.getRawY())).takeUntil(ups);
        }).subscribe(pos -> {
            button.setX(pos.first);
            button.setY(pos.second);
        });
        // Schduler (with Async) without AndroidSchedulers(error!) & with AndroidSchedulers
//        Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    button.setText("Hello, " + s + "s");
//                });

        // Schduler (with Async) with Time + Location
        Observable<Long> data = getAsyncData();
        Observable.combineLatest(data, moves.map(m -> (int) button.getX() + ", " + (int) button.getY()), (a, b) -> a + " : " + b)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    button.setText("Hello, " + s + "");
                });
    }

    private Observable<Long> getAsyncData() {
        return Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static Observable<MotionEvent> touches(View view) {
        return Observable.<MotionEvent>create(observer -> {
            Assertions.assertUiThread();
            final CompositeOnTouchListener composite = CachedListeners.getFromViewOrCreate(view);

            final View.OnTouchListener listener = (view1, event) -> {
                observer.onNext(event);
                return false;
            };

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(() -> composite.removeOnTouchListener(listener));

            composite.addOnTouchListener(listener);
            observer.add(subscription);
        });
    }

    private static class CompositeOnTouchListener implements View.OnTouchListener {
        private final List<View.OnTouchListener> listeners = new ArrayList<View.OnTouchListener>();

        public boolean addOnTouchListener(final View.OnTouchListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnTouchListener(final View.OnTouchListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public boolean onTouch(final View view, MotionEvent event) {
            List<View.OnTouchListener> copy = new ArrayList<>(listeners);
            for (final View.OnTouchListener listener : copy) {
                listener.onTouch(view, event);
            }
            return false;
        }
    }

    private static class CachedListeners {
        private static final Map<View, CompositeOnTouchListener> sCachedListeners = new WeakHashMap<View, CompositeOnTouchListener>();

        public static CompositeOnTouchListener getFromViewOrCreate(final View view) {
            final CompositeOnTouchListener cached = sCachedListeners.get(view);

            if (cached != null) {
                return cached;
            }

            final CompositeOnTouchListener listener = new CompositeOnTouchListener();

            sCachedListeners.put(view, listener);
            view.setOnTouchListener(listener);

            return listener;
        }
    }
}
