/*
 * USER: mslm, Mike
 * DATE: March 8th, 2017
 */

package com.gedder.gedderalarm.google;

import com.gedder.gedderalarm.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * <p>Class to parse the JSON received from Google Maps API.</p>
 *
 * <p>Example usage:</p>
 *
 * <code><pre>
 * public String json = "put json string here";
 * public JsonParser jsonParser = new JsonParser(json);
 * private int duration = jsonParser.duration();
 * private String origin = jsonParser.origin();
 * private String destination = jsonParser.destination();
 * private String route2_origin = jsonParser.origin(1);
 * private String route2_destination = jsonParser.destination(1);
 * </pre></code>
 */

public class JsonParser {
    // For mslm:
    // TODO: Add origin() and destination() functionality.
    // TODO: Add originLatitude() and destinationLatitude() functionality.
    // TODO: Add originLongitude() and destinationLongitude() functionality.

    private static final String TAG = JsonParser.class.getSimpleName();

    private String mJson;
    private JSONObject mJsonObj;

    /**
     * Initializes JsonParser object using a valid JSON string.
     * @param json The JSON string to parse.
     */
    public JsonParser(String json) {
        mJson = json;

        try {
            mJsonObj = new JSONObject(this.mJson);
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in constructor " + TAG + "::JsonParser(String).");
            mJson = "";
            mJsonObj = null;
        }
    }

    /**
     * Grabs json['routes'][0]['legs'][0]['duration']['value'].
     * @return duration of travel in seconds.
     */
    public int duration() {
        return duration(0, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][0]['duration']['value'].
     * @param route route, if multiple. Starts from 0.
     * @return duration of travel in seconds.
     */
    public int duration(int route) {
        return duration(route, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][leg]['duration']['value'].
     * @param route which route, if multiple. Starts from 0.
     * @param leg   which leg, if multiple. Starts from 0.
     * @return duration of travel in seconds.
     */
    public int duration(int route, int leg) {
        JSONArray routes;
        JSONObject route_number;
        JSONArray legs;
        JSONObject leg_number;
        JSONObject durationObj;
        int duration;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            legs = route_number.getJSONArray("legs");
            leg_number = legs.getJSONObject(leg);
            durationObj = leg_number.getJSONObject("duration");
            duration = durationObj.getInt("value");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::duration.");
            return -1;
        }

        return duration;
    }

    /**
     * Grabs json['routes'][0]['legs'][0]['distance']['value'].
     * @return distance of travel in meters.
     */
    public int distance() {
        return distance(0, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][0]['distance']['value'].
     * @param route which route, if multiple. Starts from 0.
     * @return distance of travel in meters.
     */
    public int distance(int route) {
        return distance(route, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][leg]['distance']['value'].
     * @param route which route, if multiple. Starts from 0.
     * @param leg   which leg, if multiple. Starts from 0.
     * @return distance of travel in meters.
     */
    public int distance(int route, int leg) {
        JSONArray routes;
        JSONObject route_number;
        JSONArray legs;
        JSONObject leg_number;
        int distance;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            legs = route_number.getJSONArray("legs");
            leg_number = legs.getJSONObject(leg);
            distance = leg_number.getInt("value");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::distance.");
            return -1;
        }

        return distance;
    }

    /**
     * Grabs json['routes'][0]['legs'][0]['duration_in_traffic']['value'].
     * <p>Only exists if the request specified a traffic model.</p>
     * @return duration in traffic in seconds.
     */
    public int durationInTraffic() {
        return durationInTraffic(0, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][0]['duration_in_traffic']['value'].
     * <p>Only exists if the request specified a traffic model.</p>
     * @param route which route, if multiple. Starts from 0.
     * @return duration in traffic in seconds.
     */
    public int durationInTraffic(int route) {
        return durationInTraffic(route, 0);
    }

    /**
     * Grabs json['routes'][route]['legs'][leg]['duration_in_traffic']['value'].
     * <p>Only exists if the request specified a traffic model.</p>
     * @param route which route, if multiple. Starts from 0.
     * @param leg   which leg, if multiple. Starts from 0.
     * @return duration in traffic in seconds.
     */
    public int durationInTraffic(int route, int leg) {
        JSONArray routes;
        JSONObject route_number;
        JSONArray legs;
        JSONObject leg_number;
        JSONObject duration_in_traffic;
        int value;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            legs = route_number.getJSONArray("legs");
            leg_number = legs.getJSONObject(leg);
            duration_in_traffic = leg_number.getJSONObject("duration_in_traffic");
            value = duration_in_traffic.getInt("value");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::durationInTraffic.");
            return -1;
        }

        return value;
    }

    /**
     * Grabs json['routes'][0]['warnings'] elements and puts them in a ArrayList of Strings.
     * @return an ArrayList<String> object containing all warnings.
     */
    public ArrayList<String> warnings() {
        return warnings(0);
    }

    /**
     * Grabs json['routes'][route]['warnings'] elements and puts them in a ArrayList of Strings.
     * @param route which route, if multiple. Starts from 0.
     * @return an ArrayList<String> object containing all warnings.
     */
    public ArrayList<String> warnings(int route) {
        JSONArray routes;
        JSONObject route_number;
        JSONArray warnings;
        ArrayList<String> warningsList = new ArrayList<>();

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            warnings = route_number.getJSONArray("warnings");
            for (int i = 0; i < warnings.length(); i++) {
                warningsList.add(warnings.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::warnings.");
            return null;
        }

        return warningsList;
    }

    /**
     * Grabs json['routes'][0]['copyrights'].
     * @return a String containing the copyright information.
     */
    public String copyrights() {
        return copyrights(0);
    }

    /**
     * Grabs json['routes'][route]['copyrights'].
     * @param route which route, if multiple. Starts from 0.
     * @return a String containing the copyright information.
     */
    public String copyrights(int route) {
        JSONArray routes;
        JSONObject route_number;
        String copyrights;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            copyrights = route_number.getString("copyrights");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::copyrights.");
            return null;
        }

        return copyrights;
    }

    /**
     * Grabs json['routes'][0]['summary'].
     * @return a String containing summary information for the route.
     */
    public String summary() {
        return summary(0);
    }

    /**
     * Grabs json['routes'][route]['summary'].
     * @param route which route, if multiple. Starts from 0.
     * @return a String containing summary information for the route.
     */
    public String summary(int route) {
        JSONArray routes;
        JSONObject route_number;
        String summary;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            summary = route_number.getString("summary");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::summary.");
            return null;
        }

        return summary;
    }

    /**
     * Grabs json['routes'][0]['fare']['currency'].
     * @return the ISO 4217 currency code that the fare is expressed in.
     */
    public String fareCurrency() {
        return fareCurrency(0);
    }

    /**
     * Grabs json['routes'][route]['fare']['currency'].
     * @param route which route, if multiple. Starts from 0.
     * @return the ISO 4217 currency code that the fare is expressed in.
     */
    public String fareCurrency(int route) {
        JSONArray routes;
        JSONObject route_number;
        JSONObject fare;
        String currency;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            fare = route_number.getJSONObject("fare");
            currency = fare.getString("currency");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::fareCurrency.");
            return null;
        }

        return currency;
    }

