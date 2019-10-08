/*
 * USER: jameskluz, mslm
 * DATE: 3/1/17
 */

package com.gedder.gedderalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gedder.gedderalarm.controller.AlarmClockCursorWrapper;
import com.gedder.gedderalarm.db.AlarmClockDBHelper;
import com.gedder.gedderalarm.model.AlarmClock;
import com.gedder.gedderalarm.model.GedderEngine;
import com.gedder.gedderalarm.util.TimeUtilities;

import java.util.Calendar;
import java.util.UUID;

public class AlarmActivity extends AppCompatActivity {
    public static final String PARAM_ALARM_UUID = "__PARAM_ALARM_UUID__";

    private static final String TAG = AlarmActivity.class.getSimpleName();

    // This is used to get the ringtone.
    private Uri alert;
    private Ringtone ringtone;
    private int mPrepTime;
    private String mDestination;
    private String mOrigin;
    private TextView mLeaveByMinutes;
    private TextView mLeaveByTimeDisplay;
    private TextView mArriveTimeDisplay;
    private Button mGoogleMapsBtn;
    private Button mSnoozeBtn;
    private Button mStopAlarmBtn;
    private Calendar mCurrentTime;
    private Calendar mArriveTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // First thing's first: turn off the alarm internally.
        Intent intent = getIntent();
        Bundle results = intent.getBundleExtra("bundle");
        final UUID alarmUuid = (UUID) intent.getSerializableExtra(PARAM_ALARM_UUID);
        turnOffAlarm(alarmUuid);
        //need this in order to initialize other variables
        mCurrentTime = Calendar.getInstance();
        // Now play the alarm sound.
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            // Use backup.
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                // 2nd backup.
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        ringtone = RingtoneManager.getRingtone(this, alert);
        ringtone.play();

