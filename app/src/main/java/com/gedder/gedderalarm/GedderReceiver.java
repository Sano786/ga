/*
 * USER: mslm
 * DATE: 3/31/2017
 */

package com.gedder.gedderalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gedder.gedderalarm.model.GedderEngine;
import com.gedder.gedderalarm.util.TimeUtilities;

import java.util.ArrayList;
import java.util.UUID;


/**
 * <p>Serves as the entry point for the Gedder algorithm.</p>
 *
 * <p>After gathering data from whatever started up this activity, it starts up the
 * {@link GedderEngine}. The {@link GedderEngine} sends back some of the relevant data after flowing
 * through its pipeline. It then analyzes the response.</p>
 *
 * <p>From going through the analysis, it determines what action to take. It may either:</p>
 *
 * <ul>
 *     <li>Reschedule itself to start the {@link GedderEngine} up for some time in the future
 *     depending on how much time we have until the alarm.</li>
 *     <li>Trigger the alarm.</li>
 * </ul><br>
 *
 * <u><strong>Case 1</strong></u>
 *
 * <p>In the first case, the algorithm sees no reason for concern, and just carries on rescheduling
 * itself for future analysis at some rate it determines based off of how much time there currently
 * is until the alarm is supposed to ring.</p>
 *
 * <u><strong>Case 2</strong></u>
 *
 * <p>In the second case, the algorithm makes the decision to wake the user up right away: it caught
 * a delay and the current time is past the optimal wake up time, so there's no time to lose.</p>
 *
 * <p>Each time the algorithm sets itself up for a future flow through the pipeline, it checks how
 * close it is to the currently planned alarm time. The closer it gets, the more often it queries
 * for updates on traffic and possible delays. This carries on until the algorithm notices that
 * now the user must wake up as soon as possible, otherwise the delay can be detrimental to reaching
 * the destination at the planned arrival time.</p>
 *
 * <p>To sum, the algorithm either reschedules itself for a rate of analysis dependent on how
 * much time is left until the alarm, or triggers the alarm because there's no time to lose.</p>
 */

public class GedderReceiver extends BroadcastReceiver {
    public static final String PARAM_UUID           = "__PARAM_UUID__";
    public static final String PARAM_ORIGIN_ID      = "__PARAM_ORIGIN__";
    public static final String PARAM_DESTINATION_ID = "__PARAM_DESTINATION__";
    public static final String PARAM_ARRIVAL_TIME   = "__PARAM_ARRIVAL_TIME__";
    public static final String PARAM_PREP_TIME      = "__PARAM_PREP_TIME__";
    public static final String PARAM_ALARM_TIME     = "__PARAM_ALARM_BOUND_TIME__";
    public static final String PARAM_ID             = "__PARAM_ID__";
    /*TODO Figure out how to not need padding*/
    public static final int TRAVEL_TIME_PADDING     = 15;

    private static final String TAG = GedderReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        UUID   uuid        = (UUID) intent.getSerializableExtra(PARAM_UUID);
        String origin      = intent.getStringExtra(PARAM_ORIGIN_ID);
        String dest        = intent.getStringExtra(PARAM_DESTINATION_ID);
        long   arrivalTime = intent.getLongExtra  (PARAM_ARRIVAL_TIME, -1);
        long   prepTime    = intent.getLongExtra  (PARAM_PREP_TIME, -1);
        long   alarmTime   = intent.getLongExtra  (PARAM_ALARM_TIME, -1);
        int    id          = intent.getIntExtra   (PARAM_ID, -1);

        if (origin == null || dest == null || origin.equals("") || dest.equals("")
                || arrivalTime == -1 || prepTime == -1 || alarmTime == -1 || id == -1) {
            throw new IllegalArgumentException("Missing parameter in GedderReceiver: "
                    + "origin = "        + origin
                    + ", dest = "        + dest
                    + ", arrivalTime = " + arrivalTime
                    + ", prepTime = "    + prepTime
                    + ", alarmTime = "   + alarmTime
                    + ", id = "          + id);
        }

