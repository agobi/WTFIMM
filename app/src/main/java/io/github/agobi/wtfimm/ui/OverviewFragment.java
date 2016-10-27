package io.github.agobi.wtfimm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.util.Map;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.MainCategory;
import io.github.agobi.wtfimm.util.AccountData;
import io.github.agobi.wtfimm.util.BalanceAdapter;

public class OverviewFragment extends FragmentBase implements FireBaseApplication.CategoryChangeListener {
    @SuppressWarnings("unused")
    private static final String TAG = "OverviewFragment";

    private OnFragmentInteractionListener mListener;
    private FireBaseApplication app;
    private BalanceAdapter adapter;

    public OverviewFragment() {
        // Required empty public constructor
    }


    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);


        app = (FireBaseApplication) getActivity().getApplication();
        MainCategory accounts = (MainCategory) app.getCategories().get("account");

        RecyclerView mRecyclerView = ((RecyclerView) view.findViewById(R.id.balance));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        if(accounts != null) {
            for(Category account : accounts.getSubcategories().values()) {
                adapter.addAccount("account/"+account.getName());
            }
        }

        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(adapter == null) {
            app = (FireBaseApplication) getActivity().getApplication();
            adapter = new OverviewBalanceAdapter();
        }
        app.addCategoryChangeListener(this);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
    public void categoryChange(Map<String, Category> categories) {
        MainCategory account = (MainCategory) categories.get("account");
        if(account != null)
            for(String cat : account.getSubcategories().keySet())
                adapter.addAccount("account"+"/"+cat);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }


    private class OverviewBalanceAdapter extends BalanceAdapter<OverviewBalanceAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView name;
            private final TextView amount;

            ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.balance_name);
                amount = (TextView) itemView.findViewById(R.id.balance_amount);
            }
        }

        OverviewBalanceAdapter() {
            super(OverviewFragment.this.app);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.balance, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AccountData accountData = this.accountData.get(position);
            holder.name.setText(accountData.getName());
            holder.amount.setText(Long.toString(accountData.getBalance()));
        }

        @Override
        protected void onCancelled(DatabaseError databaseError) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapter != null)
            adapter.cleanup();
    }
}