    /**
     * Grabs json['routes'][0]['fare']['value'].
     * @return the total fare amount in the currency specified by fareCurrency().
     */
    public int fare() {
        return fare(0);
    }

    /**
     * Grabs json['routes'][route]['fare']['value'].
     * @param route which route, if multiple. Starts from 0.
     * @return the total fare amount in the currency specified by fareCurrency(). -1 if there was a
     * JSONException.
     */
    public int fare(int route) {
        JSONArray routes;
        JSONObject route_number;
        JSONObject fareObj;
        int fare;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            fareObj = route_number.getJSONObject("fare");
            fare = fareObj.getInt("value");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::fare.");
            return -1;
        }

        return fare;
    }

    /**
     * Grabs json['status'].
     * @return status number of the request, corresponding to com.gedder.gedderalarm.util.JSONStatus
     * enumerations.
     */
    public JsonStatus status() {
        JsonStatus code;
        String status;

        try {
            status = mJsonObj.getString("status");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::status.");
            return null;
        }

        switch (status) {
            case "OK":
                code = JsonStatus.OK;
                break;
            case "NOT_FOUND":
                code = JsonStatus.NOT_FOUND;
                break;
            case "ZERO_RESULTS":
                code = JsonStatus.ZERO_RESULTS;
                break;
            case "MAX_WAYPOINTS_EXCEEDED":
                code = JsonStatus.MAX_WAYPOINTS_EXCEEDED;
                break;
            case "MAX_ROUTE_LENGTH_EXCEEDED":
                code = JsonStatus.MAX_ROUTE_LENGTH_EXCEEDED;
                break;
            case "INVALID_REQUEST":
                code = JsonStatus.INVALID_REQUEST;
                break;
            case "OVER_QUERY_LIMIT":
                code = JsonStatus.OVER_QUERY_LIMIT;
                break;
            case "REQUEST_DENIED":
                code = JsonStatus.REQUEST_DENIED;
                break;
            default:
                code = JsonStatus.UNKNOWN_ERROR;
                break;
        }

        return code;
    }

    /**
     * Grabs json['error_message']. Only exists if json['status'] != "OK".
     * @return error message string.
     */
    public String errorMessage() {
        String error;

        try {
            error = mJsonObj.getString("error_message");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::errorMessage.");
            return "";
        } catch (NullPointerException e) {
            return "";
        }

        return error;
    }

    /**
     * Grabs json['available_travel_modes']. Only exists if a request specifies a travel mode and
     * gets no results.
     * @return an ArrayList<String> object containing all available travel modes.
     */
    public ArrayList<String> availableTravelModes() {
        JSONArray travel_modes;
        ArrayList<String> modes = new ArrayList<>();

        try {
            travel_modes = mJsonObj.getJSONArray("available_travel_modes");
            for (int i = 0; i < travel_modes.length(); i++) {
                modes.add(travel_modes.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::availableTravelModes.");
            return null;
        }

        return modes;
    }

    /**
     *
     * @param route
     * @param leg
     * @param step
     * @return
     */
    public TravelMode travelMode(int route, int leg, int step) {
        JSONArray routes;
        JSONObject route_number;
        JSONArray legs;
        JSONObject leg_number;
        JSONArray steps;
        JSONObject step_number;
        String travelModeStr;
        TravelMode travelMode;

        try {
            routes = mJsonObj.getJSONArray("routes");
            route_number = routes.getJSONObject(route);
            legs = route_number.getJSONArray("legs");
            leg_number = legs.getJSONObject(leg);
            steps = leg_number.getJSONArray("steps");
            step_number = steps.getJSONObject(step);
            travelModeStr = step_number.getString("travel_mode");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string in function " + TAG + "::travelMode.");
            return null;
        }

        switch (travelModeStr) {
            case "DRIVING":
                travelMode = TravelMode.DRIVING;
                break;
            case "WALKING":
                travelMode = TravelMode.WALKING;
                break;
            case "BICYCLING":
                travelMode = TravelMode.BICYCLING;
                break;
            case "TRANSIT":
                travelMode = TravelMode.TRANSIT;
                break;
            default:
                travelMode = null;
                break;
        }

        return travelMode;
    }
}