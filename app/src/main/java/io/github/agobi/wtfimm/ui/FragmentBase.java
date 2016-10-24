package io.github.agobi.wtfimm.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by gobi on 10/24/16.
 */
public abstract class FragmentBase extends Fragment {

    private static final String TAG = "FRAGMENTBASE";

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "CREATED CTX");
        if(getContext() instanceof BaseActivity) {
            onBaseCreated((BaseActivity)getContext());
        }
    }

    protected void onBaseCreated(BaseActivity context) {
        context.setFabVisible(false);
        context.setSpinnerVisible(false);
    }
}
