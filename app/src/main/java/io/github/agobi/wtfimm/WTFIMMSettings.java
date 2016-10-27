package io.github.agobi.wtfimm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WTFIMMSettings {
    private final SharedPreferences sp;

    private static Locale getLocale() {
        if(Build.VERSION.SDK_INT>=24)
            return Locale.getDefault(Locale.Category.FORMAT);
        else
            return Locale.getDefault();
    }

    WTFIMMSettings(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(context, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(context, R.xml.pref_notification, false);
    }

    public String getDefaultSource() {
        return "account/cash";
    }

    public String getDefaultTarget() {
        return "attila/food";
    }

    public DateFormat getDateFormat() {
        //noinspection ConstantConditions
        return new SimpleDateFormat(sp.getString("sep_format", null), getLocale());
    }

    public DateFormat getTimeFormat() {
        //noinspection ConstantConditions
        return new SimpleDateFormat(sp.getString("time_format", null), getLocale());
    }

    public int getStartOfMonth() {
        return sp.getInt("startofmonth", 1);
    }

    public DateFormat getMonthFormat() {
        return new SimpleDateFormat("MMM yy");
    }
}
