package io.github.agobi.wtfimm.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gobi on 10/21/16.
 */
public class Month implements Comparable<Month>, Serializable {
    private final long start, end;

    public Month(Date date, int startOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -startOfMonth + 1);
        Calendar mc = Calendar.getInstance();
        mc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), startOfMonth, 0, 0, 0);
        mc.set(Calendar.MILLISECOND, 0);
        start = mc.getTimeInMillis() / 1000;

        mc.add(Calendar.MONTH, 1);
        end = mc.getTimeInMillis() / 1000;
    }

    @Override
    public int compareTo(Month o) {
        return (int) (o.start - start);
    }

    @Override
    public int hashCode() {
        return (int) (start ^ (start >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Month) {
            Month other = (Month) obj;
            return start == other.start;
        }
        return false;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
