package io.github.agobi.wtfimm.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.model.Transaction;
import io.github.agobi.wtfimm.util.TransactionListAdapter;

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

    public TRListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TRListFragment newInstance(Month m) {
        TRListFragment fragment = new TRListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MONTH, m);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnClickListenerFactory {
        View.OnClickListener create(DataSnapshot t);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_trlist, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.trlist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Month m = (Month)getArguments().getSerializable(ARG_MONTH);
        FireBaseApplication app = (FireBaseApplication)getActivity().getApplication();
        mRecyclerView.setAdapter(new TransactionListAdapter(m, app, new OnClickListenerFactory() {
            @Override
            public View.OnClickListener create(final DataSnapshot t) {
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

    private void transactionClicked(View v, final DataSnapshot data) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        TREditDialog tredit = TREditDialog.createDialog(data.getValue(Transaction.class));

        tredit.setTransactionSaveListener(new TREditDialog.TransactionSaveListener() {
            @Override
            public void onTREditSave(Transaction transaction) {
                data.getRef().setValue(transaction);
            }
        });

        tredit.show(fragmentManager, "dialog");
    }
}
