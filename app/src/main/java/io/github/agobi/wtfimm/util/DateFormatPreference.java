package io.github.agobi.wtfimm.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.preference.ListPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by gobi on 10/26/16.
 */
public class DateFormatPreference extends ListPreference {

    private void addDefaults(Context context, AttributeSet attrs) {
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();
        Date now = new Date();

        String type = attrs != null ? attrs.getAttributeValue(null, "dateTimeFormat") : null;
        if("date".equals(type)) {
            addFormat(entries, values, DateFormat.getMediumDateFormat(context), now);
            addFormat(entries, values, DateFormat.getDateFormat(context), now);
            addFormat(entries, values, DateFormat.getLongDateFormat(context), now);
            addFormat(entries, values, java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL), now);
        } else if("time".equals(type)) {
            addFormat(entries, values, new SimpleDateFormat("H:mm", Locale.getDefault()), now);
            addFormat(entries, values, new SimpleDateFormat("h:mm a", Locale.getDefault()), now);
            addFormat(entries, values, new SimpleDateFormat("H:mm z", Locale.getDefault()), now);
            addFormat(entries, values, new SimpleDateFormat("h:mm a z", Locale.getDefault()), now);
        }
        
        setEntries(entries.toArray(new String[] {}));
        setEntryValues(values.toArray(new String[] {}));
        setDefaultValue(values.get(0));
    }

    private static void addFormat(List<String> entries, List<String> values, java.text.DateFormat df, Date now) {
        SimpleDateFormat sdf = (SimpleDateFormat)df;
        entries.add(sdf.format(now));
        values.add(sdf.toLocalizedPattern());
    }

    @TargetApi(21)
    public DateFormatPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        addDefaults(context, attrs);
    }

    @TargetApi(21)
    public DateFormatPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addDefaults(context, attrs);
    }

    public DateFormatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        addDefaults(context, attrs);
    }

    public DateFormatPreference(Context context) {
        super(context);
        addDefaults(context, null);
    }
}
