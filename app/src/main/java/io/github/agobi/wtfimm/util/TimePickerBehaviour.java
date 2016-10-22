package io.github.agobi.wtfimm.util;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gobi on 10/21/16.
 */

public class TimePickerBehaviour {
    private static final String TAG = "TimePickerBehaviour";
    private final FragmentManager fragmentManager;
    private final Calendar calendar;
    private java.text.DateFormat timeViewFormat = null;
    private TextView timeView = null;
    private java.text.DateFormat dateViewFormat = null;
    private TextView dateView = null;

    public TimePickerBehaviour(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        this.calendar = Calendar.getInstance();
    }

    public Date getDate() { return this.calendar.getTime(); }

    public void setDate(Date date) {
        this.calendar.setTime(date);
    }

    public void setTimeView(TextView timeView, java.text.DateFormat timeViewFormat) {
        this.timeViewFormat = timeViewFormat;
        this.timeView = timeView;
        timeView.setText(timeViewFormat.format(calendar.getTime()));

        timeView.setClickable(true);
        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment picker = new TimePickerFragment();
                Bundle args = new Bundle();
                args.putInt("HOUR", calendar.get(Calendar.HOUR_OF_DAY));
                args.putInt("MINUTES", calendar.get(Calendar.MINUTE));
                picker.setArguments(args);
                picker.behaviour = TimePickerBehaviour.this;
                picker.show(fragmentManager, "time");
            }
        });
    }

    public void setDateView(TextView dateView, java.text.DateFormat dateViewFormat) {
        this.dateViewFormat = dateViewFormat;
        this.dateView = dateView;
        dateView.setText(dateViewFormat.format(calendar.getTime()));

        dateView.setClickable(true);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment picker = new DatePickerFragment();
                Bundle args = new Bundle();
                args.putInt("YEAR", calendar.get(Calendar.YEAR));
                args.putInt("MONTH", calendar.get(Calendar.MONTH));
                args.putInt("DAY", calendar.get(Calendar.DAY_OF_MONTH));
                picker.setArguments(args);
                picker.behaviour = TimePickerBehaviour.this;
                picker.show(fragmentManager, "date");
            }
        });
    }

    public static class TimePickerFragment extends AppCompatDialogFragment implements TimePickerDialog.OnTimeSetListener {
        private TimePickerBehaviour behaviour;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, getArguments().getInt("HOUR"),
                    getArguments().getInt("MINUTES"), DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            behaviour.onTimeSet(hourOfDay, minute);
        }
    }

    public static class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
        private TimePickerBehaviour behaviour;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(),
                    this,
                    getArguments().getInt("YEAR"),
                    getArguments().getInt("MONTH"),
                    getArguments().getInt("DAY"));
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            behaviour.onDateSet(year, month, dayOfMonth);
        }
    }

    private void onDateSet(int year, int month, int dayOfMonth) {
        Log.d(TAG, "COMMIT "+year+" "+month+" "+dayOfMonth);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        dateView.setText(dateViewFormat.format(calendar.getTime()));
    }

    private void onTimeSet(int hourOfDay, int minute) {
        Log.d(TAG, "COMMIT "+hourOfDay+" "+minute);

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        timeView.setText(timeViewFormat.format(calendar.getTime()));
    }
}

