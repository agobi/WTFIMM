package io.github.agobi.wtfimm.ui;

import android.os.Build;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.TextView;

import io.github.agobi.wtfimm.R;

/**
 * Created by gobi on 10/27/16.
 */
public class DialogBase extends AppCompatDialogFragment {
    protected void addHintBehaviour(View target, final TextView targetHint) {
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
}
