/*
 * USER: mslm
 * DATE: 3/28/17
 */

package com.gedder.gedderalarm.util;

import java.util.Calendar;

/** Contains general time-related utilities. */

public final class TimeUtilities {
    private static final String TAG = TimeUtilities.class.getSimpleName();

    private static final int HOUR_PER_DAY = 24;

    private static final int MINUTE_PER_HOUR = 60;
    private static final int MINUTE_PER_DAY = HOUR_PER_DAY*MINUTE_PER_HOUR;

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE*SECONDS_PER_MINUTE;
    private static final int SECONDS_PER_DAY = MINUTE_PER_DAY*SECONDS_PER_MINUTE;

    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = SECONDS_PER_MINUTE*MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = SECONDS_PER_HOUR*MILLIS_PER_SECOND;
    private static final long MILLIS_PER_DAY = SECONDS_PER_DAY*MILLIS_PER_SECOND;

    private TimeUtilities() {}

    /**
     * Gets the time until some specified day, hour, and minute combination, from today. Recommended
     * to instead use {@link #getMillisUntil(Calendar)} to avoid {@link IllegalArgumentException}.
     * @param day       A day number where 1 = Sunday, 2 = Monday, ..., 7 = Saturday.
     * @param hour      An hour between 0 and 23 for the 24-hour clock.
     * @param minute    A minute between 0 and 59.
     * @return The milliseconds from now until the specified time in the future..
     */
    public static long getMillisUntil(int day, int hour, int minute) {
        if (day < 1 || day > 7 || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Incorrect parameter ranges in call to"
                    + " getMillisUntil(int, int, int): "
                    + "day = "      + day
                    + ", hour = "   + hour
                    + ", minute = " + minute);
        }

        Calendar calendar = Calendar.getInstance();
        long now = calendar.get(Calendar.DAY_OF_YEAR)*MILLIS_PER_DAY
                 + calendar.get(Calendar.HOUR_OF_DAY)*MILLIS_PER_HOUR
                 + calendar.get(Calendar.MINUTE)*MILLIS_PER_MINUTE;
        long then = day*MILLIS_PER_DAY
                  + hour*MILLIS_PER_HOUR
                  + minute*MILLIS_PER_MINUTE;
        return then - now;
    }

    /**
     * Gets the time until some specified day, hour, and minute combination, from today.
     * @param future A calendar containing the day, hour, and minute to calculate the number of
     *               milliseconds up to.
     * @return The milliseconds from now until the specified time in the future.
     */
    public static long getMillisUntil(Calendar future) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.get(Calendar.DAY_OF_YEAR)*MILLIS_PER_DAY
                 + calendar.get(Calendar.HOUR_OF_DAY)*MILLIS_PER_HOUR
                 + calendar.get(Calendar.MINUTE)*MILLIS_PER_MINUTE;
        long then = future.get(Calendar.DAY_OF_YEAR)*MILLIS_PER_DAY
                  + future.get(Calendar.HOUR_OF_DAY)*MILLIS_PER_HOUR
                  + future.get(Calendar.MINUTE)*MILLIS_PER_MINUTE;
        return then - now;
    }

    /**
     * Gets the number of milliseconds one can count in the hour and minute inputs. Equivalent to
     * counting up to that time since midnight.
     * @param hour
     * @param minute
     * @return The amount of milliseconds in the hour and minute input.
     */
    public static long getMillisIn(int hour, int minute) {
        return getMillisSinceMidnight(hour, minute);
    }

    /**
     * Gets the time since midnight up to hour:minute.
     * @param hour      The hour to count up to since midnight.
     * @param minute    The minute to count up to since midnight.
     * @return The time since midnight up to hour:minute in milliseconds.
     */
    public static long getMillisSinceMidnight(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Incorrect parameter ranges in call to"
                    + " getMillisSinceMidnight(int, int): "
                    + "hour = "     + hour
                    + ", minute = " + minute);
        }
        return hour*MILLIS_PER_HOUR + minute*MILLIS_PER_MINUTE;
    }

    /**
     * Gets the milliseconds since midnight up to now (on function invocation).
     * @return the milliseconds since midnight up to now.
     */
    public static long getMillisSinceMidnight() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR)*MILLIS_PER_HOUR + cal.get(Calendar.MINUTE)*MILLIS_PER_MINUTE;
    }

    /**
     *
     * @param millis
     * @return
     */
    public static double millisToSeconds(long millis) {
        return ((double) millis) / secondsToMillis(1);
    }

    /**
     *
     * @param millis
     * @return
     */
    public static double millisToMinutes(long millis) {
        return ((double) millis) / minutesToMillis(1);
    }

    /**
     *
     * @param millis
     * @return
     */
    public static double millisToHours(long millis) {
        return ((double) millis) / hoursToMillis(1);
    }

    /**
     *
     * @param millis
     * @return
     */
    public static double millisToDays(long millis) {
        return ((double) millis) / daysToMillis(1);
    }

    /**
     *
     * @param seconds
     * @return
     */
    public static long secondsToMillis(int seconds) {
        return seconds*MILLIS_PER_SECOND;
    }

    /**
     *
     * @param seconds
     * @return
     */
    public static double secondsToMinutes(int seconds) {
        return ((double) seconds) / minutesToSeconds(1);
    }

    /**
     *
     * @param seconds
     * @return
     */
    public static double secondsToHours(int seconds) {
        return ((double) seconds) / hoursToSeconds(1);
    }

    /**
     *
     * @param seconds
     * @return
     */
    public static double secondsToDays(int seconds) {
        return ((double) seconds) / daysToSeconds(1);
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static long minutesToMillis(int minutes) {
        return minutes*MILLIS_PER_MINUTE;
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static long minutesToSeconds(int minutes) {
        return minutes*SECONDS_PER_MINUTE;
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static double minutesToHours(int minutes) {
        return ((double) minutes) / hoursToMinutes(1);
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static double minutesToDays(int minutes) {
        return ((double) minutes) / daysToMinutes(1);
    }

    /**
     * Gets the number of milliseconds in the specified number of hours.
     * @param hours The number of hours to convert to milliseconds.
     * @return The number of milliseconds in hours.
     */
    public static long hoursToMillis(int hours) {
        return hours*MILLIS_PER_HOUR;
    }

    /**
     * Gets the number of seconds in the specified number of hours.
     * @param hours The number of hours to convert to seconds.
     * @return The number of seconds in hours.
     */
    public static long hoursToSeconds(int hours) {
        return hours*SECONDS_PER_HOUR;
    }

    /**
     *
     * @param hours
     * @return
     */
    public static long hoursToMinutes(int hours) {
        return hours*MINUTE_PER_HOUR;
    }

    /**
     *
     * @param hours
     * @return
     */
    public static double hoursToDays(int hours) {
        return ((double) hours) / daysToHours(1);
    }

    /**
     * Gets the number of milliseconds in the specified number of days.
     * @param days The number of days to convert to milliseconds.
     * @return The number of milliseconds in days.
     */
    public static long daysToMillis(int days) {
        return days*MILLIS_PER_DAY;
    }

    /**
     * Gets the number of seconds in the specified number of days.
     * @param days The number of days to convert to seconds.
     * @return The number of seconds in days.
     */
    public static long daysToSeconds(int days) {
        return days*SECONDS_PER_DAY;
    }

    /**
     *
     * @param days
     * @return
     */
    public static long daysToMinutes(int days) {
        return days*MINUTE_PER_DAY;
    }

    /**
     *
     * @param days
     * @return
     */
    public static long daysToHours(int days) {
        return days*HOUR_PER_DAY;
    }
}
