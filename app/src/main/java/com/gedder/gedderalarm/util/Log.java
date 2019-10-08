/*
 * USER: mslm
 * DATE: 3/1/2017
 */

package com.gedder.gedderalarm.util;

/**
 * <p><a href="http://stackoverflow.com/a/4592958">Online source.</a></p>
 *
 * <p>A wrapper on android.util.Log's functionality, to make it easier to disable/enable logging
 * (i.e. for pushing to production, or debugging).</p>
 *
 * @see android.util.Log
 */

public class Log {
    private static final boolean LOG = true;

    /** @see android.util.Log */
    public static void i(String tag, String string) {
        if (LOG) android.util.Log.i(tag, string);
    }

    /** @see android.util.Log */
    public static void e(String tag, String string) {
        if (LOG) android.util.Log.e(tag, string);
    }

    /** @see android.util.Log */
    public static void d(String tag, String string) {
        if (LOG) android.util.Log.d(tag, string);
    }

    /** @see android.util.Log */
    public static void v(String tag, String string) {
        if (LOG) android.util.Log.v(tag, string);
    }

    /** @see android.util.Log */
    public static void w(String tag, String string) {
        if (LOG) android.util.Log.w(tag, string);
    }

    /** @see android.util.Log */
    public static void wtf(String tag, String string) {
        if (LOG) android.util.Log.wtf(tag, string);
    }
}
