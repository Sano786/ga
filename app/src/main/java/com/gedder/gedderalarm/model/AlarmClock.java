/*
 * USER: mslm
 * DATE: 3/10/2017
 */

package com.gedder.gedderalarm.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.gedder.gedderalarm.GedderAlarmManager;
import com.gedder.gedderalarm.google.TransitMode;
import com.gedder.gedderalarm.google.TravelMode;
import com.gedder.gedderalarm.util.DaysOfWeek;
import com.gedder.gedderalarm.util.TimeUtilities;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

/**
 * An alarm clock class encapsulating all data for both a typical alarm clock and the specialized
 * Gedder alarm clock.
 */

public class AlarmClock implements Parcelable {
    public static final int ON  = 1;
    public static final int OFF = 0;

    private static final String TAG = AlarmClock.class.getSimpleName();

    // Default values for certain private variables.
    private static final String      DEFAULT_ORIGIN_ID           = "";
    private static final String      DEFAULT_ORIGIN_ADDRESS      = "";
    private static final String      DEFAULT_DESTINATION_ID      = "";
    private static final String      DEFAULT_DESTINATION_ADDRESS = "";
    private static final TravelMode  DEFAULT_TRAVEL_MODE         = TravelMode.DRIVING;
    private static final TransitMode DEFAULT_TRANSIT_MODE        = TransitMode.SUBWAY;
    private static final int         DEFAULT_ALARM_HOUR          = 6;
    private static final int         DEFAULT_ALARM_MINUTE        = 0;
    private static final int         DEFAULT_ARRIVAL_HOUR        = 7;
    private static final int         DEFAULT_ARRIVAL_MINUTE      = 0;
    private static final int         DEFAULT_PREP_HOUR           = 0;
    private static final int         DEFAULT_PREP_MINUTE         = 0;
    private static final boolean     DEFAULT_ALARM_SET           = false;
    private static final boolean     DEFAULT_GEDDER_SET          = false;

    // Required for universal uniqueness of each alarm.
    private UUID mUuid;

    // Unique number to identify this alarm in PendingIntents.
    private int mRequestCode;

    // Required for smart alarm.
    private String      mOriginId;
    private String      mOriginAddress;
    private String      mDestinationId;
    private String      mDestinationAddress;
    private TravelMode  mTravelMode;
    private TransitMode mTransitMode;

    // The days this alarm will repeat in its current form.
    private DaysOfWeek mRepeatDays;

    // The real-time internal values for when this alarm is set, if it is set.
    private int  mAlarmDay;          // Day in year.
    private int  mAlarmHour;         // 0-23 (24 hour clock)
    private int  mAlarmMinute;       // 0-59 (60 minutes)
    private long mAlarmTime;         // Milliseconds since the epoch.

    // The user-planned arrival time to mDestinationId from mOriginId.
    private int  mArrivalDay;        // Day in year.
    private int  mArrivalHour;       // 0-23 (24 hour clock)
    private int  mArrivalMinute;     // 0-59 (60 minutes)
    private long mArrivalTime;       // Milliseconds since the epoch.

    // The user-inputted or smart-adjusted time it takes to get prepared in the morning.
    private int  mPrepHour;          // 0-23 (24 hour clock)
    private int  mPrepMinute;        // 0-59 (60 minutes)
    private long mPrepTime;          // Milliseconds since the epoch.

    // The different types of alarms available.
    private boolean mAlarmSet;
    private boolean mGedderSet;

    /** Initializes an unset alarm clock with default parameters. */
    public AlarmClock() {
        Calendar calendar = Calendar.getInstance();
        int tomorrow = calendar.get(Calendar.DAY_OF_YEAR) + 1;

        mUuid               = UUID.randomUUID();
        mRequestCode        = Math.abs((new Random()).nextInt());
        mOriginId           = DEFAULT_ORIGIN_ID;           // Device location should be default.
        mOriginAddress      = DEFAULT_ORIGIN_ADDRESS;      // Device location should be default.
        mDestinationId      = DEFAULT_DESTINATION_ID;      // With history, pick latest one.
        mDestinationAddress = DEFAULT_DESTINATION_ADDRESS; // With history, pick latest one.
        mTravelMode         = DEFAULT_TRAVEL_MODE;
        mTransitMode        = DEFAULT_TRANSIT_MODE;
        mRepeatDays         = new DaysOfWeek();
        setAlarmTime        (tomorrow, DEFAULT_ALARM_HOUR, DEFAULT_ALARM_MINUTE);
        setArrivalTime      (tomorrow, DEFAULT_ARRIVAL_HOUR, DEFAULT_ARRIVAL_MINUTE);
        setPrepTime         (DEFAULT_PREP_HOUR, DEFAULT_PREP_MINUTE);
        mAlarmSet           = DEFAULT_ALARM_SET;
        mGedderSet          = DEFAULT_GEDDER_SET;
    }

