/*
 * USER: Mike, mslm
 * DATE: 3/4/2017
 */

package com.gedder.gedderalarm.google;

import com.gedder.gedderalarm.util.Log;
import com.gedder.gedderalarm.util.TimeUtilities;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * <p>Class to generate URLs for Google Maps API.</p>
 *
 * <p>Example usage:</p>
 *
 * <code><pre>
 * public UrlGenerator url = new UrlGenerator.UrlBuilder("mOrigin", "mDestination", "mApiKey") // required
 *                      .mArrivalTime("mArrivalTime")     // optional
 *                      .mDepartureTime("mDepartureTime") // optional
 *                      .mTravelMode("mTravelMode")       // optional
 *                      .mAvoidToll()                    // optional
 *                      .mAvoidHighways()                // optional
 *                      .build(); // must call this to get back a Url
 * </pre></code>
 */

public class UrlGenerator {
    private static final String TAG = UrlGenerator.class.getSimpleName();

    private final String  mBaseUrl = "https://maps.googleapis.com/maps/api/directions/json?";
    private final String  mOrigin;            // required
    private final String  mDestination;       // required
    private final String  mApiKey;            // required
    private final String  mArrivalTime;       // optional
    private final String  mDepartureTime;     // optional
    private final String  mTravelMode;        // optional
    private final String  mTransitMode;       // optional
    private final boolean mAvoidToll;         // optional
    private final boolean mAvoidHighways;     // optional

    private String url = mBaseUrl;

    /**
     * Builder constructor.
     * @param builder The builder to base the URL off of.
     */
    private UrlGenerator(UrlBuilder builder) {
        mOrigin          = builder.mOrigin;
        mDestination     = builder.mDestination;
        mApiKey          = builder.mApiKey;
        mArrivalTime     = builder.mArrivalTime;
        mDepartureTime   = builder.mDepartureTime;
        mTravelMode      = builder.mTravelMode;
        mTransitMode     = builder.mTransitMode;
        mAvoidToll       = builder.mAvoidToll;
        mAvoidHighways   = builder.mAvoidHighways;
        addOrigin       (mOrigin);
        url += "&";
        addDestination  (mDestination);
        url += "&";
        addArrivalTime  (mArrivalTime);
        url += "&";
        if (addDepartureTime(mDepartureTime)) url += "&";
        if (addTravelMode   (mTravelMode))    url += "&";
        if (addTransitMode  (mTransitMode))   url += "&";
        if (addAvoidToll    (mAvoidToll))     url += "&";
        if (addAvoidHighways(mAvoidHighways)) url += "&";
        addApiKey       (mApiKey);
    }

    /**
     * Gets the base of the URL (regardless of what was built, it's always the same).
     * @return The base url.
     */
    public String getBaseUrl() {
        return mBaseUrl;
    }

    /**
     *
     * @return The mOrigin's place ID.
     */
    public String getOrigin() {
        return mOrigin;
    }

    /**
     *
     * @return The mDestination's place ID.
     */
    public String getDestination() {
        return mDestination;
    }

    /**
     *
     * @return The API key associated with this URL.
     */
    public String getApiKey() {
        return mApiKey;
    }

    /**
     *
     * @return The arrival time in milliseconds since the epoch.
     */
    public String getArrivalTime() {
        return mArrivalTime;
    }

    /**
     *
     * @return The departure time in milliseconds since the epoch.
     */
    public String getDepartureTime() {
        return mDepartureTime;
    }

    /**
     *
     * @return The mode of travel.
     */
    public String getTravelMode() {
        return mTravelMode;
    }

    public String getTransitMode() {
        return mTransitMode;
    }

    /**
     *
     * @return Whether to avoid tolls.
     */
    public boolean avoidToll() {
        return mAvoidToll;
    }

    /**
     *
     * @return Whether to avoid highways.
     */
    public boolean avoidHighways() {
        return mAvoidHighways;
    }

    /**
     *
     * @param origin
     */
    private void addOrigin(String origin) {
        url += "origin=place_id:" + origin;
    }

    /**
     *
     * @param destination
     */
    private void addDestination(String destination) {
        url += "destination=place_id:" + destination;
    }

    /**
     *
     * @param apiKey
     */
    private void addApiKey(String apiKey) {
        url += "key=" + apiKey;
    }

    /**
     *
     * @param arrivalTime
     */
    private boolean addArrivalTime(String arrivalTime) {
        if (arrivalTime != null) {
            url += "arrival_time=" + arrivalTime;
            return true;
        }
        return false;
    }

    /**
     *
     * @param departureTime
     */
    private boolean addDepartureTime(String departureTime) {
        if (departureTime != null) {
            url += "departure_time=" + departureTime;
            return true;
        }
        return false;
    }

    /**
     *
     * @param travelMode
     */
    private boolean addTravelMode(String travelMode) {
        if (travelMode != null) {
            url += "mode=" + travelMode;
            return true;
        }
        return false;
    }

    private boolean addTransitMode(String transitMode) {
        if (transitMode != null) {
            url += "transit_mode=" + transitMode;
            return true;
        }
        return false;
    }

