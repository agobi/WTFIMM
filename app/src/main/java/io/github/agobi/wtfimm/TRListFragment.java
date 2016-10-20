package io.github.agobi.wtfimm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class TRListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_MONTH = "month";
    private RecyclerView mRecyclerView;
    private static final String TAG = "TRList";

    private static final int SEPARATOR = 1, TRANSACTION = 0;

    public TRListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TRListFragment newInstance(FireBaseApplication.Month m) {
        TRListFragment fragment = new TRListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MONTH, m);
        fragment.setArguments(args);
        return fragment;
    }

    private static interface OnClickListenerFactory {
        public View.OnClickListener create(FireBaseApplication.Transaction t);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_trlist, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.trlist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FireBaseApplication.Month m = (FireBaseApplication.Month)getArguments().getSerializable(ARG_MONTH);
        FireBaseApplication app = (FireBaseApplication)getActivity().getApplication();
        mRecyclerView.setAdapter(new MyAdapter(m, app, new OnClickListenerFactory() {
            @Override
            public View.OnClickListener create(final FireBaseApplication.Transaction t) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        transactionClicked(v, t);
                    }
                };
            }
        }));

        return rootView;
    }

    private void transactionClicked(View v, FireBaseApplication.Transaction t) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        TREditDialog tredit = TREditDialog.createDialog(t);
        tredit.show(fragmentManager, "dialog");
    }


    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolderBase> implements ValueEventListener, FireBaseApplication.CategoryChangeListener {
        private final OnClickListenerFactory onClickListenerFactory;
        private Map<String, FireBaseApplication.Category> categories;
        private ArrayList<Object> mData = new ArrayList<>();
        private static String LOG_TAG = "MyRecyclerViewAdapter";

        public MyAdapter(FireBaseApplication.Month m, FireBaseApplication app, OnClickListenerFactory onClickListenerFactory) {
            app.getMonth(m).addValueEventListener(this);
            app.addCategoryChangeListener(this);
            this.onClickListenerFactory = onClickListenerFactory;
        }

        public abstract class ViewHolderBase extends RecyclerView.ViewHolder {
            public ViewHolderBase(View itemView) {
                super(itemView);
            }

            public abstract void setData(Object data, OnClickListenerFactory factory);
        }

        public class SeparatorViewHolder extends ViewHolderBase {
            private final TextView sepLabel;

            public SeparatorViewHolder(View itemView) {
                super(itemView);
                sepLabel = (TextView) itemView.findViewById(R.id.sepLabel);
            }

            @Override
            public void setData(Object data, OnClickListenerFactory factory) {
                sepLabel.setText((String)data);
            }
        }

        public class TransactionViewHolder extends ViewHolderBase {
            private final TextView trTime;
            private final TextView trSource;
            private final TextView trDestination;
            private final TextView trAmount;
            private final TextView trNote;

            public TransactionViewHolder(View itemView) {
                super(itemView);
                trTime = (TextView) itemView.findViewById(R.id.trTime);
                trSource = (TextView) itemView.findViewById(R.id.trSource);
                trDestination = (TextView) itemView.findViewById(R.id.trDestination);
                trAmount = (TextView) itemView.findViewById(R.id.trAmount);
                trNote = (TextView) itemView.findViewById(R.id.trNote);
            }

            public FireBaseApplication.Category getCategory(String key) {
                FireBaseApplication.Category cat = categories.get(key);
                return cat != null ? cat : FireBaseApplication.defaultCategory;
            }

            @Override
            public void setData(Object data, OnClickListenerFactory factory) {
                FireBaseApplication.Transaction tr = (FireBaseApplication.Transaction)data;
                trTime.setText(DateFormat.format("HH:mm", tr.timestamp*1000));
                trSource.setText(getCategory(tr.source).name);
                trDestination.setText(categories.get(tr.target).name);
                trAmount.setText(Integer.toString(tr.amount)+" Ft");
                trNote.setText(tr.note);
                itemView.setOnClickListener(factory.create(tr));
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
            if(mData.get(position) instanceof FireBaseApplication.Transaction)
                return TRANSACTION;
            else if(mData.get(position) instanceof String)
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

            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                FireBaseApplication.Transaction t = ds.getValue(FireBaseApplication.Transaction.class);
                String day = FireBaseApplication.getDay(t.timestamp);
                if(!day.equals(lastDay)) {
                    if(lastDay != null)
                        mData.add(0, lastDay);
                    lastDay = day;
                }
                mData.add(0, t);
            }
            if(lastDay != null)
                mData.add(0, lastDay);

            notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void categoryChange(Map<String, FireBaseApplication.Category> categories) {
            this.categories = categories;
            notifyDataSetChanged();
        }
    }
}
