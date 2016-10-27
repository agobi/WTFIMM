package io.github.agobi.wtfimm.util;

import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.model.Balance;
import io.github.agobi.wtfimm.model.Transaction;

public abstract class BalanceAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    @SuppressWarnings("unused")
    private static final String TAG = "BalanceAdapter";

    private class AdapterAccountData implements AccountData {
        private final String name;
        private final int id;
        private long balance;
        private DataSnapshot data;
        private Map<String, Long> changeLog = new HashMap<>();
        private Query query;

        private ChildEventListener transactionListener = new ChildEventListener() {
            private long getChange(DataSnapshot dataSnapshot) {
                Transaction transaction = dataSnapshot.getValue(Transaction.class);
                long change = 0;
                if(name.equals(app.getSource(transaction))) {
                    change = -transaction.getAmount();
                } else if(name.equals(app.getTarget(transaction))) {
                    change = transaction.getAmount();
                }
                return change;
            }

            private void addChange(String key, long change) {
                Long last;

                if(change == 0) {
                    last = changeLog.remove(key);
                } else {
                    last = changeLog.put(key, change);
                }

                change -= (last == null ? 0 : last);
                if(change != 0) {
                    balance += change;
                    notifyItemChanged(id);
                }
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addChange(dataSnapshot.getKey(), getChange(dataSnapshot));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                addChange(dataSnapshot.getKey(), getChange(dataSnapshot));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                addChange(dataSnapshot.getKey(), 0);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                BalanceAdapter.this.onCancelled(databaseError);
            }
        };


        AdapterAccountData(String name, int id) {
            this.name = name;
            this.id = id;
            app.getBalance().child(name).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Balance newBalance = dataSnapshot.exists() ? dataSnapshot.getValue(Balance.class) : new Balance();
                    Balance oldBalance = data != null && data.exists() ? data.getValue(Balance.class) : null;

                    if(oldBalance != null && oldBalance.timestamp == newBalance.timestamp) {
                        // Only balance changed...

                        balance += (newBalance.balance - oldBalance.balance);
                        data = dataSnapshot;

                    } else {
                        // Timestamp changed, relcalculating everything

                        if (query != null)
                            query.removeEventListener(transactionListener);

                        balance = newBalance.balance;
                        data = dataSnapshot;
                        changeLog.clear();

                        query = app.getTransactions().orderByChild("timestamp").startAt(newBalance.timestamp);
                        query.addChildEventListener(transactionListener);
                    }

                    notifyItemChanged(AdapterAccountData.this.id);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    BalanceAdapter.this.onCancelled(databaseError);
                }
            });
        }

        @Override
        public long getBalance() {
            return balance;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DataSnapshot getData() {
            return data;
        }


        void cleanup() {
            if(query != null)
                query.removeEventListener(transactionListener);
        }
    }

    protected abstract void onCancelled(DatabaseError databaseError);

    private final FireBaseApplication app;
    protected final List<AdapterAccountData> accountData = new ArrayList<>();

    public BalanceAdapter(FireBaseApplication app) {
        this.app = app;
    }

    public void addAccount(String name) {
        for(AccountData d : accountData) {
            if(name.equals(d.getName()))
                return;
        }

        int pos = accountData.size();
        accountData.add(new AdapterAccountData(name, pos));

        notifyItemInserted(pos);
    }

    @Override
    public int getItemCount() {
        return accountData.size();
    }

    public void cleanup() {
        Iterator<BalanceAdapter<T>.AdapterAccountData> it = accountData.iterator();
        while(it.hasNext())
            it.next().cleanup();

        accountData.clear();
    }
}
