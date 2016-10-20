package io.github.agobi.wtfimm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TREditDialog extends AppCompatDialogFragment {
    public static final String TRANSACTION_KEY = "TRANSACTION_KEY";

    public static TREditDialog createDialog(FireBaseApplication.Transaction t) {
        TREditDialog ret = new TREditDialog();
        Bundle args = new Bundle();
        args.putSerializable(TRANSACTION_KEY, t);
        ret.setArguments(args);
        return ret;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        FireBaseApplication.Transaction tr =
                getArguments() != null ?
                (FireBaseApplication.Transaction) getArguments().getSerializable(TRANSACTION_KEY) :
                        null;

        FireBaseApplication app = (FireBaseApplication) getActivity().getApplication();
        List<String> catIds = new ArrayList<>(), catNames = new ArrayList<>();
        for(Map.Entry<String, FireBaseApplication.Category> x : app.getCategories().entrySet()) {
            catIds.add(x.getKey());
            catNames.add(x.getValue().name);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.tredit, null);

        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner source = (Spinner) v.findViewById(R.id.source);
        final TextView sourceHint = (TextView)v.findViewById(R.id.source_hint);
        source.setAdapter(adapter);
        addHintBehaviour(source, sourceHint);


        Spinner target = (Spinner) v.findViewById(R.id.target);
        target.setAdapter(adapter);
        final TextView targetHint = (TextView)v.findViewById(R.id.target_hint);
        addHintBehaviour(target, targetHint);

        int title = R.string.createTransaction;
        int ok = R.string.create;
        if(tr != null) {
            title = R.string.editTransaction;
            ok = R.string.save;

            ((TextInputLayout)v.findViewById(R.id.edit_note)).getEditText().setText(tr.note);

        }

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
                        TREditDialog.this.getDialog().dismiss();
                    }
                })
                ;

        return builder.create();
    }

    private void addHintBehaviour(Spinner target, final TextView targetHint) {
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


    private static final String TAG = "EditDialog";

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
}
