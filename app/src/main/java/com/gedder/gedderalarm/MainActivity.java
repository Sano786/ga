/*
 * USER: jameskluz, mslm
 * DATE: 2/24/17
 */

package com.gedder.gedderalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.gedder.gedderalarm.controller.AlarmClockCursorWrapper;
import com.gedder.gedderalarm.controller.AlarmClocksCursorAdapter;
import com.gedder.gedderalarm.db.AlarmClockDBHelper;
import com.gedder.gedderalarm.model.AlarmClock;
import com.gedder.gedderalarm.util.DayPicker;

import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // TODO: Stop handling UI in this activity. Move it to a view class in the view package.
    // See http://www.techyourchance.com/mvp-mvc-android-2/

    public static final String PARCEL_ALARM_CLOCK = "_GEDDER_PARCEL_ALARM_CLOCK_";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ID_GEDDER_PERSISTENT_NOTIFICATION = 5323321;

    private final int mIntentRequestCode = 31582;

    private ListView alarmClocksListView;
    private AlarmClocksCursorAdapter mAlarmClocksCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmClockDBHelper db = new AlarmClockDBHelper(this);

        Cursor mAlarmClockCursor = db.getAllAlarmClocks();

        // Make an adapter based off of the cursor.
        mAlarmClocksCursorAdapter = new AlarmClocksCursorAdapter(this, mAlarmClockCursor);

        // Attach the adapter to the list view which we'll populate.
        alarmClocksListView = (ListView) findViewById(R.id.alarm_clocks_list);
        alarmClocksListView.setAdapter(mAlarmClocksCursorAdapter);
        db.close();

        // When an alarm in the list is clicked, go to the add/edit activity with that alarm's info.
        alarmClocksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlarmClock alarmClock = getAlarmClockInListViewFromChild(view);
                Intent intent = new Intent(GedderAlarmApplication.getAppContext(),
                        AddEditAlarmScrollingActivity.class);
                intent.putExtra(PARCEL_ALARM_CLOCK, alarmClock);
                startActivityForResult(intent, mIntentRequestCode);
            }
        });
        // When an alarm in the list is long-clicked, we activate deletion mode.
        alarmClocksListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               // First make visible the delete button.
               findViewById(R.id.activityMain_DeleteAlarmBtn).setVisibility(View.VISIBLE);
               // Now loop through all rows and make visible the checkboxes.
               for (int i = 0; i < parent.getCount(); ++i) {
                   View child = parent.getChildAt(i);
                   View item = child.findViewById(R.id.itemAlarmClock_removeCheckBox);
                   CheckBox cb = (CheckBox) item.findViewById(R.id.itemAlarmClock_removeCheckBox);
                   cb.setVisibility(View.VISIBLE);
               }
               // Finally, the item initially long-clicked is checked.
               CheckBox cb = (CheckBox) view.findViewById(R.id.itemAlarmClock_removeCheckBox);
               cb.setChecked(true);
               return true;
           }
       });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Alarm clocks might have been modified in other activities.
        AlarmClockDBHelper db = new AlarmClockDBHelper(this);
        mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
        db.close();
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.activityMain_DeleteAlarmBtn).getVisibility() != View.GONE) {
            findViewById(R.id.activityMain_DeleteAlarmBtn).setVisibility(View.GONE);
            for (int i = 0; i < alarmClocksListView.getCount(); ++i) {
                View child = alarmClocksListView.getChildAt(i);
                View item = child.findViewById(R.id.itemAlarmClock_removeCheckBox);
                CheckBox cb = (CheckBox) item.findViewById(R.id.itemAlarmClock_removeCheckBox);
                cb.setChecked(false);
                cb.setVisibility(View.GONE);
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Called by some view when a new alarm is to be made. Brings up some alarm creation activity.
     * @param view The view that references this function.
     */
    public void onClickNewAlarm(View view) {
        // Pass in new, default alarm.
        Intent intent = new Intent(this, AddEditAlarmScrollingActivity.class);
        intent.putExtra(PARCEL_ALARM_CLOCK, new AlarmClock());
        startActivityForResult(intent, mIntentRequestCode);
    }

    public void onClickDeleteAlarm(View view) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(this);
        // For each row, uncheck the checkbox, hide it, and delete alarms which were checked.
        for (int i = 0; i < alarmClocksListView.getCount(); ++i) {
            View child = alarmClocksListView.getChildAt(i);
            CheckBox cb = (CheckBox) child.findViewById(R.id.itemAlarmClock_removeCheckBox);
            cb.setVisibility(View.GONE);
            if (cb.isChecked()) {
                cb.setChecked(false);

                AlarmClock alarmClock = getAlarmClockInListViewFromChild(child);
                turnAlarmOff(alarmClock);
                if (alarmClock.isGedderOn()) {
                    turnGedderOff(alarmClock);
                }
                db.deleteAlarmClock(UUID.fromString(child.getTag().toString()));
            }
        }
        // DB is presumably different, so tell the adapter that.
        mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
        view.setVisibility(View.GONE);
        db.close();
    }

    public void onClickToggleGedder(View view) {
        View row              = (View) view.getParent();
        AlarmClock alarmClock = getAlarmClockInListViewFromChild(row);

        if (alarmClock.isGedderOn()) {
            turnGedderOff(alarmClock);
        } else {
            if (!alarmClock.isGedderEligible()) {
                Intent intent = new Intent(GedderAlarmApplication.getAppContext(),
                        AddEditAlarmScrollingActivity.class);
                intent.putExtra(PARCEL_ALARM_CLOCK, alarmClock);
                startActivityForResult(intent, mIntentRequestCode);
            } else {
                if (!alarmClock.isAlarmOn()) {
                    turnAlarmOn(alarmClock);
                }
                turnGedderOn(alarmClock);
                turnAllGeddersOffBesidesThis(alarmClock);
            }
        }

        AlarmClockDBHelper db = new AlarmClockDBHelper(this);
        db.updateAlarmClock(alarmClock);
        mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
        db.close();
    }

    public void onClickToggleAlarm(View view) {
        View row              = (View) view.getParent();
        AlarmClock alarmClock = getAlarmClockInListViewFromChild(row);

        if (!alarmClock.isAlarmOn()) {
            turnAlarmOn(alarmClock);
        } else {
            turnAlarmOff(alarmClock);
        }

        if (alarmClock.isGedderOn()) {
            turnGedderOff(alarmClock);
        }

        AlarmClockDBHelper db = new AlarmClockDBHelper(this);
        db.updateAlarmClock(alarmClock);
        mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
        db.close();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mIntentRequestCode) {
            if (resultCode == RESULT_OK) {
                AlarmClockDBHelper db = new AlarmClockDBHelper(this);
                AlarmClock alarmClock = data.getParcelableExtra(PARCEL_ALARM_CLOCK);
                turnAlarmOn(alarmClock);

                if (alarmClock.isGedderEligible()) {
                    turnGedderOn(alarmClock);
                    turnAllGeddersOffBesidesThis(alarmClock);
                } else if (!alarmClock.isGedderEligible()) {
                    turnGedderOff(alarmClock);
                }

                db.updateAlarmClock(alarmClock);
                mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
            }
        }
    }

    public static AlarmClock getAlarmClockInListViewFromChild(View view) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(GedderAlarmApplication.getAppContext());
        AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(
                db.getAlarmClock(UUID.fromString(view.getTag().toString())));
        cursor.moveToFirst();
        AlarmClock alarmClock = cursor.getAlarmClock();
        cursor.close();
        db.close();
        return alarmClock;
    }

    public static void setGedderPersistentIcon() {
        Notification notification = new Notification.Builder(GedderAlarmApplication.getAppContext())
                .setSmallIcon(R.drawable.gedder_on)
                .setContentTitle("Gedder Alarm")
                .setContentText("Gedder service on.")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();
        ((NotificationManager) GedderAlarmApplication.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(ID_GEDDER_PERSISTENT_NOTIFICATION, notification);
    }

    public static void cancelGedderPersistentIcon() {
        ((NotificationManager) GedderAlarmApplication.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(ID_GEDDER_PERSISTENT_NOTIFICATION);
    }

    /* TEMPORARY */
    public static String timeToArrival(AlarmClock alarmClock) {
        long delta = alarmClock.getArrivalTimeMillis() - System.currentTimeMillis();
        long hoursToAlarm = delta / (1000 * 60 * 60);
        long minutesToAlarm = delta / (1000 * 60) % 60;
        long daysToAlarm = hoursToAlarm / 24;
        hoursToAlarm = hoursToAlarm % 24;
        Context c = GedderAlarmApplication.getAppContext();

        String daySeq = (daysToAlarm == 0) ? "" :
                (daysToAlarm == 1) ? c.getString(R.string.day) :
                        c.getString(R.string.days, Long.toString(daysToAlarm));

        String minSeq = (minutesToAlarm == 0) ? "" :
                (minutesToAlarm == 1) ? c.getString(R.string.minute) :
                        c.getString(R.string.minutes, Long.toString(minutesToAlarm));

        String hourSeq = (hoursToAlarm == 0) ? "" :
                (hoursToAlarm == 1) ? c.getString(R.string.hour) :
                        c.getString(R.string.hours, Long.toString(hoursToAlarm));

        boolean displayDays = daysToAlarm > 0;
        boolean displayHours = hoursToAlarm > 0;
        boolean displayMinutes = minutesToAlarm > 0;

        int index = (displayDays ? 1 : 0) |
                (displayHours ? 2 : 0) |
                (displayMinutes ? 4 : 0);

        String[] formats = c.getResources().getStringArray(R.array.arrival_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    /*
     * FROM: https://android.googlesource.com/platform/packages/apps/AlarmClock
     * FILE: src/com/android/alarmclock/SetAlarm.java
     * MODIFIED BY: mslm
     * DATE OF MODIFICATION: 4/28/17 and onward.
     */
    private String timeToAlarm(AlarmClock alarmClock) {
        long delta = alarmClock.getAlarmTimeMillis() - System.currentTimeMillis();
        long hoursToAlarm = delta / (1000 * 60 * 60);
        long minutesToAlarm = delta / (1000 * 60) % 60;
        long daysToAlarm = hoursToAlarm / 24;
        hoursToAlarm = hoursToAlarm % 24;

        String daySeq = (daysToAlarm == 0) ? "" :
                (daysToAlarm == 1) ? this.getString(R.string.day) :
                        this.getString(R.string.days, Long.toString(daysToAlarm));

        String minSeq = (minutesToAlarm == 0) ? "" :
                (minutesToAlarm == 1) ? this.getString(R.string.minute) :
                        this.getString(R.string.minutes, Long.toString(minutesToAlarm));

        String hourSeq = (hoursToAlarm == 0) ? "" :
                (hoursToAlarm == 1) ? this.getString(R.string.hour) :
                        this.getString(R.string.hours, Long.toString(hoursToAlarm));

        boolean displayDays = daysToAlarm > 0;
        boolean displayHours = hoursToAlarm > 0;
        boolean displayMinutes = minutesToAlarm > 0;

        int index = (displayDays ? 1 : 0) |
                    (displayHours ? 2 : 0) |
                    (displayMinutes ? 4 : 0);

        String[] formats = this.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    private void adjustDaysInAlarm(AlarmClock alarmClock) {
        Calendar alarmTimeCalendar = alarmClock.getAlarmTime();
        Calendar arrivalTimeCalendar = alarmClock.getArrivalTime();
        DayPicker dayPicker = new DayPicker(
                alarmTimeCalendar.get(Calendar.HOUR_OF_DAY),
                alarmTimeCalendar.get(Calendar.MINUTE),
                arrivalTimeCalendar.get(Calendar.HOUR_OF_DAY),
                arrivalTimeCalendar.get(Calendar.MINUTE));
        alarmClock.setAlarmTime(
                dayPicker.getAlarmDay(),
                alarmTimeCalendar.get(Calendar.HOUR_OF_DAY),
                alarmTimeCalendar.get(Calendar.MINUTE));
        alarmClock.setArrivalTime(
                dayPicker.getArrivalDay(),
                arrivalTimeCalendar.get(Calendar.HOUR_OF_DAY),
                arrivalTimeCalendar.get(Calendar.MINUTE));
    }

    private void turnAlarmOn(AlarmClock alarmClock) {
        adjustDaysInAlarm(alarmClock);
        alarmClock.turnAlarmOn();
        Toast.makeText(this, timeToAlarm(alarmClock), Toast.LENGTH_LONG).show();
    }

    private void turnAlarmOff(AlarmClock alarmClock) {
        alarmClock.turnAlarmOff();
        toastShortMessage("Alarm off.");
    }

    private void turnGedderOn(AlarmClock alarmClock) {
        adjustDaysInAlarm(alarmClock);
        alarmClock.turnGedderOn();
        toastShortMessage("Gedder on.");
        setGedderPersistentIcon();

        // TEMPORARY
        Toast.makeText(this, timeToArrival(alarmClock), Toast.LENGTH_LONG).show();
    }

    private void turnGedderOff(AlarmClock alarmClock) {
        alarmClock.turnGedderOff();
        toastShortMessage("Gedder off.");
        cancelGedderPersistentIcon();
    }

    private void turnAllGeddersOffBesidesThis(AlarmClock alarmClock) {
        AlarmClockDBHelper db = new AlarmClockDBHelper(this);
        UUID uuid = alarmClock.getUUID();
        AlarmClockCursorWrapper cursor = new AlarmClockCursorWrapper(db.getAllAlarmClocks());
        cursor.moveToFirst();
        do {
            AlarmClock otherAlarmClock = cursor.getAlarmClock();
            if (uuid != otherAlarmClock.getUUID()) {
                otherAlarmClock.turnGedderOff();
                db.updateAlarmClock(otherAlarmClock);
            }
        } while (cursor.moveToNext());
        mAlarmClocksCursorAdapter.changeCursor(db.getAllAlarmClocks());
        db.close();
    }

    private void toastShortMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