    /**
     * Copy constructor.
     * @param alarmClock The alarm clock instance to copy.
     */
    public AlarmClock(AlarmClock alarmClock) {
        mUuid               = alarmClock.mUuid;
        mRequestCode        = alarmClock.mRequestCode;
        mOriginId           = alarmClock.mOriginId;
        mOriginAddress      = alarmClock.mOriginAddress;
        mDestinationId      = alarmClock.mDestinationId;
        mDestinationAddress = alarmClock.mDestinationAddress;
        mTravelMode         = alarmClock.mTravelMode;
        mTransitMode        = alarmClock.mTransitMode;
        mRepeatDays         = alarmClock.mRepeatDays;
        setAlarmTime        (alarmClock.mAlarmDay, alarmClock.mAlarmHour, alarmClock.mAlarmMinute);
        setArrivalTime      (alarmClock.mArrivalDay, alarmClock.mArrivalHour, alarmClock.mArrivalMinute);
        setPrepTime         (alarmClock.mPrepHour, alarmClock.mPrepMinute);
        mAlarmSet           = alarmClock.mAlarmSet;
        mGedderSet          = alarmClock.mGedderSet;
    }

    /**
     * A copy constructor with explicit variables.
     * @param originId           The place the user is leaving from.
     * @param originAddress
     * @param destinationId      The place the user wants to get to.
     * @param destinationAddress
     * @param repeatDays         The days this alarm clock will repeat.
     * @param alarmTime          The time for which this alarm is set (or going to be set for).
     * @param arrivalTime        The time the user needs to get to their destination.
     * @param prepHour           The hour portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     * @param prepMinute         The minute portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     */
    public AlarmClock(String originId, String originAddress,
                      String destinationId, String destinationAddress,
                      TravelMode travelMode, TransitMode transitMode,
                      DaysOfWeek repeatDays,
                      Calendar alarmTime,
                      Calendar arrivalTime,
                      int prepHour, int prepMinute) {
        mUuid               = UUID.randomUUID();
        mRequestCode        = Math.abs((new Random()).nextInt());
        mOriginId           = originId;
        mOriginAddress      = originAddress;
        mDestinationId      = destinationId;
        mDestinationAddress = destinationAddress;
        mTravelMode         = travelMode;
        mTransitMode        = transitMode;
        mRepeatDays         = repeatDays;
        setAlarmTime        (alarmTime);
        setArrivalTime      (arrivalTime);
        setPrepTime         (prepHour, prepMinute);
        mAlarmSet           = DEFAULT_ALARM_SET;
        mGedderSet          = DEFAULT_GEDDER_SET;
    }

    /**
     * A copy constructor with <em>very</em> explicit values.
     * @param originId           The place the user is leaving from.
     * @param originAddress
     * @param destinationId      The place the user wants to get to.
     * @param destinationAddress
     * @param repeatDays         The days this alarm clock will repeat.
     * @param alarmDay           The day portion of the time for which this alarm is to be set.
     * @param alarmHour          The hour portion of the time for which this alarm is to be set.
     * @param alarmMinute        The minute portion of the time for which this alarm is to be set.
     * @param arrivalDay         The day portion of the time the user needs to get to their
     *                           destination.
     * @param arrivalHour        The hour portion of the time the user needs to get to their
     *                           destination.
     * @param arrivalMinute      The minute portion of the time the user needs to get to their
     *                           destination.
     * @param prepHour           The hour portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     * @param prepMinute         The minute portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     */
    public AlarmClock(String originId, String originAddress,
                      String destinationId, String destinationAddress,
                      TravelMode travelMode, TransitMode transitMode,
                      DaysOfWeek repeatDays,
                      int alarmDay, int alarmHour, int alarmMinute,
                      int arrivalDay, int arrivalHour, int arrivalMinute,
                      int prepHour, int prepMinute) {
        mUuid               = UUID.randomUUID();
        mRequestCode        = Math.abs((new Random()).nextInt());
        mOriginId           = originId;
        mOriginAddress      = originAddress;
        mDestinationId      = destinationId;
        mDestinationAddress = destinationAddress;
        mTravelMode         = travelMode;
        mTransitMode        = transitMode;
        mRepeatDays         = repeatDays;
        setAlarmTime        (alarmDay, alarmHour, alarmMinute);
        setArrivalTime      (arrivalDay, arrivalHour, arrivalMinute);
        setPrepTime         (prepHour, prepMinute);
        mAlarmSet           = DEFAULT_ALARM_SET;
        mGedderSet          = DEFAULT_GEDDER_SET;
    }

