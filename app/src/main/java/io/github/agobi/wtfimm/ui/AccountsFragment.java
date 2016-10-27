package io.github.agobi.wtfimm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Balance;
import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.MainCategory;
import io.github.agobi.wtfimm.util.AccountData;
import io.github.agobi.wtfimm.util.BalanceAdapter;

public class AccountsFragment extends Fragment implements FireBaseApplication.CategoryChangeListener {
    private AccountAdapter adapter;
    private FireBaseApplication application;
    private OnAccountsFragmentEventListener listener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AccountsFragment() {
    }

    public static AccountsFragment newInstance() {
        return new AccountsFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);

        }
        return view;
    }

    @Override
    public void categoryChange(Map<String, Category> categories) {
        MainCategory account = (MainCategory) categories.get("account");
        if(account != null)
            for(String cat : account.getSubcategories().keySet())
                adapter.addAccount("account"+"/"+cat);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(adapter == null) {
            application = (FireBaseApplication) getActivity().getApplication();
            adapter = new AccountAdapter();
        }
        application.addCategoryChangeListener(this);

        if (context instanceof OnAccountsFragmentEventListener) {
            listener = (OnAccountsFragmentEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountsFragmentEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        application.removeCategoryChangeListener(this);
    }

    public class AccountAdapter extends BalanceAdapter<AccountAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView name;
            private final TextView balance;
            private final TextView initial;
            private final TextView timestamp;
            AccountData accountData;

            ViewHolder(View itemView) {
                super(itemView);
                this.name = (TextView)itemView.findViewById(R.id.account_name);
                this.balance = (TextView)itemView.findViewById(R.id.account_balance);
                this.initial = (TextView)itemView.findViewById(R.id.account_initial);
                this.timestamp = (TextView)itemView.findViewById(R.id.account_timestamp);
                itemView.findViewById(R.id.account_card).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listener!=null)
                            listener.onAccountSelected(accountData);
                    }
                });
            }
        }

        AccountAdapter() {
            super(application);
        }

        @Override
        protected void onCancelled(DatabaseError databaseError) {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_accounts_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AccountAdapter.ViewHolder holder, int position) {
            AccountData accountData = this.accountData.get(position);
            holder.accountData = accountData;
            holder.name.setText(accountData.getName());
            holder.balance.setText(Long.toString(accountData.getBalance()));
            if(accountData.getData() != null && accountData.getData().exists()) {
                Balance balance = accountData.getData().getValue(Balance.class);
                holder.initial.setText(Long.toString(balance.balance));
                Date date = new Date(balance.timestamp * 1000);
                holder.timestamp.setText(SimpleDateFormat.getDateTimeInstance().format(date));
            } else {
                holder.initial.setText("");
                holder.timestamp.setText("");
            }
        }
    }

    interface OnAccountsFragmentEventListener {
        void onAccountSelected(AccountData accountData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapter != null)
            adapter.cleanup();
    }
}

