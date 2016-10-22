package io.github.agobi.wtfimm.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.agobi.wtfimm.FireBaseApplication;
import io.github.agobi.wtfimm.R;
import io.github.agobi.wtfimm.model.Category;
import io.github.agobi.wtfimm.model.Transaction;
import io.github.agobi.wtfimm.util.TimePickerBehaviour;

public class TREditDialog extends AppCompatDialogFragment {
    private static final String TAG = "EditDialog";
    private static final String TRANSACTION_KEY = "TRANSACTION_KEY";
    private final List<String> categoryIds = new ArrayList<>(), catNames = new ArrayList<>();
    private TransactionSaveListener transactionSaveListener;

    public static TREditDialog createDialog(Transaction t) {
        TREditDialog ret = new TREditDialog();
        Bundle args = new Bundle();
        args.putSerializable(TRANSACTION_KEY, t);
        ret.setArguments(args);
        return ret;
    }

    private int getCatIndex(String name) {
        for(int i = 0; i< categoryIds.size(); ++i) {
            if(categoryIds.get(i).equals(name))
                return i;
        }
        return -1;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Transaction tr2 = getArguments() != null ?
                (Transaction) getArguments().getSerializable(TRANSACTION_KEY) : new Transaction();
        final Transaction tr = tr2 == null ? new Transaction() : tr2;

        FireBaseApplication app = (FireBaseApplication) getActivity().getApplication();
        for(Map.Entry<String, Category> x : app.getCategories().entrySet()) {
            categoryIds.add(x.getKey());
            catNames.add(x.getValue().getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.tredit, null);

        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner source = (Spinner) v.findViewById(R.id.source);
        final TextView sourceHint = (TextView)v.findViewById(R.id.source_hint);
        source.setAdapter(adapter);
        addHintBehaviour(source, sourceHint);

        final Spinner target = (Spinner) v.findViewById(R.id.target);
        target.setAdapter(adapter);
        final TextView targetHint = (TextView)v.findViewById(R.id.target_hint);
        addHintBehaviour(target, targetHint);

        TextView dateHint = (TextView)v.findViewById(R.id.edit_date_hint);
        TextView editDate = (TextView) v.findViewById(R.id.edit_date);
        final TextView editTime = (TextView) v.findViewById(R.id.edit_time);
        addHintBehaviour(editDate, dateHint);
        addHintBehaviour(editTime, dateHint);

        final TimePickerBehaviour timePicker = new TimePickerBehaviour(getFragmentManager());
        final EditText editAmount = ((TextInputLayout) v.findViewById(R.id.edit_amount)).getEditText();
        final EditText editNote = ((TextInputLayout) v.findViewById(R.id.edit_note)).getEditText();

        int title = R.string.createTransaction;
        int ok = R.string.create;
        if(tr2 != null) {
            title = R.string.editTransaction;
            ok = R.string.save;

            timePicker.setDate(tr.getDate());
            editAmount.setText(String.valueOf(tr.getAmount()));
            editNote.setText(tr.getNote());
            source.setSelection(getCatIndex(tr.getSource()));
            target.setSelection(getCatIndex(tr.getTarget()));
        } else {
            source.setSelection(getCatIndex(app.getSettings().getDefaultSource()));
            target.setSelection(getCatIndex(app.getSettings().getDefaultTarget()));
            editAmount.setText("0");
        }

        timePicker.setDateView(editDate, app.getSettings().getDateFormat());
        timePicker.setTimeView(editTime, app.getSettings().getTimeFormat());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                .setTitle(title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TREditDialog.this.getDialog().cancel();
                    }
                })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tr.setAmount(Integer.parseInt(editAmount.getText().toString()));
                        tr.setDate(timePicker.getDate());
                        tr.setSource(categoryIds.get(source.getSelectedItemPosition()));
                        tr.setTarget(categoryIds.get(target.getSelectedItemPosition()));
                        tr.setNote(editNote.getText().toString());

                        if(transactionSaveListener != null)
                            transactionSaveListener.onTREditSave(tr);
                        TREditDialog.this.getDialog().dismiss();
                    }
                })
                ;

        return builder.create();
    }

    private void addHintBehaviour(View target, final TextView targetHint) {
        target.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            int resId = hasFocus? R.style.InputLabel_Hint:R.style.InputLabel;
            if (Build.VERSION.SDK_INT < 23) {
                targetHint.setTextAppearance(getContext(), resId);
            } else {
                targetHint.setTextAppearance(resId);
            }
            }
        });
    }



    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d(TAG, "FRAGMENT"+childFragment.toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "FRAGMENT ctx"+context.toString());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "Cancel");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroy");
    }

    interface TransactionSaveListener {
        public void onTREditSave(Transaction transaction);
    }

    public void setTransactionSaveListener(TransactionSaveListener transactionSaveListener) {
        this.transactionSaveListener = transactionSaveListener;

    }
}
