/*
 * USER: jameskluz, mslm
 * DATE: 3/3/17
 */

package com.gedder.gedderalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gedder.gedderalarm.controller.AlarmClockCursorWrapper;
import com.gedder.gedderalarm.db.AlarmClockDBHelper;
import com.gedder.gedderalarm.model.AlarmClock;
import com.gedder.gedderalarm.util.Log;

public class AlarmRestartReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmRestartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                restartAllAlarms(context);
            }
        }
    }

    private void restartAllAlarms(Context context) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(context);
        AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(db.getAllAlarmClocks());
        if (!cursor.moveToFirst()) {
            Log.i(TAG, "No alarm clock in database upon restart.");
        } else {
            do {
                AlarmClock alarmClock = cursor.getAlarmClock();
                if (alarmClock.isAlarmOn() && alarmClock.getAlarmTimeMillis() > System.currentTimeMillis()) {
                    Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context, alarmClock.getRequestCode(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    GedderAlarmManager.setOptimal(
                            AlarmManager.RTC_WAKEUP, alarmClock.getAlarmTimeMillis(), pendingIntent);
                } else {
                    // We missed the alarm while the phone was off; appropriate alarm variables.
                    alarmClock.setAlarm(AlarmClock.OFF);
                    db.updateAlarmClock(alarmClock);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}
