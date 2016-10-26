package io.github.agobi.wtfimm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by gobi on 10/21/16.
 */

public class WTFIMMSettings {
    private static final String TAG = "Preferences";
    private final SharedPreferences sp;
    private final Locale locale;
    private String defaultSource = "account/cash";
    private String defaultTarget = "attila/food";
    private DateFormat monthFormat;

    private static Locale getLocale() {
        if(Build.VERSION.SDK_INT>=24)
            return Locale.getDefault(Locale.Category.FORMAT);
        else
            return Locale.getDefault();
    }

    public WTFIMMSettings(Context context) {
        locale = getLocale();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(context, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(context, R.xml.pref_notification, false);
        monthFormat = new SimpleDateFormat("MMM yy");
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public String getDefaultTarget() {
        return defaultTarget;
    }

    public DateFormat getDateFormat() {
        return new SimpleDateFormat(sp.getString("sep_format", null), locale);
    }

    public DateFormat getTimeFormat() {
        return new SimpleDateFormat(sp.getString("time_format", null), locale);
    }

    public int getStartOfMonth() {
        return sp.getInt("startofmonth", 1);
    }

    public DateFormat getMonthFormat() {
        return monthFormat;
    }
}
