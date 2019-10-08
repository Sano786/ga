/*
 * USER: jameskluz, mslm
 * DATE: 2/24/17.
 */

package com.gedder.gedderalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String PARAM_ALARM_UUID = "__PARAM_ALARM_UUID__";

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmActivity = new Intent(context.getApplicationContext(), AlarmActivity.class);

        alarmActivity.putExtra(AlarmActivity.PARAM_ALARM_UUID,
                intent.getSerializableExtra(PARAM_ALARM_UUID));

        Bundle results = intent.getBundleExtra("bundle");
        if (results != null) {
            alarmActivity.putExtra("bundle", results);
        }

        alarmActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(alarmActivity);
    }
}
