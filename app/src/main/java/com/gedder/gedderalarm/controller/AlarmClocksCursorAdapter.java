/*
 * USER: mslm
 * DATE: 3/15/17
 */

package com.gedder.gedderalarm.controller;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gedder.gedderalarm.R;
import com.gedder.gedderalarm.db.AlarmClockDBSchema.AlarmClockTable;

import java.util.Locale;

/**
 * <p>Provides a custom ArrayAdapter for the AlarmClock class.</p>
 *
 * <p>Can be used, for example, to populate ListViews with alarm clocks.</p>
 */

public class AlarmClocksCursorAdapter extends CursorAdapter {
    private static final String TAG = AlarmClocksCursorAdapter.class.getSimpleName();

    public AlarmClocksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_alarm_clock, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Set the UUID of the alarm as a tag to this view.
        view.setTag(cursor.getString(cursor.getColumnIndexOrThrow(
                AlarmClockTable.Columns.UUID)));

        // Get data needed for this view.
        int hour         = cursor.getInt(cursor.getColumnIndexOrThrow(
                AlarmClockTable.Columns.ALARM_HOUR));
        int minute       = cursor.getInt(cursor.getColumnIndexOrThrow(
                AlarmClockTable.Columns.ALARM_MINUTE));
        boolean alarmOn  = cursor.getInt(cursor.getColumnIndexOrThrow(
                AlarmClockTable.Columns.ALARM_SET)) > 0;
        boolean gedderOn = cursor.getInt(cursor.getColumnIndexOrThrow(
                AlarmClockTable.Columns.GEDDER_SET)) > 0;

        // Format our display.
        String period = hour >= 12? "PM":"AM";
        hour          = hour > 12? (hour % 12) : (hour == 0? 12 : hour);
        String time   = String.format(Locale.getDefault(), "%d:%02d %s", hour, minute, period);

        // Populate our views with that formatted data.
        ((TextView) view.findViewById(R.id.itemAlarmClock_WakeupTime)).setText(time);

        if (alarmOn) {
            ((ToggleButton) view.findViewById(R.id.itemAlarmClock_alarmClockToggleBtn))
                    .setChecked(true);
        } else {
            ((ToggleButton) view.findViewById(R.id.itemAlarmClock_alarmClockToggleBtn))
                    .setChecked(false);
        }

        if (gedderOn) {
            ((ToggleButton) view.findViewById(R.id.itemAlarmClock_GedderAlarmToggleBtn))
                    .setChecked(true);
        } else {
            ((ToggleButton) view.findViewById(R.id.itemAlarmClock_GedderAlarmToggleBtn))
                    .setChecked(false);
        }
    }
}
