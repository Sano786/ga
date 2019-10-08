/*
 * USER: mslm
 * DATE: 3/11/2017
 */

package com.gedder.gedderalarm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gedder.gedderalarm.db.AlarmClockDBSchema.AlarmClockTable;
import com.gedder.gedderalarm.model.AlarmClock;

import java.util.Calendar;
import java.util.UUID;

/** A SQLite wrapper class to help in creating, reading, updating, and deleting Alarm Clocks. */

public class AlarmClockDBHelper extends SQLiteOpenHelper {
    private static final String TAG = AlarmClockDBHelper.class.getSimpleName();
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "alarmClockDatabase.db";

    /**
     * Default constructor requiring context.
     * @param context The context of the database.
     */
    public AlarmClockDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates all tables for the database.
     * @see com.gedder.gedderalarm.db.AlarmClockDBSchema
     * @param db The database we create tables for.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO: Change AlarmDay/ArrivalDay to INT.
        db.execSQL("CREATE TABLE " + AlarmClockTable.TABLE_NAME + "("
                + AlarmClockTable.Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AlarmClockTable.Columns.UUID  + " TEXT, "
                + AlarmClockTable.Columns.REQUEST_CODE + " INTEGER, "
                + AlarmClockTable.Columns.ORIGIN_ID + " TEXT, "
                + AlarmClockTable.Columns.ORIGIN_ADDRESS + " TEXT, "
                + AlarmClockTable.Columns.DESTINATION_ID + " TEXT, "
                + AlarmClockTable.Columns.DESTINATION_ADDRESS + " TEXT, "
                + AlarmClockTable.Columns.TRAVEL_MODE + " TEXT, "
                + AlarmClockTable.Columns.TRANSIT_MODE + " TEXT, "
                + AlarmClockTable.Columns.REPEAT_DAYS + " SMALLINT, "
                + AlarmClockTable.Columns.ALARM_DAY    + " INTEGER, "
                + AlarmClockTable.Columns.ALARM_HOUR   + " SMALLINT, "
                + AlarmClockTable.Columns.ALARM_MINUTE + " SMALLINT, "
                + AlarmClockTable.Columns.ALARM_TIME   + " INT8, "
                + AlarmClockTable.Columns.ARRIVAL_DAY    + " INTEGER, "
                + AlarmClockTable.Columns.ARRIVAL_HOUR   + " SMALLINT, "
                + AlarmClockTable.Columns.ARRIVAL_MINUTE + " SMALLINT, "
                + AlarmClockTable.Columns.ARRIVAL_TIME   + " INT8, "
                + AlarmClockTable.Columns.PREP_HOUR   + " SMALLINT, "
                + AlarmClockTable.Columns.PREP_MINUTE + " SMALLINT, "
                + AlarmClockTable.Columns.PREP_TIME   + " INT8, "
                + AlarmClockTable.Columns.ALARM_SET  + " BOOLEAN, "
                + AlarmClockTable.Columns.GEDDER_SET + " BOOLEAN)"
        );
    }

    /**
     * Will drop all tables if they exist and recreate them.
     * @see com.gedder.gedderalarm.db.AlarmClockDBSchema
     * @param db The database to drop and recreate.
     * @param oldVersion Old version of the database.
     * @param newVersion New version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AlarmClockTable.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Adds an alarm clock to the database, including the time for its alarm, and whether it is
     * currently set or not. Also adds its UUID to the database.
     * @param alarmClock The AlarmClock object to add to the database.
     */
    public void addAlarmClock(AlarmClock alarmClock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = putAll(alarmClock);
        db.insert(AlarmClockTable.TABLE_NAME, null, cv);
        db.close();
    }

    /**
     * Gets the alarm clock with UUID uuid, if it exists in the database.
     * @param uuid The UUID of the alarm clock to get.
     * @return The AlarmClock object having UUID uuid.
     */
    public Cursor getAlarmClock(UUID uuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + AlarmClockTable.TABLE_NAME
                            + " WHERE " + AlarmClockTable.Columns.UUID + "=?",
                            new String[] { uuid.toString() });
    }

    /**
     * Returns a list of all alarm clocks currently in the database, active or not.
     * @return A list of all alarm clocks currently existing in the database.
     */
    public Cursor getAllAlarmClocks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + AlarmClockTable.TABLE_NAME, null);
    }

    /**
     * Returns the number of alarm clocks in the database, active or not.
     * @return The number of alarm clocks in the database.
     */
    public int getAlarmClockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT  * FROM " + AlarmClockTable.TABLE_NAME, null).getCount();
    }

    /**
     * Updates an existing alarm clock to the parameters of the passed in alarm clock.
     * @param alarmClock The alarm clock whose values to copy into the alarm clock with UUID uuid.
     * @return Number of rows affected by the update. Not 1 if failed.
     */
    public int updateAlarmClock(AlarmClock alarmClock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = putAllExceptUuid(alarmClock);
        return db.update(AlarmClockTable.TABLE_NAME, cv,
                AlarmClockTable.Columns.UUID + "=?",
                new String[] { alarmClock.getUUID().toString() });
    }

    /**
     * Deletes the alarm clock that has UUID uuid.
     * @param uuid The UUID of the alarm clock to delete.
     */
    public int deleteAlarmClock(UUID uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(AlarmClockTable.TABLE_NAME,
                AlarmClockTable.Columns.UUID + "=?", new String[] { uuid.toString() });
    }

    /**
     *
     * @param alarmClock
     * @return
     */
    private ContentValues putAll(AlarmClock alarmClock) {
        ContentValues cv = new ContentValues(putAllExceptUuid(alarmClock));
        cv.put(AlarmClockTable.Columns.UUID, alarmClock.getUUID().toString());
        return cv;
    }

    /**
     *
     * @param alarmClock
     * @return
     */
    private ContentValues putAllExceptUuid(AlarmClock alarmClock) {
        ContentValues cv = new ContentValues();
        cv.put(AlarmClockTable.Columns.REQUEST_CODE,        alarmClock.getRequestCode());
        cv.put(AlarmClockTable.Columns.ORIGIN_ID,           alarmClock.getOriginId());
        cv.put(AlarmClockTable.Columns.ORIGIN_ADDRESS,      alarmClock.getOriginAddress());
        cv.put(AlarmClockTable.Columns.DESTINATION_ID,      alarmClock.getDestinationId());
        cv.put(AlarmClockTable.Columns.DESTINATION_ADDRESS, alarmClock.getDestinationAddress());
        cv.put(AlarmClockTable.Columns.TRAVEL_MODE,         alarmClock.getTravelMode().name());
        cv.put(AlarmClockTable.Columns.TRANSIT_MODE,        alarmClock.getTransitMode().name());
        cv.put(AlarmClockTable.Columns.REPEAT_DAYS,         alarmClock.getRepeatDays().getCoded());
        cv.put(AlarmClockTable.Columns.ALARM_DAY,           alarmClock.getAlarmTime().get(Calendar.DAY_OF_YEAR));
        cv.put(AlarmClockTable.Columns.ALARM_HOUR,          alarmClock.getAlarmTime().get(Calendar.HOUR_OF_DAY));
        cv.put(AlarmClockTable.Columns.ALARM_MINUTE,        alarmClock.getAlarmTime().get(Calendar.MINUTE));
        cv.put(AlarmClockTable.Columns.ALARM_TIME,          alarmClock.getAlarmTimeMillis());
        cv.put(AlarmClockTable.Columns.ARRIVAL_DAY,         alarmClock.getArrivalTime().get(Calendar.DAY_OF_YEAR));
        cv.put(AlarmClockTable.Columns.ARRIVAL_HOUR,        alarmClock.getArrivalTime().get(Calendar.HOUR_OF_DAY));
        cv.put(AlarmClockTable.Columns.ARRIVAL_MINUTE,      alarmClock.getArrivalTime().get(Calendar.MINUTE));
        cv.put(AlarmClockTable.Columns.ARRIVAL_TIME,        alarmClock.getArrivalTimeMillis());
        cv.put(AlarmClockTable.Columns.PREP_HOUR,           alarmClock.getPrepHours());
        cv.put(AlarmClockTable.Columns.PREP_MINUTE,         alarmClock.getPrepMinutes());
        cv.put(AlarmClockTable.Columns.PREP_TIME,           alarmClock.getPrepTimeMillis());
        cv.put(AlarmClockTable.Columns.ALARM_SET,           alarmClock.isAlarmOn());
        cv.put(AlarmClockTable.Columns.GEDDER_SET,          alarmClock.isGedderOn());
        return cv;
    }

    /**
     * Gets the last ID in the database.
     * @return The last ID that exists in the database. -1 if no alarm clocks exist.
     */
    private int getLastId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor idCursor = db.rawQuery("SELECT seq FROM sqlite_sequence " +
                "WHERE NAME=\"" + AlarmClockTable.TABLE_NAME + "\"", null);
        db.close();

        int lastId = -1;
        if (idCursor != null) {
            idCursor.moveToFirst();
            lastId = idCursor.getInt(0);
            idCursor.close();
        }
        return lastId;
    }
}
