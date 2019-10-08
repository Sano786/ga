/*
 * USER: mslm
 * DATE: 3/26/17
 */

package com.gedder.gedderalarm;

import android.app.Application;
import android.content.Context;

/**
 * <p>Used solely as a way to get application context for alarms.</p>
 */

public class GedderAlarmApplication extends Application {
    private static final String TAG = GedderAlarmApplication.class.getSimpleName();

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        GedderAlarmApplication.mContext = getApplicationContext();
    }

    /**
     *
     * @return
     */
    public static Context getAppContext() {
        return GedderAlarmApplication.mContext;
    }
}
