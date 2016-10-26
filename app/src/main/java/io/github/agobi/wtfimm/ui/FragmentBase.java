package io.github.agobi.wtfimm.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by gobi on 10/24/16.
 */
public abstract class FragmentBase extends Fragment {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getContext() instanceof BaseActivity) {
            onBaseCreated((BaseActivity)getContext());
        }
    }

    protected void onBaseCreated(BaseActivity context) {
        context.setFabVisible(false);
        context.setSpinnerVisible(false);
    }
}