    /**
     * A copy constructor with <em>very</em> explicit values plus the ability to set UUID and
     * request code.
     * @param uuid               The universally unique identifier for this alarm.
     * @param requestCode        The code used to identify the alarm among others in pending intents.
     * @param originId           The place the user is leaving from.
     * @param originAddress
     * @param destinationId      The place the user wants to get to.
     * @param destinationAddress
     * @param repeatDays         The days this alarm clock will repeat.
     * @param alarmDay           The day portion of the time for which this alarm is to be set.
     * @param alarmHour          The hour portion of the time for which this alarm is to be set.
     * @param alarmMinute        The minute portion of the time for which this alarm is to be set.
     * @param arrivalDay         The day portion of the time the user needs to get to their
     *                           destination.
     * @param arrivalHour        The hour portion of the time the user needs to get to their
     *                           destination.
     * @param arrivalMinute      The minute portion of the time the user needs to get to their
     *                           destination.
     * @param prepHour           The hour portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     * @param prepMinute         The minute portion of the time it takes the user to get prepared for
     *                           travel after the alarm goes off.
     */
    public AlarmClock(UUID uuid, int requestCode,
                      String originId, String originAddress,
                      String destinationId, String destinationAddress,
                      TravelMode travelMode, TransitMode transitMode,
                      DaysOfWeek repeatDays,
                      int alarmDay, int alarmHour, int alarmMinute,
                      int arrivalDay, int arrivalHour, int arrivalMinute,
                      int prepHour, int prepMinute) {
        mUuid               = uuid;
        mRequestCode        = requestCode;
        mOriginId           = originId;
        mOriginAddress      = originAddress;
        mDestinationId      = destinationId;
        mDestinationAddress = destinationAddress;
        mTravelMode         = travelMode;
        mTransitMode        = transitMode;
        mRepeatDays         = repeatDays;
        setAlarmTime        (alarmDay, alarmHour, alarmMinute);
        setArrivalTime      (arrivalDay, arrivalHour, arrivalMinute);
        setPrepTime         (prepHour, prepMinute);
        mAlarmSet           = DEFAULT_ALARM_SET;
        mGedderSet          = DEFAULT_GEDDER_SET;
    }

    /** Defaults any current settings and turns off any running alarms. */
    public void defaultAlarmSettings() {
        Calendar calendar = Calendar.getInstance();
        int tomorrow = calendar.get(Calendar.DAY_OF_YEAR) + 1;

        mOriginId           = DEFAULT_ORIGIN_ID;           // Device location should be default.
        mOriginAddress      = DEFAULT_ORIGIN_ADDRESS;      // Device location should be default.
        mDestinationId      = DEFAULT_DESTINATION_ID;      // With history, pick latest one.
        mDestinationAddress = DEFAULT_DESTINATION_ADDRESS; // With history, pick latest one.
        mTravelMode         = DEFAULT_TRAVEL_MODE;
        mTransitMode        = DEFAULT_TRANSIT_MODE;
        mRepeatDays         = new DaysOfWeek();
        setAlarmTime        (tomorrow, DEFAULT_ALARM_HOUR, DEFAULT_ALARM_MINUTE);
        setArrivalTime      (tomorrow, DEFAULT_ARRIVAL_HOUR, DEFAULT_ARRIVAL_MINUTE);
        setPrepTime         (DEFAULT_PREP_HOUR, DEFAULT_PREP_MINUTE);
        if (isAlarmOn()) turnAlarmOff();
        if (isGedderOn()) turnGedderOff();
    }

