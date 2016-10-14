package io.github.agobi.wtfimm;

import android.app.Application;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by gobi on 10/11/16.
 */
public class FireBaseApplication extends Application {
    private static final String TAG = "FireBaseApplication";
    private static int startOfMonth = 5;



    static class Transaction {
        public long timestamp;
        public int amount;
        public String source, target, note, emailid;

        public Transaction() {}

        @Override
        public String toString() {
            return "Transaction["+timestamp+", "+amount+" "+source+" -> "+target+" ("+note+")]";
        }
    }

    static class Month implements Comparable<Month> {
        private final String name;
        private final long start, end;

        public Month(Long timestamp) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp*1000);
            cal.add(Calendar.DAY_OF_MONTH, -startOfMonth+1);
            Calendar mc = Calendar.getInstance();
            mc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), startOfMonth, 0, 0, 0);
            mc.set(Calendar.MILLISECOND ,0);
            name = DateFormat.format("yyyy-MM", mc).toString();
            start = mc.getTimeInMillis();

            mc.add(Calendar.MONTH, 1);
            end = mc.getTimeInMillis();
        }

        @Override
        public int compareTo(Month o) {
            return (int)(o.start - start);
        }

        @Override
        public int hashCode() {
            return (int)(start^(start>>>32));
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof  Month) {
                Month other = (Month)obj;
                return start == other.start;
            }
            return false;
        }

        public String getName() {
            return name;
        }

        public long getStart() {
            return start;
        }
    }

    public interface MonthsChangeListener {
        void monthsChanged(Month[] months);
    }

    private Set<Month> months = new TreeSet<>();
    private List<MonthsChangeListener> monthsChangeListeners = new ArrayList<>();

    public void addMonthsChangeListener(MonthsChangeListener monthsChangeListener) {
        monthsChangeListeners.add(monthsChangeListener);
        monthsChangeListener.monthsChanged(months.toArray(new Month[0]));
    }

    public void removeMonthsChangeListener(MonthsChangeListener monthsChangeListener) {
        monthsChangeListeners.remove(monthsChangeListener);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        DatabaseReference myRef =  database.getReference("transactions");
        myRef.keepSynced(true);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Transaction data = dataSnapshot.getValue(Transaction.class);
                if(months.add(new Month(data.timestamp))) {
                    for (MonthsChangeListener m : monthsChangeListeners) {
                        m.monthsChanged(months.toArray(new Month[0]));
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Config changed");
        super.onConfigurationChanged(newConfig);
    }
}