        Bundle results = GedderEngine.start(origin, dest, arrivalTime);
        long duration = TimeUtilities.secondsToMillis(
                results.getInt(GedderEngine.RESULT_DURATION, -1));
        long durationInTraffic = TimeUtilities.secondsToMillis(
                results.getInt(GedderEngine.RESULT_DURATION_IN_TRAFFIC, -1));
        @SuppressWarnings("unchecked")
        ArrayList<String> warnings = (ArrayList<String>)
                results.getSerializable(GedderEngine.RESULT_WARNINGS);

        /*TODO Figure out how to not need padding*/
        duration += TimeUtilities.minutesToMillis(TRAVEL_TIME_PADDING);
        /* *********/

        long optimalWakeUpTime = arrivalTime - duration - prepTime;
        long timeUntilAlarm    = optimalWakeUpTime - System.currentTimeMillis();

        Intent next = new Intent(GedderAlarmApplication.getAppContext(), GedderReceiver.class);
        long nextTime;

        if (System.currentTimeMillis() > optimalWakeUpTime
                || System.currentTimeMillis() > alarmTime) {
            // Wake up
            next.setClass(GedderAlarmApplication.getAppContext(), AlarmReceiver.class);
            next.putExtra(AlarmReceiver.PARAM_ALARM_UUID, uuid);
            next.putExtra("bundle",                       results);
            nextTime = System.currentTimeMillis();
        } else {
            next.putExtra(PARAM_UUID,           uuid);
            next.putExtra(PARAM_ORIGIN_ID,      origin);
            next.putExtra(PARAM_DESTINATION_ID, dest);
            next.putExtra(PARAM_ARRIVAL_TIME,   arrivalTime);
            next.putExtra(PARAM_PREP_TIME,      prepTime);
            next.putExtra(PARAM_ALARM_TIME,     alarmTime);
            next.putExtra(PARAM_ID,             id);
            nextTime = System.currentTimeMillis() + getFrequencyDependingOn(timeUntilAlarm);
        }

        // Whatever the analysis, we must set the intent.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                GedderAlarmApplication.getAppContext(), id, next, PendingIntent.FLAG_UPDATE_CURRENT);
        GedderAlarmManager.setOptimal(
                AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
    }

    /**
     * Returns a requery frequency depending upon some time.
     * <br>
     * Uses the following intervals:
     * <br>
     * <table>
     *     <thead>
     *         <tr>
     *             <th>Interval</th>
     *             <th>Return</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>Hour > 5</td>
     *             <td>1 hour 30 minutes</td>
     *         </tr>
     *         <tr>
     *             <td>1 < Hour &le; 5</td>
     *             <td>30 minutes</td>
     *         </tr>
     *         <tr>
     *             <td>30 < Minutes &le; 60</td>
     *             <td>10 minutes</td>
     *         </tr>
     *         <tr>
     *             <td>15 < Minutes &le; 30</td>
     *             <td>5 minutes</td>
     *         </tr>
     *         <tr>
     *             <td>10 < Minutes &le; 15</td>
     *             <td>2 minutes</td>
     *         </tr>
     *         <tr>
     *             <td>Minutes &le; 10</td>
     *             <td>1 minute</td>
     *         </tr>
     *     </tbody>
     * </table>
     * @param dependent The time to base the heuristics off of.
     * @return The frequency to check based off of the dependent.
     */
    protected long getFrequencyDependingOn(long dependent) {
        double hours = TimeUtilities.millisToHours(dependent);
        double minutes = TimeUtilities.millisToMinutes(dependent);
        if (hours > 5) {
            return TimeUtilities.getMillisIn(1, 30);    // 1 hour 30 minutes
        } else if (1 < hours && hours <= 5) {
            return TimeUtilities.minutesToMillis(30);   // 30 minutes
        } else if (30 < minutes && minutes <= 60) {
            return TimeUtilities.minutesToMillis(10);   // 10 minutes
        } else if (15 < minutes && minutes <= 30) {
            return TimeUtilities.minutesToMillis(5);    // 5 minutes
        } else if (10 < minutes && minutes <= 15) {
            return TimeUtilities.minutesToMillis(2);    // 2 minutes
        } else {
            return TimeUtilities.minutesToMillis(1);    // 1 minute
        }
    }
}
