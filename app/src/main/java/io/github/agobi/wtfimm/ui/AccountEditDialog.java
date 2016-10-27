package io.github.agobi.wtfimm.ui;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Balance;
import io.github.agobi.wtfimm.util.TimePickerBehaviour;

/**
 * Created by gobi on 10/27/16.
 */

public class AccountEditDialog extends DialogBase {
    private static final String ARG_BALANCE = "BALANCE";
    private AccountSaveListener accountSaveListener;

    public void setAccountSaveListener(AccountSaveListener accountSaveListener) {
        this.accountSaveListener = accountSaveListener;
    }

    public static AccountEditDialog createDialog(Balance balance) {
        AccountEditDialog ret = new AccountEditDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BALANCE, balance);
        ret.setArguments(args);
        return ret;
    }

    interface AccountSaveListener {
        void onAccountSave(Balance balance);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        FireBaseApplication app = (FireBaseApplication) getActivity().getApplication();
        Balance arg = getArguments() != null ?
                (Balance) getArguments().getSerializable(ARG_BALANCE) : null;
        boolean creating = arg == null;
        final Balance balance = creating ? new Balance() : arg;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.account_edit, null);

        final TextView editBalance = (TextView)v.findViewById(R.id.edit_balance);
        TextView dateHint = (TextView)v.findViewById(R.id.edit_date_hint);
        TextView editDate = (TextView) v.findViewById(R.id.edit_date);
        final TextView editTime = (TextView) v.findViewById(R.id.edit_time);
        addHintBehaviour(editDate, dateHint);
        addHintBehaviour(editTime, dateHint);
        final TimePickerBehaviour timePicker = new TimePickerBehaviour(getFragmentManager());

        int title = R.string.createTransaction;
        int ok = R.string.create;

        if(creating) {
            
        } else {
            title = R.string.editTransaction;
            ok = R.string.save;

            timePicker.setDate(new Date(balance.timestamp*1000));
            editBalance.setText(Long.toString(balance.balance));
        }

        timePicker.setDateView(editDate, app.getSettings().getDateFormat());
        timePicker.setTimeView(editTime, app.getSettings().getTimeFormat());

        builder.setView(v)
                .setTitle(title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            balance.balance = Integer.parseInt(editBalance.getText().toString());
                        } catch (NumberFormatException ex ) {
                        }
                        balance.timestamp = timePicker.getDate().getTime()/1000;

                        if(accountSaveListener != null)
                            accountSaveListener.onAccountSave(balance);
                        getDialog().dismiss();
                    }
                })
        ;

        return builder.create();
    }
}
