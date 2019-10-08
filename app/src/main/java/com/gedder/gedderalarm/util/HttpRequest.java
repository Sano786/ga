/*
 * USER: Mike
 * DATE: 3/18/2017
 */

package com.gedder.gedderalarm.util;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>Class to request data from Google Maps Directions API.</p>
 * <p>Example usage:</p>
 *
 * <code><pre>
 * // Use urlGenerator class to create/encode url.
 * url.build();
 * String mainTest = new HTTPRequest().execute(url).get();
 * JsonParser test = new JsonParser(mainTest);
 * </pre></code>
 */

public class HttpRequest extends AsyncTask<String, String, String> {
    private static final String TAG = HttpRequest.class.getSimpleName();

    private HttpURLConnection mUrlConnection = null;

    /**
     * Sends a request to the Google website.
     * @param urlTest Url to be sent out.
     * @return A string builder of jsonResults.
     */
    protected String doInBackground(String... urlTest) {

        this.setURL(urlTest[0]);

        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(urlTest[0]);

            Log.i(TAG, "Querying " + url.toString());

            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setRequestMethod("GET");
            mUrlConnection.setDoOutput(true);
            mUrlConnection.setDoInput(true);

            InputStream is = mUrlConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            // Load the results into a StringBuilder
            String line;
            while ((line = rd.readLine()) != null) {
                jsonResults.append(line);
                jsonResults.append("\r");
            }

            rd.close();

        } catch (MalformedURLException e) {
            Log.e(TAG, "URL provided is of an incorrect format.");
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong in handling input from query.");
        } finally {
            if (mUrlConnection != null) {
                mUrlConnection.disconnect();
            }
        }
        return jsonResults.toString();
    }

    /**
     * Sets a String to URL, exception is thrown if not an url.
     * @param url Url to be sent out.
     */
    private void setURL(String url) {
        try {
            URL setURL = new URL(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}