    /**
     *
     * @param avoidToll
     */
    private boolean addAvoidToll(boolean avoidToll) {
        if (avoidToll) {
            url += "avoid=tolls";
            return true;
        }
        return false;
    }

    /**
     *
     * @param avoidHighways
     */
    private boolean  addAvoidHighways(boolean avoidHighways) {
        if (avoidHighways) {
            url += "avoid=highways";
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return url;
    }

    /**
     *
     * @return
     */
    public URL getUrl() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException");
            return null;
        }
    }

    /** Builds a URL and instantiates it with {@link #build()}. */
    public static class UrlBuilder {
        private static final String SUB_TAG = UrlBuilder.class.getSimpleName();

        private String  mOrigin;        // required
        private String  mDestination;   // required
        private String  mApiKey;        // required
        private String  mArrivalTime;   // optional
        private String  mDepartureTime; // optional
        private String  mTravelMode;    // optional
        private String  mTransitMode;   // optional
        private boolean mAvoidToll;     // optional
        private boolean mAvoidHighways; // optional

        /**
         * Initializes required parameters for the URL.
         * @param origin        The starting location.
         * @param destination   The ending location.
         * @param apiKey        The API key with which to query Google Maps API.
         * mDestination, and API key in the HTTP request, at the least.
         */
        public UrlBuilder(String origin, String destination, String apiKey) {
            try {
                if (origin == null || destination == null
                        || origin.equals("") || destination.equals("")) {
                    throw new IllegalArgumentException("Incorrect parameter in URLBuilder: "
                            + "origin = "        + origin
                            + ", destination = " + destination
                            + ", apiKey = "      + apiKey);
                }
                mOrigin      = URLEncoder.encode(origin, "UTF-8");
                mDestination = URLEncoder.encode(destination, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.wtf(SUB_TAG, "UTF-8 is apparently unsupported.");
            }
            mApiKey = apiKey;
        }

        /**
         * Set the arrival time for this URL.
         * @param arrivalTime Arrival time in a milliseconds string since the epoch.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder arrivalTime(String arrivalTime) {
            mArrivalTime = String.valueOf(TimeUtilities.millisToSeconds(Long.valueOf(arrivalTime)));
            return this;
        }

        /**
         * Set the arrival time for this URL.
         * @param arrivalTime Arrival time in milliseconds since the epoch.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder arrivalTime(long arrivalTime) {
            mArrivalTime = String.valueOf(TimeUtilities.millisToSeconds(arrivalTime));
            return this;
        }

        /**
         * Set the departure time for this URL.
         * @param departureTime Departure time in a milliseconds string since the epoch.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder departureTime(String departureTime) {
            mDepartureTime = String.valueOf(
                    TimeUtilities.millisToSeconds(Long.valueOf(departureTime)));
            return this;
        }

        /**
         * Set the departure time for this URL.
         * @param departureTime Departure time in milliseconds since the epoch.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder departureTime(long departureTime) {
            mDepartureTime = String.valueOf(TimeUtilities.millisToSeconds(departureTime));
            return this;
        }

        /**
         * Set the mode of travel for this URL.
         * @param travelMode
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder travelMode(String travelMode) {
            if (!isAvailableTravelMode(travelMode)) {
                throw new IllegalArgumentException(
                        SUB_TAG + "::UrlBuilder::mTravelMode: travel mode not available.");
            }
            mTravelMode = travelMode;
            return this;
        }

        public UrlBuilder transitMode(String transitMode) {
            if (!isAvailableTransitMode(transitMode)) {
                throw new IllegalArgumentException(
                        SUB_TAG + "::UrlBuilder::mTransitMode: transit mode not available.");
            }
            mTransitMode = transitMode;
            return this;
        }

        /**
         * Set whether the query should return paths where we avoid tolls.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder avoidToll() {
            mAvoidToll = true;
            return this;
        }

        /**
         * Set whether the query should return paths where we avoid highways.
         * @return UrlBuilder to chain method calls off of.
         */
        public UrlBuilder avoidHighways() {
            mAvoidHighways = true;
            return this;
        }

        /**
         * Builds the URL.
         * @return A built UrlGenerator class.
         */
        public UrlGenerator build() {
            return new UrlGenerator(this);
        }

        /**
         * Convenience function to check whether the mode string is valid.
         * @param mode The intended mode of travel.
         * @return whether the intended mode of travel is valid for Google Maps API.
         */
        private boolean isAvailableTravelMode(String mode) {
            return !mode.equals(TravelMode.DRIVING.name().toLowerCase())   &&
                   !mode.equals(TravelMode.BICYCLING.name().toLowerCase()) &&
                   !mode.equals(TravelMode.WALKING.name().toLowerCase())   &&
                   !mode.equals(TravelMode.TRANSIT.name().toLowerCase());
        }

        private boolean isAvailableTransitMode(String mode) {
            return !mode.equals(TransitMode.BUS.name().toLowerCase())    &&
                   !mode.equals(TransitMode.SUBWAY.name().toLowerCase()) &&
                   !mode.equals(TransitMode.TRAIN.name().toLowerCase())  &&
                   !mode.equals(TransitMode.TRAM.name().toLowerCase())   &&
                   !mode.equals(TransitMode.RAIL.name().toLowerCase());
        }
    }
}