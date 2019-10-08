/*
 * USER: mslm
 * DATE: 3/27/17
 */

package com.gedder.gedderalarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.gedder.gedderalarm.model.AlarmClock;

import java.util.UUID;

import static android.content.Context.ALARM_SERVICE;

/**
 * <p>A reimplementation of {@link AlarmManager} (because we can't extend it), to manage Gedder
 * Alarms.</p>
 *
 * <p>All modified/added functionality is documented.</p>
 *
 * @see AlarmManager
 */

public final class GedderAlarmManager {
    public static final String PARAM_ALARM_CLOCK = "__PARAM_ALARM_CLOCK__";
    public static final String PARAM_UNIQUE_ID   = "__PARAM_UNIQUE_ID__";
    public static final String PARAM_ALARM_TIME  = "__PARAM_ALARM_TIME__";
    public static final String PARAM_UUID        = "__PARAM_UUID__";

    private static final String TAG = GedderAlarmManager.class.getSimpleName();

    private static AlarmManager sAlarmManager =
            (AlarmManager) GedderAlarmApplication.getAppContext().getSystemService(ALARM_SERVICE);

    private GedderAlarmManager() {}

    /**
     *
     * @param alarmData
     */
    public static void setAlarm(Bundle alarmData) {
        UUID uuid = (UUID) alarmData.getSerializable(PARAM_UUID);
        int id    = alarmData.getInt(PARAM_UNIQUE_ID, -1);
        long time = alarmData.getLong(PARAM_ALARM_TIME, -1);

        if (uuid == null || id == -1 || time == -1) {
            throw new IllegalArgumentException("Missing parameter in call to setAlarm(): "
                    + "id = "     + id
                    + ", time = " + time
                    + ", uuid = " + uuid);
        }

        Intent alarmIntent =
                new Intent(GedderAlarmApplication.getAppContext(), AlarmReceiver.class);
        alarmIntent.putExtra(AlarmReceiver.PARAM_ALARM_UUID, uuid);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(GedderAlarmApplication.getAppContext(),
                        id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        setOptimal(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    /**
     *
     * @param alarmData
     */
    public static void cancelAlarm(Bundle alarmData) {
        int id = alarmData.getInt(PARAM_UNIQUE_ID, -1);

        if (id == -1) throw new IllegalArgumentException("No id given in call to cancelAlarm().");

        Intent alarmIntent =
                new Intent(GedderAlarmApplication.getAppContext(), AlarmReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(GedderAlarmApplication.getAppContext(),
                        id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        cancel(pendingIntent);
    }

    /**
     * Sets the Gedder service to work for the alarm that sent the bundle of data.
     * @param gedderData The data the Gedder services require for any alarm clock.
     */
    public static void setGedder(Bundle gedderData) {
        AlarmClock alarmClock = gedderData.getParcelable(PARAM_ALARM_CLOCK);
        int id = gedderData.getInt(PARAM_UNIQUE_ID, -1);

        if (alarmClock == null || id == -1) {
            throw new IllegalArgumentException("Missing parameter in call to setGedder(): "
                    + "alarmClock = " + alarmClock
                    + ", id = "       + id);
        }

        Intent intent = new Intent(GedderAlarmApplication.getAppContext(), GedderReceiver.class);
        intent.putExtra(GedderReceiver.PARAM_UUID,           alarmClock.getUUID());
        intent.putExtra(GedderReceiver.PARAM_ORIGIN_ID,      alarmClock.getOriginId());
        intent.putExtra(GedderReceiver.PARAM_DESTINATION_ID, alarmClock.getDestinationId());
        intent.putExtra(GedderReceiver.PARAM_ARRIVAL_TIME,   alarmClock.getArrivalTimeMillis());
        intent.putExtra(GedderReceiver.PARAM_PREP_TIME,      alarmClock.getPrepTimeMillis());
        intent.putExtra(GedderReceiver.PARAM_ALARM_TIME,     alarmClock.getAlarmTimeMillis());
        intent.putExtra(GedderReceiver.PARAM_ID,             id);
        GedderAlarmApplication.getAppContext().sendBroadcast(intent);
    }

    /**
     * Cancels the Gedder service for the alarm that sent this bundle of data.
     * @param gedderData The data that Gedder services require for any alarm clock.
     */
    public static void cancelGedder(Bundle gedderData) {
        int id = gedderData.getInt(PARAM_UNIQUE_ID, -1);

        if (id == -1) throw new IllegalArgumentException("No id given in call to cancelGedder().");

        Intent alarmIntent =
                new Intent(GedderAlarmApplication.getAppContext(), GedderReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(GedderAlarmApplication.getAppContext(),
                        id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        cancel(pendingIntent);
    }

    /**
     * Checks build version to decide which set functionality to use.
     * @param type              See AlarmManager documentation.
     * @param triggerAtMillis   See AlarmManager documentation.
     * @param operation         See AlarmManager documentation.
     */
    public static void setOptimal(int type, long triggerAtMillis, PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            sAlarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, operation);
        } else if (Build.VERSION.SDK_INT >= 19) {
            sAlarmManager.setExact(type, triggerAtMillis, operation);
        } else {
            sAlarmManager.set(type, triggerAtMillis, operation);
        }
    }

    /**
     * Copy of {@link AlarmManager#cancel(PendingIntent)}.
     * @see AlarmManager#cancel(PendingIntent)
     */
    public static void cancel(PendingIntent operation) {
        sAlarmManager.cancel(operation);
    }

    /**
     * Copy of {@link AlarmManager#cancel(AlarmManager.OnAlarmListener)}.
     * @see AlarmManager#cancel(AlarmManager.OnAlarmListener)
     */
    @TargetApi(24)
    public static void cancel(AlarmManager.OnAlarmListener listener) {
        if (Build.VERSION.SDK_INT >= 24) sAlarmManager.cancel(listener);
    }

    /**
     * Copy of {@link AlarmManager#getNextAlarmClock()}.
     * @see AlarmManager#getNextAlarmClock()
     */
    @TargetApi(21)
    public static AlarmManager.AlarmClockInfo getNextAlarmClock() {
        if (Build.VERSION.SDK_INT >= 21) return sAlarmManager.getNextAlarmClock();
        return null;
    }

    /**
     * Copy of {@link AlarmManager#set(int, long, PendingIntent)}.
     * @see AlarmManager#set(int, long, PendingIntent)
     */
    public static void set(int type, long triggerAtMillis, PendingIntent operation) {
        sAlarmManager.set(type, triggerAtMillis, operation);
    }

    /**
     * Copy of {@link AlarmManager#set(int, long, String, AlarmManager.OnAlarmListener, Handler)}.
     * @see AlarmManager#set(int, long, String, AlarmManager.OnAlarmListener, Handler)
     */
    @TargetApi(24)
    public static void set(int type, long triggerAtMillis, String tag,
                    AlarmManager.OnAlarmListener listener, Handler targetHandler) {
        if (Build.VERSION.SDK_INT >= 24) {
            sAlarmManager.set(type, triggerAtMillis, tag, listener, targetHandler);
        }
    }

    /**
     * Copy of {@link AlarmManager#setAlarmClock(AlarmManager.AlarmClockInfo, PendingIntent)}.
     * @see AlarmManager#setAlarmClock(AlarmManager.AlarmClockInfo, PendingIntent)
     */
    @TargetApi(21)
    public static void setAlarmClock(AlarmManager.AlarmClockInfo info, PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 21) sAlarmManager.setAlarmClock(info, operation);
    }

    /**
     * Copy of {@link AlarmManager#setAndAllowWhileIdle(int, long, PendingIntent)}.
     * @see AlarmManager#setAndAllowWhileIdle(int, long, PendingIntent)
     */
    @TargetApi(23)
    public static void setAndAllowWhileIdle(int type, long triggerAtMillis,
                                            PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            sAlarmManager.setAndAllowWhileIdle(type, triggerAtMillis, operation);
        }
    }

    /**
     * Copy of {@link AlarmManager#setExact(int, long, PendingIntent)}.
     * @see AlarmManager#setExact(int, long, PendingIntent)
     */
    public static void setExact(int type, long triggerAtMillis, PendingIntent operation) {
        sAlarmManager.setExact(type, triggerAtMillis, operation);
    }

    /**
     * Copy of {@link
     * AlarmManager#setExact(int, long, String, AlarmManager.OnAlarmListener, Handler)}.
     * @see AlarmManager#setExact(int, long, String, AlarmManager.OnAlarmListener, Handler)
     */
    @TargetApi(24)
    public static void setExact(int type, long triggerAtMillis, String tag,
                         AlarmManager.OnAlarmListener listener, Handler targetHandler) {
        if (Build.VERSION.SDK_INT >= 24) {
            sAlarmManager.setExact(type, triggerAtMillis, tag, listener, targetHandler);
        }
    }

    /**
     * Copy of {@link AlarmManager#setExactAndAllowWhileIdle(int, long, PendingIntent)}.
     * @see AlarmManager#setExactAndAllowWhileIdle(int, long, PendingIntent)
     */
    @TargetApi(23)
    public static void setExactAndAllowWhileIdle(int type, long triggerAtMillis,
                                                 PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            sAlarmManager.setAndAllowWhileIdle(type, triggerAtMillis, operation);
        }
    }

    /**
     * Copy of {@link AlarmManager#setInexactRepeating(int, long, long, PendingIntent)}.
     * @see AlarmManager#setInexactRepeating(int, long, long, PendingIntent)
     */
    public static void setInexactRepeating(int type, long triggerAtMillis, long intervalMillis,
                                    PendingIntent operation) {
        sAlarmManager.setInexactRepeating(type, triggerAtMillis, intervalMillis, operation);
    }

    /**
     * Copy of {@link AlarmManager#setRepeating(int, long, long, PendingIntent)}.
     * @see AlarmManager#setRepeating(int, long, long, PendingIntent)
     */
    public static void setRepeating(int type, long triggerAtMillis, long intervalMillis,
                             PendingIntent operation) {
        sAlarmManager.setRepeating(type, triggerAtMillis, intervalMillis, operation);
    }

    /**
     * Copy of {@link AlarmManager#setTime(long)}.
     * @see AlarmManager#setTime(long)
     */
    public static void setTime(long millis) {
        sAlarmManager.setTime(millis);
    }

    /**
     * Copy of {@link AlarmManager#setTimeZone(String)}.
     * @see AlarmManager#setTimeZone(String)
     */
    public static void setTimeZone(String timeZone) {
        sAlarmManager.setTimeZone(timeZone);
    }

    /**
     * Copy of {@link AlarmManager#setWindow(int, long, long, PendingIntent)}.
     * @see AlarmManager#setWindow(int, long, long, PendingIntent)
     */
    public static void setWindow(int type, long windowStartMillis, long windowLengthMillis,
                          PendingIntent operation) {
        sAlarmManager.setWindow(type, windowStartMillis, windowLengthMillis, operation);
    }

    /**
     * Copy of {@link AlarmManager#setWindow(int, long, long, String, AlarmManager.OnAlarmListener, Handler)}.
     * @see AlarmManager#setWindow(int, long, long, String, AlarmManager.OnAlarmListener, Handler)}
     */
    @TargetApi(24)
    public static void setWindow(int type, long windowStartMillis, long windowLengthMillis,
                                 String tag, AlarmManager.OnAlarmListener listener,
                                 Handler targetHandler) {
        if (Build.VERSION.SDK_INT >= 24) {
            sAlarmManager.setWindow(
                    type, windowStartMillis, windowLengthMillis, tag, listener, targetHandler);
        }
    }
}
