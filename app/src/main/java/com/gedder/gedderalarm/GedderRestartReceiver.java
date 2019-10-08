/*
 * USER: mslm
 * DATE: 4/5/17
 */

package com.gedder.gedderalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gedder.gedderalarm.controller.AlarmClockCursorWrapper;
import com.gedder.gedderalarm.db.AlarmClockDBHelper;
import com.gedder.gedderalarm.model.AlarmClock;
import com.gedder.gedderalarm.util.Log;

/**  */

public class GedderRestartReceiver extends BroadcastReceiver {
    private static final String TAG = GedderRestartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                restartAllGedders(context);
            }
        }
    }

    private void restartAllGedders(Context context) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(context);
        AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(db.getAllAlarmClocks());
        if (!cursor.moveToFirst()) {
            Log.i(TAG, "No alarm clock in database upon restart.");
        } else {
            do {
                AlarmClock alarmClock = cursor.getAlarmClock();

                if (alarmClock.isAlarmOn() && alarmClock.getAlarmTimeMillis() > System.currentTimeMillis()) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(GedderAlarmManager.PARAM_ALARM_CLOCK, alarmClock);
                    bundle.putInt(GedderAlarmManager.PARAM_UNIQUE_ID, alarmClock.getRequestCode());
                    GedderAlarmManager.setGedder(bundle);
                } else {
                    // We missed the alarm while the phone was off.
                    alarmClock.turnGedderOff();
                    db.updateAlarmClock(alarmClock);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}
