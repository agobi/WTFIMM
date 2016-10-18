package io.github.agobi.wtfimm;

import android.app.Application;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by gobi on 10/11/16.
 */
public class FireBaseApplication extends Application {
    private static final String TAG = "FireBaseApplication";
    private static int startOfMonth = 5;
    private DatabaseReference trRef;
    private DatabaseReference catRef;

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

    public static final Category defaultCategory = new Category("???");
    static class Category {
        public Category() {}
        public Category(String name) { this.name = name; }
        public String name;
    }

    interface CategoryChangeListener {
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


    static String getDay(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp*1000);
        return DateFormat.format("MM-dd", cal).toString();
    }

    static class Month implements Comparable<Month>, Serializable {
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
            start = mc.getTimeInMillis() / 1000;

            mc.add(Calendar.MONTH, 1);
            end = mc.getTimeInMillis() / 1000;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        trRef = database.getReference("transactions");
        trRef.keepSynced(true);
        trRef.addChildEventListener(new ChildEventListener() {
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

        catRef = database.getReference("categories");
        catRef.keepSynced(true);
        catRef.addValueEventListener(new ValueEventListener() {
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
        Query q = trRef.orderByChild("timestamp").startAt(m.start).endAt(m.end);
        return q;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Config changed");
        super.onConfigurationChanged(newConfig);
    }
}
