package io.github.agobi.wtfimm;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.res.Configuration;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.model.Transaction;

public class FireBaseApplication extends Application {
    private static final String TAG = "FireBaseApplication";
    private DatabaseReference transactionsReference;
    private DatabaseReference categoriesReference;
    private WTFIMMSettings mSettings;

    public static final Category defaultCategory = new Category("???");

    public DatabaseReference getTransactions() {
        return transactionsReference;
    }

    public interface CategoryChangeListener {
        void categoryChange(Map<String, Category> categories);
    }
    private final List<CategoryChangeListener> categoryChangeListeners = new ArrayList<>();;

    public void addCategoryChangeListener(CategoryChangeListener ccl) {
        categoryChangeListeners.add(ccl);
        ccl.categoryChange(mCategories);
    }

    public void removeCategoryChangeListener(CategoryChangeListener ccl) {
        categoryChangeListeners.remove(ccl);
    }

    private Map<String, Category> mCategories = new HashMap<>();

    public Map<String, Category> getCategories() {
        return mCategories;
    }


    public static String getDay(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp*1000);
        return DateFormat.format("MM-dd", cal).toString();
    }

    public interface MonthsChangeListener {
        void monthsChanged(Month[] months);
    }

    private Set<Month> months = new TreeSet<>(new Comparator<Month>() {
        @Override
        public int compare(Month o1, Month o2) {
            return o2.compareTo(o1);
        }
    });
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

        mSettings = new WTFIMMSettings(getApplicationContext());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        transactionsReference = database.getReference("transactions");
        transactionsReference.keepSynced(true);
        transactionsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Transaction data = dataSnapshot.getValue(Transaction.class);
                Log.d(TAG, data.toString());
                if(months.add(new Month(data.getDate(), getSettings().getStartOfMonth()))) {
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

        categoriesReference = database.getReference("categories");
        categoriesReference.keepSynced(true);
        categoriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Category> newData = new HashMap<String, Category>();
                for(DataSnapshot d : dataSnapshot.getChildren()) {
                    Category data = d.getValue(Category.class);
                    newData.put(d.getKey(), data);
                }
                mCategories = newData;
                for(CategoryChangeListener ccl : categoryChangeListeners)
                    ccl.categoryChange(mCategories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public Query getMonth(Month m) {
        Query q = transactionsReference.orderByChild("timestamp").startAt(m.getStart()).endAt(m.getEnd());
        return q;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Config changed");
        super.onConfigurationChanged(newConfig);
    }

    public WTFIMMSettings getSettings() {
        return mSettings;
    }
}
