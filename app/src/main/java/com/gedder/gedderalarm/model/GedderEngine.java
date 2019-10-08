/*
 * USER: mslm
 * DATE: 3/27/17
 */

package com.gedder.gedderalarm.model;


import android.os.Bundle;
import android.util.Log;

import com.gedder.gedderalarm.google.JsonParser;
import com.gedder.gedderalarm.google.UrlGenerator;
import com.gedder.gedderalarm.util.HttpRequest;
import com.gedder.gedderalarm.util.except.GoogleMapsAPIException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * <p>The engine goes through the following pipeline:</p>
 *
 * <ol>
 *     <li>Generate a URL to query Google Maps API using (Origin, Destination, Arrival Time,
 *     Preparation Time, Upper Bound Time, API key) as input.</li>
 *     <li>Query Google Maps API using the generated URL.</li>
 *     <li>Store the response JSON and parse it.</li>
 *     <li>Send back relevant data.</li>
 * </ol><br>
 *
 * <p>It essentially serves as a service to anything looking to utilize this flow.</p>
 */

public final class GedderEngine {
    private static final String TAG = GedderEngine.class.getSimpleName();

    public static final String RESULT_DURATION = "__GEDDER_ENGINE_RESULT_DURATION__";
    public static final String RESULT_DURATION_IN_TRAFFIC =
            "__GEDDER_ENGINE_RESULT_DURATION_IN_TRAFFIC__";
    public static final String RESULT_WARNINGS = "__GEDDER_ENGINE_WARNINGS__";

    // We get this in some secret way. For development, just keep a local API key in a file that
    // is ignored by git.
    private static final String sApiKey = "AIzaSyAdVG58cxY0i9JAjGaGM4yqDeSlD1BUoik";

    private GedderEngine() {}

    /**
     * <p>Flows through the following pipeline once:</p>
     *
     * <ol>
     *     <li>Generate URL.</li>
     *     <li>Query API.</li>
     *     <li>Parse the response.</li>
     *     <li>Return data of interest.</li>
     * </ol>
     * @param origin            The place the user is leaving from.
     * @param destination       The place the user wants to get to.
     * @param arrivalTime       The time the user needs to get to their destination.
     * @return A {@link Bundle} containing relevant data from the pipeline. You can extract this
     * data by using keys like {@link #RESULT_DURATION}, {@link #RESULT_WARNINGS}, etc..
     */
    public static Bundle start(String origin, String destination, long arrivalTime) {
        // TODO: Fix up some of the "style" of the code.
        // TODO: Move parts to different functions.
        // TODO: Make another `start` function with departureTime instead of arrival (use prep + upperBound).

        String            url;
        String            json = "";
        int               duration;
        int               durationInTraffic;
        ArrayList<String> warnings;

        // Generate URL
        url = new UrlGenerator.UrlBuilder(origin, destination, sApiKey)
                .arrivalTime(arrivalTime)
                .build().toString();

        // Query API
        try {
            json = new HttpRequest().execute(url).get();
        } catch (ExecutionException e) {
            Log.e(TAG, "Couldn't retrieve JSON from Google Maps API.");
        } catch (InterruptedException e) {
            Log.e(TAG, "Query to Google Maps API taking too long.");
        }

        // Parse the relevant variables.
        JsonParser jp = new JsonParser(json);
        if (!jp.errorMessage().equals("")) throw new GoogleMapsAPIException(jp.errorMessage());
        duration = jp.duration();
        durationInTraffic = jp.durationInTraffic();
        warnings = jp.warnings();

        Bundle results = new Bundle();
        results.putInt(RESULT_DURATION, duration);
        results.putInt(RESULT_DURATION_IN_TRAFFIC, durationInTraffic);
        results.putSerializable(RESULT_WARNINGS, warnings);
        return results;
    }
}