        if (results != null) {
            gedder_initialize(results);
        } else {
            alarm_initialize();
        }
        /*TODO Add way for user to interact with a snoozed alarm*/
        mStopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopAlarm();
            }
        });
        mSnoozeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long snoozeTime = TimeUtilities.minutesToMillis(10);

                /* TECH DEBT: Repeat code! Make a damn single global instance of db already. */
                AlarmClockDBHelper db = new AlarmClockDBHelper(GedderAlarmApplication.getAppContext());
                AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(db.getAlarmClock(alarmUuid));
                cursor.moveToFirst();
                AlarmClock alarmClock = cursor.getAlarmClock();

                Intent snooze = new Intent(GedderAlarmApplication.getAppContext(), AlarmReceiver.class);
                snooze.putExtra(AlarmReceiver.PARAM_ALARM_UUID, alarmClock.getUUID());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        GedderAlarmApplication.getAppContext(),
                        alarmClock.getRequestCode(), snooze, PendingIntent.FLAG_UPDATE_CURRENT);
                GedderAlarmManager.setOptimal(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + snoozeTime, pendingIntent);

                snooze();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        turnOffAlarmSound();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnOffAlarmSound();
        finish();
    }

    /** This disables the back button; the user should explicitly say what to do next. */
    @Override
    public void onBackPressed() {
        /* Intentionally empty */
    }

    /**
     * Turns off both the alarm clock and any associated services (i.e. Gedder).
     * @param uuid The UUID of the alarm clock in question.
     */
    private void turnOffAlarm(UUID uuid) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(GedderAlarmApplication.getAppContext());
        AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(db.getAlarmClock(uuid));
        cursor.moveToFirst();
        AlarmClock alarmClock = cursor.getAlarmClock();

        // Grab variables we need from the alarmClock.
        mPrepTime = (int) (alarmClock.getPrepTimeMillis() / 60000);
        mDestination = alarmClock.getDestinationAddress();
        mOrigin = alarmClock.getOriginAddress();
        mArriveTime = alarmClock.getArrivalTime();

        // Since the alarm just went off, we need to now internally say it's off.
        alarmClock.setAlarm(AlarmClock.OFF);

        if (alarmClock.isGedderOn()) {
            alarmClock.turnGedderOff();
            MainActivity.cancelGedderPersistentIcon();
        }

        db.updateAlarmClock(alarmClock);
        db.close();
    }

    private void turnOffAlarmSound() {
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void gedder_initialize(Bundle results){
        setContentView(R.layout.alarm_display_gedder);
        mLeaveByMinutes = (TextView) findViewById(R.id.gedderAlarmDisp_leaveByXminBox);
        Long arriveTimeMilli = mArriveTime.getTimeInMillis();
        int arriveTimeMin = (int)(arriveTimeMilli / 60000);
        int currentTimeMin = (int)(mCurrentTime.getTimeInMillis()/60000);
        /*TODO Figure out how to not need padding*/
        int padding = com.gedder.gedderalarm.GedderReceiver.TRAVEL_TIME_PADDING;
        int travelTimeMin = (results.getInt(GedderEngine.RESULT_DURATION) / 60) + padding;
        int actualPrepMin = arriveTimeMin - (currentTimeMin + travelTimeMin);
        boolean mWarnLessPrep = actualPrepMin < mPrepTime;
        if (actualPrepMin > 0) {
            int actualPrepHours = actualPrepMin / 60;
            actualPrepMin %= 60;
            mLeaveByMinutes.setText(timeToLeave(actualPrepHours, actualPrepMin));
        } else {
            mLeaveByMinutes.setText("YOU MUST LEAVE IMMEDIATELY!");
        }
        if (mWarnLessPrep) {
            mLeaveByMinutes.setTextColor(Color.RED);
        } else {
            mLeaveByMinutes.setTextColor(Color.parseColor("#FF74BA59"));
        }
        mLeaveByTimeDisplay = (TextView) findViewById(R.id.gedderAlarmDisp_leaveByTime);
        Long leaveTimeMilli = arriveTimeMilli - (travelTimeMin*60000);
        Calendar leaveBy = Calendar.getInstance();
        leaveBy.setTimeInMillis(leaveTimeMilli);
        mLeaveByTimeDisplay.setText(returnTimeAsString(leaveBy));
        mArriveTimeDisplay = (TextView) findViewById(R.id.gedderAlarmDisp_getThereByTime);
        mArriveTimeDisplay.setText(returnTimeAsString(mArriveTime));
        mGoogleMapsBtn = (Button) findViewById(R.id.gedderAlarmDisp_GoogleMapsBtn);
        mGoogleMapsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String origin_address = mOrigin.replaceAll(" ", "+");
                    String destination_address = mDestination.replaceAll(" ", "+");
                    String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+origin_address+","+"&daddr="+ destination_address;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(Intent.createChooser(intent, "Select an application"));
                } catch (Exception e){
                    Toast.makeText(getBaseContext(),
                            "Trouble opening Google Maps, please make sure it is installed!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        mStopAlarmBtn = (Button) findViewById(R.id.gedderAlarmDisp_stopAlarmBtn);
        mSnoozeBtn = (Button) findViewById(R.id.gedderAlarmDisp_snoozeBtn);
    }

    private void alarm_initialize() {
        setContentView(R.layout.alarm_display_normal);
        mStopAlarmBtn = (Button) findViewById(R.id.normalAlarmDisp_stopAlarmBtn);
        mSnoozeBtn = (Button) findViewById(R.id.normalAlarmDisp_snoozeBtn);
    }

    private String tellUserMinutesTillLeave(int prepHours, int prepMinutes) {
        return "You have " + Integer.toString(prepHours) + " hour(s) and "
                + Integer.toString(prepMinutes) + " minute(s) until you need to leave.";
    }

    private String timeToLeave(int prepHours, int prepMinutes) {
        long hoursToAlarm = prepHours;
        long minutesToAlarm = prepMinutes;

        String minSeq = (minutesToAlarm == 0) ? "" :
                (minutesToAlarm == 1) ? this.getString(R.string.minute) :
                        this.getString(R.string.minutes, Long.toString(minutesToAlarm));

        String hourSeq = (hoursToAlarm == 0) ? "" :
                (hoursToAlarm == 1) ? this.getString(R.string.hour) :
                        this.getString(R.string.hours, Long.toString(hoursToAlarm));

        boolean displayHours = hoursToAlarm > 0;
        boolean displayMinutes = minutesToAlarm > 0;

        int index = (displayHours ? 2 : 0) |
                (displayMinutes ? 4 : 0);

        String[] formats = this.getResources().getStringArray(R.array.leave_by);
        return String.format(formats[index], "", hourSeq, minSeq);
    }

    private String returnTimeAsString(Calendar time) {
        int hourOfDay = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        String am_or_pm;
        if (hourOfDay >= 12) {
            am_or_pm = "pm";
            hourOfDay = hourOfDay - 12;
        } else {
            am_or_pm = "am";
        }
        if (hourOfDay == 0) {
            hourOfDay = 12;
        }
        String hour_string = Integer.toString(hourOfDay);
        if(hourOfDay < 10) {
            hour_string = "0" + hour_string;
        }
        String minute_string = Integer.toString(minute);
        if(minute < 10) {
            minute_string = "0" + minute_string;
        }
        return (hour_string + ":" + minute_string + " " + am_or_pm);
    }

    private void stopAlarm() {
        turnOffAlarmSound();
        finish();
    }

    private void snooze() {
        turnOffAlarmSound();
        finish();
    }
}
