package io.github.agobi.wtfimm;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by gobi on 10/21/16.
 */

public class WTFIMMSettings {
    private String defaultSource = "account/cash";
    private String defaultTarget = "attila/food";
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private DateFormat monthFormat;


    public WTFIMMSettings(Context context) {
        dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        monthFormat = new SimpleDateFormat("MMM yy");
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public String getDefaultTarget() {
        return defaultTarget;
    }

    public DateFormat getDateFormat() {
        return (DateFormat)dateFormat.clone();
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }

    public int getStartOfMonth() {
        return 5;
    }

    public DateFormat getMonthFormat() {
        return monthFormat;
    }
}
