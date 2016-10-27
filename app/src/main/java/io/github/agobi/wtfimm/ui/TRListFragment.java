package io.github.agobi.wtfimm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Month;
import io.github.agobi.wtfimm.util.TransactionListAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class TRListFragment extends FragmentBase {

    /*
     * The fragment argument representing the section number for this
     * fragment.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "TRList";
    private static final String ARG_MONTH = "month";
    private TransactionListAdapter adapter;
    private OnTransactionSelectedListeners mListener;

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
        final FireBaseApplication app = (FireBaseApplication) getActivity().getApplication();
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.trlist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Month m = (Month)getArguments().getSerializable(ARG_MONTH);
        adapter = new TransactionListAdapter(m, app, new OnClickListenerFactory() {
            @Override
            public View.OnClickListener create(final DataSnapshot t) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onTransactionSelected(t);
                    }
                };
            }

        });
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    interface OnTransactionSelectedListeners {
        void onTransactionSelected(DataSnapshot data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTransactionSelectedListeners) {
            mListener = (OnTransactionSelectedListeners) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapter != null)
            adapter.cleanup();
    }

    @Override
    protected void onBaseCreated(BaseActivity context) {
        context.setFabVisible(true);
        context.setSpinnerVisible(true);
    }
}