    /**
     * Sets id for the purpose of the Gedder alarm.
     * @param id The id to set to. Must be non-null.
     * @throws IllegalArgumentException If id is null.
     */
    public void setOriginId(String id) {
        if (id == null) throw new IllegalArgumentException("Null origin id.");
        mOriginId = id;
    }

    /**
     * Sets address address for the purpose of the Gedder alarm.
     * @param address The address to set to. Must be non-null.
     * @throws IllegalArgumentException If address is null.
     */
    public void setOriginAddress(String address) {
        if (address == null) throw new IllegalArgumentException("Null origin address.");
        mOriginAddress = address;
    }

    /**
     * Sets id for the purpose of the Gedder alarm.
     * @param id The id to set to. Must be non-null.
     * @throws IllegalArgumentException If id is null.
     */
    public void setDestinationId(String id) {
        if (id == null) throw new IllegalArgumentException("Null destination id.");
        mDestinationId = id;
    }

    /**
     * Sets address for the purpose of the Gedder alarm.
     * @param address The address to set to. Must be non-null.
     * @throws IllegalArgumentException If address is null.
     */
    public void setDestinationAddress(String address) {
        if (address == null) throw new IllegalArgumentException("Null destination address.");
        mDestinationAddress = address;
    }

    public void setTravelMode(TravelMode travelMode) {
        if (travelMode == null) throw new IllegalArgumentException("Null travel mode.");
        mTravelMode = travelMode;
    }

    public void setTransitMode(TransitMode transitMode) {
        if (transitMode == null) throw new IllegalArgumentException("Null transit mode.");
        mTransitMode = transitMode;
    }

    /**
     * Sets the internal alarm time according to the Calendar's {@link Calendar#DAY_OF_YEAR},
     * {@link Calendar#HOUR}, and {@link Calendar#MINUTE} keys.
     * @param future A calendar that must have valid values for the keys
     *               {@link Calendar#DAY_OF_YEAR}, {@link Calendar#HOUR}, and
     *               {@link Calendar#MINUTE}.
     */
    public void setAlarmTime(Calendar future) {
        setAlarmTime(
                future.get(Calendar.DAY_OF_YEAR),
                future.get(Calendar.HOUR_OF_DAY),
                future.get(Calendar.MINUTE));
    }

    /**
     * Works the same as its public counterpart, {@link #setAlarmTime(Calendar)}, but uses explicit
     * inputs.
     * @param day       The day to set the internal alarm to.
     * @param hour      The hour to set the internal alarm to.
     * @param minute    The minute to set the internal alarm to.
     * @see #setAlarmTime(Calendar)
     */
    public void setAlarmTime(int day, int hour, int minute) {
        mAlarmDay    = day;
        mAlarmHour   = hour;
        mAlarmMinute = minute;
        mAlarmTime   = getAlarmTimeMillis();
    }

    /**
     * Sets the arrival time according to the Calendar's {@link Calendar#DAY_OF_YEAR},
     * {@link Calendar#HOUR}, and {@link Calendar#MINUTE} keys.
     * @param future A calendar that must have valid values for the keys
     *               {@link Calendar#DAY_OF_YEAR}, {@link Calendar#HOUR}, and
     *               {@link Calendar#MINUTE}.
     */
    public void setArrivalTime(Calendar future) {
        setArrivalTime(
                future.get(Calendar.DAY_OF_YEAR),
                future.get(Calendar.HOUR_OF_DAY),
                future.get(Calendar.MINUTE));
    }

    /**
     * Works the same as its public counterpart, {@link #setArrivalTime(Calendar)}, but uses explicit
     * inputs.
     * @param day       The day to set the arrival time to.
     * @param hour      The hour to set the arrival time to.
     * @param minute    The minute to set the arrival time to.
     * @see #setArrivalTime(Calendar)
     */
    public void setArrivalTime(int day, int hour, int minute) {
        mArrivalDay    = day;
        mArrivalHour   = hour;
        mArrivalMinute = minute;
        mArrivalTime   = getArrivalTimeMillis();
    }

