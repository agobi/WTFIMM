package io.github.agobi.wtfimm;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.MainCategory;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.model.SubCategory;
import io.github.agobi.wtfimm.model.Transaction;

public class FireBaseApplication extends Application {
    private static final String TAG = "FireBaseApplication";
    private DatabaseReference transactionsReference;
    private DatabaseReference categoriesReference;
    private WTFIMMSettings mSettings;

    public static final Category defaultCategory = new MainCategory("uncategorised");

    public DatabaseReference getTransactions() {
        return transactionsReference;
    }

    public interface CategoryChangeListener {
        void categoryChange(Map<String, Category> categories);
    }
    private final List<CategoryChangeListener> categoryChangeListeners = new ArrayList<>();

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

    private List<Category> getDefaultCategories() throws IOException, XmlPullParserException {
        // Create ResourceParser for XML file
        XmlResourceParser xpp = getResources().getXml(R.xml.default_categories);
        // check state
        int eventType = xpp.getEventType();
        List<Category> categories = new ArrayList<>();
        MainCategory current = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            // instead of the following if/else if lines
            // you should custom parse your xml
            if(eventType == XmlPullParser.START_DOCUMENT) {
                System.out.println("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if(name.equals("Categories")) {
                } else if(name.equals("Category")) {
                    current = new MainCategory(xpp.getAttributeValue(null, "name"));
                    categories.add(current);
                } else if(name.equals("SubCategory")) {
                    SubCategory sub = new SubCategory(xpp.getAttributeValue(null, "name"));
                    current.getSubcategories().put(sub.getName(), sub);

                }
            } else if(eventType == XmlPullParser.END_TAG) {
            } else if(eventType == XmlPullParser.TEXT) {
                System.out.println("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }
        // indicate app done reading the resource.
        xpp.close();

        return categories;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = new WTFIMMSettings(getApplicationContext());

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        if(months.add(new Month(new Date(), getSettings().getStartOfMonth()))) {
            for (MonthsChangeListener m : monthsChangeListeners) {
                m.monthsChanged(months.toArray(new Month[0]));
            }
        }

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
                Log.d(TAG, "DS "+dataSnapshot.toString());
                if(!dataSnapshot.exists()) {
                    try {
                        Log.d(TAG, "SAVING");
                        List<Category> categories = getDefaultCategories();
                        for(Category cat : categories) {
                            Log.d(TAG, "Adding +"+cat.getName());
                            Task t = categoriesReference.child(cat.getName()).setValue(cat);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }

                HashMap<String, Category> newData = new HashMap<String, Category>();
                for(DataSnapshot d : dataSnapshot.getChildren()) {
                    MainCategory data = d.getValue(MainCategory.class);
                    newData.put(d.getKey(), data);
                    if(data.getSubcategories() != null) {
                        for (Map.Entry<String, SubCategory> sub : data.getSubcategories().entrySet()) {
                            newData.put(d.getKey() + "/" + sub.getKey(), sub.getValue());
                        }
                    }
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

    public String getSource(Transaction t) {
        String s = t.getSource();
        if(!getCategories().containsKey(s)) s = null;

        if (s == null)
            s = t.getGuessedSource();
        if(!getCategories().containsKey(s)) s = null;

        if (s == null)
            s = defaultCategory.getName();

        return s;
    }

    public String getTarget(Transaction t) {
        String s = t.getTarget();
        if(!getCategories().containsKey(s)) s = null;

        if (s == null)
            s = t.getGuessedTarget();
        if(!getCategories().containsKey(s)) s = null;

        if (s == null)
            s = defaultCategory.getName();

        return s;
    }

}
