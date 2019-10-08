/*
 * USER: mslm
 * DATE: 4/5/2017
 */

package com.gedder.gedderalarm.controller;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.gedder.gedderalarm.db.AlarmClockDBSchema.AlarmClockTable;
import com.gedder.gedderalarm.google.TransitMode;
import com.gedder.gedderalarm.google.TravelMode;
import com.gedder.gedderalarm.model.AlarmClock;
import com.gedder.gedderalarm.util.DaysOfWeek;

import java.util.UUID;

/** A cursor wrapper to easily retrieve alarm clocks from a cursor. */

public class AlarmClockCursorWrapper extends CursorWrapper {
    private static final String TAG = AlarmClockCursorWrapper.class.getSimpleName();

    public AlarmClockCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public AlarmClock getAlarmClock() {
        AlarmClock alarmClock = new AlarmClock(
                UUID.fromString(getString(getColumnIndexOrThrow(AlarmClockTable.Columns.UUID))),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.REQUEST_CODE)),
                getString(getColumnIndexOrThrow(AlarmClockTable.Columns.ORIGIN_ID)),
                getString(getColumnIndexOrThrow(AlarmClockTable.Columns.ORIGIN_ADDRESS)),
                getString(getColumnIndexOrThrow(AlarmClockTable.Columns.DESTINATION_ID)),
                getString(getColumnIndexOrThrow(AlarmClockTable.Columns.DESTINATION_ADDRESS)),
                TravelMode.valueOf(getString(getColumnIndexOrThrow(AlarmClockTable.Columns.TRAVEL_MODE))),
                TransitMode.valueOf(getString(getColumnIndexOrThrow(AlarmClockTable.Columns.TRANSIT_MODE))),
                new DaysOfWeek(getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.REPEAT_DAYS))),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ALARM_DAY)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ALARM_HOUR)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ALARM_MINUTE)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ARRIVAL_DAY)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ARRIVAL_HOUR)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ARRIVAL_MINUTE)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.PREP_HOUR)),
                getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.PREP_MINUTE))
        );
        if (getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.ALARM_SET)) > 0) {
            alarmClock.setAlarm(AlarmClock.ON);
        }
        if (getInt(getColumnIndexOrThrow(AlarmClockTable.Columns.GEDDER_SET)) > 0) {
            alarmClock.setGedder(AlarmClock.ON);
        }
        return alarmClock;
    }
}