    /**
     * Sets the time it takes to get prepared for departure after the alarm triggers.
     * @param hour      The hours it takes to get prepared.
     * @param minute    The minutes it takes to get prepared.
     */
    public void setPrepTime(int hour, int minute) {
        mPrepHour   = hour;
        mPrepMinute = minute;
        mPrepTime   = TimeUtilities.getMillisIn(mPrepHour, mPrepMinute);
    }

    /**
     * Toggles the alarm on and off.
     */
    public void toggleAlarm() {
        if (!isAlarmOn()) {
            turnAlarmOn();
        } else {
            turnAlarmOff();
        }
    }

    public void turnAlarmOn() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GedderAlarmManager.PARAM_UUID, mUuid);
        bundle.putInt(GedderAlarmManager.PARAM_UNIQUE_ID, mRequestCode);
        bundle.putLong(GedderAlarmManager.PARAM_ALARM_TIME, mAlarmTime);
        GedderAlarmManager.setAlarm(bundle);
        mAlarmSet = true;
    }

    public void turnAlarmOff() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GedderAlarmManager.PARAM_UUID, mUuid);
        bundle.putInt(GedderAlarmManager.PARAM_UNIQUE_ID, mRequestCode);
        bundle.putLong(GedderAlarmManager.PARAM_ALARM_TIME, mAlarmTime);
        GedderAlarmManager.cancelAlarm(bundle);
        mAlarmSet = false;
    }

    /** Toggle the Gedder service for this alarm on and off. */
    public void toggleGedder() {
        if (!isGedderOn()) {
            turnGedderOn();
        } else {
            turnGedderOff();
        }
    }

    public void turnGedderOn() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(GedderAlarmManager.PARAM_ALARM_CLOCK, this);
        bundle.putInt(GedderAlarmManager.PARAM_UNIQUE_ID, mRequestCode);
        GedderAlarmManager.setGedder(bundle);
        mGedderSet = true;
    }

    public void turnGedderOff() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(GedderAlarmManager.PARAM_ALARM_CLOCK, this);
        bundle.putInt(GedderAlarmManager.PARAM_UNIQUE_ID, mRequestCode);
        GedderAlarmManager.cancelGedder(bundle);
        mGedderSet = false;
    }

    /**
     *
     * @param flag
     */
    public void setAlarm(int flag) {
        if (flag == OFF) {
            mAlarmSet = false;
        } else if (flag == ON) {
            mAlarmSet = true;
        } else {
            throw new IllegalArgumentException("Unrecognized flag in setAlarm.");
        }
    }

    /**
     *
     * @param flag
     */
    public void setGedder(int flag) {
        if (flag == OFF) {
            mGedderSet = false;
        } else if (flag == ON) {
            mGedderSet = true;
        } else {
            throw new IllegalArgumentException("Unrecognized flag in setGedder.");
        }
    }

    /**
     * Gets the universally unique identification associated with this particular alarm clock.
     * @return The universally unique identifcation for this alarm clock.
     */
    public UUID getUUID() {
        return mUuid;
    }

    /**
     * Gets the unique number used to identify this alarm in PendingIntents.
     * @return The unique number used to identify this alarm in PendingIntents.
     */
    public int getRequestCode() {
        return mRequestCode;
    }

    /**
     * Gets the place the user is leaving from.
     * @return The place the user is leaving from.
     */
    public String getOriginId() {
        return mOriginId;
    }

    /**
     * Gets the address of the place the user is leaving from.
     * @return The place the user is leaving from.
     */
    public String getOriginAddress() {
        return mOriginAddress;
    }

    /**
     * Gets the place the user is going to.
     * @return The place the user is going to.
     */
    public String getDestinationId() {
        return mDestinationId;
    }

    /**
     * Gets the address of the place place the user is going to.
     * @return The place the user is going to.
     */
    public String getDestinationAddress() {
        return mDestinationAddress;
    }

    public TravelMode getTravelMode() {
        return mTravelMode;
    }

    public TransitMode getTransitMode() {
        return mTransitMode;
    }

    /**
     * Gets the days that this alarm is set to repeat on.
     * @return The days this alarm is set to repeat on.
     */
    public DaysOfWeek getRepeatDays() {
        return mRepeatDays;
    }

    /**
     * Gets the internal alarm time for this alarm clock.
     * @return The internal alarm time.
     */
    public Calendar getAlarmTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, mAlarmDay);
        calendar.set(Calendar.HOUR_OF_DAY, mAlarmHour);
        calendar.set(Calendar.MINUTE, mAlarmMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Gets the internal alarm time for this alarm clock in milliseconds since the "epoch".
     * @return The internal alarm time in milliseconds since the "epoch".
     */
    public long getAlarmTimeMillis() {
        return getAlarmTime().getTimeInMillis();
    }

    /**
     *
     * @return
     */
    public Calendar getArrivalTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, mArrivalDay);
        calendar.set(Calendar.HOUR_OF_DAY, mArrivalHour);
        calendar.set(Calendar.MINUTE, mArrivalMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     *
     * @return
     */
    public long getArrivalTimeMillis() {
        return getArrivalTime().getTimeInMillis();
    }

    public int getPrepHours() {
        return mPrepHour;
    }

    public int getPrepMinutes() {
        return mPrepMinute;
    }

    /**
     *
     * @return
     */
    public long getPrepTimeMillis() {
        return mPrepTime;
    }

    /**
     * Tells you whether the alarm is currently set or not. It will return false if the alarm has
     * already gone off or has been explicitly canceled.
     * @return Whether the alarm is set or not.
     */
    public boolean isAlarmOn() {
        return mAlarmSet;
    }

    /**
     *
     * @return
     */
    public boolean isGedderOn() {
        return mGedderSet;
    }

    public boolean isGedderEligible() {
        return !mOriginAddress.equals("") && !mDestinationAddress.equals("");
    }

    /**
     *
     * @return
     */
    public long getTimeUntilAlarm() {
        return mAlarmTime - System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mUuid);
        dest.writeInt(this.mRequestCode);
        dest.writeString(this.mOriginId);
        dest.writeString(this.mOriginAddress);
        dest.writeString(this.mDestinationId);
        dest.writeString(this.mDestinationAddress);
        dest.writeString(this.mTravelMode.name());
        dest.writeString(this.mTransitMode.name());
        dest.writeInt(this.mRepeatDays.getCoded());
        dest.writeInt(this.mAlarmDay);
        dest.writeInt(this.mAlarmHour);
        dest.writeInt(this.mAlarmMinute);
        dest.writeLong(this.mAlarmTime);
        dest.writeInt(this.mArrivalDay);
        dest.writeInt(this.mArrivalHour);
        dest.writeInt(this.mArrivalMinute);
        dest.writeLong(this.mArrivalTime);
        dest.writeInt(this.mPrepHour);
        dest.writeInt(this.mPrepMinute);
        dest.writeLong(this.mPrepTime);
        dest.writeByte(this.mAlarmSet ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mGedderSet ? (byte) 1 : (byte) 0);
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcel constructor.
     * @param in The parcel to read.
     */
    protected AlarmClock(Parcel in) {
        this.mUuid               = (UUID) in.readSerializable();
        this.mRequestCode        = in.readInt();
        this.mOriginId           = in.readString();
        this.mOriginAddress      = in.readString();
        this.mDestinationId      = in.readString();
        this.mDestinationAddress = in.readString();
        this.mTravelMode         = TravelMode.valueOf(in.readString());
        this.mTransitMode        = TransitMode.valueOf(in.readString());
        this.mRepeatDays         = new DaysOfWeek(in.readInt());
        this.mAlarmDay           = in.readInt();
        this.mAlarmHour          = in.readInt();
        this.mAlarmMinute        = in.readInt();
        this.mAlarmTime          = in.readLong();
        this.mArrivalDay         = in.readInt();
        this.mArrivalHour        = in.readInt();
        this.mArrivalMinute      = in.readInt();
        this.mArrivalTime        = in.readLong();
        this.mPrepHour           = in.readInt();
        this.mPrepMinute         = in.readInt();
        this.mPrepTime           = in.readLong();
        this.mAlarmSet           = in.readByte() != 0;
        this.mGedderSet          = in.readByte() != 0;
    }

    public static final Creator<AlarmClock> CREATOR = new Creator<AlarmClock>() {
        @Override
        public AlarmClock createFromParcel(Parcel source) {
            return new AlarmClock(source);
        }

        @Override
        public AlarmClock[] newArray(int size) {
            return new AlarmClock[size];
        }
    };
}
