package io.github.agobi.wtfimm.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.model.Transaction;
import io.github.agobi.wtfimm.ui.TRListFragment;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolderBase> implements ValueEventListener, FireBaseApplication.CategoryChangeListener {
    private static final int SEPARATOR = 1, TRANSACTION = 0;
    private final TRListFragment.OnClickListenerFactory onClickListenerFactory;
    private final Query month;
    private ArrayList<Object> mData = new ArrayList<>();
    private FireBaseApplication app;
    private DateFormat dayFormatter;
    private DateFormat timeFormat;

    public TransactionListAdapter(Month m, FireBaseApplication app, TRListFragment.OnClickListenerFactory onClickListenerFactory) {
        month = app.getMonth(m);
        month.addValueEventListener(this);
        app.addCategoryChangeListener(this);
        this.app = app;
        this.onClickListenerFactory = onClickListenerFactory;
        dayFormatter = app.getSettings().getDateFormat();
        timeFormat = app.getSettings().getTimeFormat();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void cleanup() {
        month.removeEventListener(this);
        app.removeCategoryChangeListener(this);
    }

    abstract class ViewHolderBase extends RecyclerView.ViewHolder {
        ViewHolderBase(View itemView) {
            super(itemView);
        }

        public abstract void setData(Object data, TRListFragment.OnClickListenerFactory factory);
    }

    private class SeparatorViewHolder extends ViewHolderBase {
        private final TextView sepLabel;

        SeparatorViewHolder(View itemView) {
            super(itemView);
            sepLabel = (TextView) itemView.findViewById(R.id.sepLabel);
        }

        @Override
        public void setData(Object data, TRListFragment.OnClickListenerFactory factory) {
            sepLabel.setText((String) data);
        }
    }

    private class TransactionViewHolder extends ViewHolderBase {
        private final TextView trTime;
        private final TextView trSource;
        private final TextView trDestination;
        private final TextView trAmount;
        private final TextView trNote;

        TransactionViewHolder(View itemView) {
            super(itemView);
            trTime = (TextView) itemView.findViewById(R.id.trTime);
            trSource = (TextView) itemView.findViewById(R.id.trSource);
            trDestination = (TextView) itemView.findViewById(R.id.trDestination);
            trAmount = (TextView) itemView.findViewById(R.id.trAmount);
            trNote = (TextView) itemView.findViewById(R.id.trNote);
        }

        @Override
        public void setData(Object data, TRListFragment.OnClickListenerFactory factory) {
            DataSnapshot ds = (DataSnapshot) data;
            Transaction tr = ds.getValue(Transaction.class);
            trTime.setText(timeFormat.format(tr.getDate()));
            trSource.setText(app.getSource(tr));
            trDestination.setText(app.getTarget(tr));
            trAmount.setText(Integer.toString(tr.getAmount()) + " Ft");
            trNote.setText(tr.getNote());
            itemView.setOnClickListener(factory.create(ds));
        }
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderBase viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TRANSACTION:
                View v1 = layoutInflater.inflate(R.layout.transaction, parent, false);
                viewHolder = new TransactionViewHolder(v1);
                break;

            case SEPARATOR:
                View v2 = layoutInflater.inflate(R.layout.trlist_separator, parent, false);
                viewHolder = new SeparatorViewHolder(v2);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        final Object data = mData.get(position);
        holder.setData(data, onClickListenerFactory);
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof DataSnapshot)
            return TRANSACTION;
        else if (mData.get(position) instanceof String)
            return SEPARATOR;
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mData.clear();

        String lastDay = null;

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            String day = dayFormatter.format(new Date(ds.child("timestamp").getValue(Long.class)*1000));
            if (!day.equals(lastDay)) {
                if (lastDay != null)
                    mData.add(0, lastDay);
                lastDay = day;
            }
            mData.add(0, ds);
        }
        if (lastDay != null)
            mData.add(0, lastDay);

        notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void categoryChange(Map<String, Category> categories) {
        notifyDataSetChanged();
    }

}
