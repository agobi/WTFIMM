package io.github.agobi.wtfimm.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by gobi on 10/26/16.
 */
public class IntEditTextPreference extends EditTextPreference {

    public IntEditTextPreference(Context context) {
        super(context);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.valueOf(value));
    }

}