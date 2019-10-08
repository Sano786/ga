/*
 * USER: mslm
 * DATE: March 8th, 2017
 */

package com.gedder.gedderalarm.google;

/** Enumeration class holding different status codes returned by Google Maps API. */

public enum JsonStatus {
    OK,
    NOT_FOUND,
    ZERO_RESULTS,
    MAX_WAYPOINTS_EXCEEDED,
    MAX_ROUTE_LENGTH_EXCEEDED,
    INVALID_REQUEST,
    OVER_QUERY_LIMIT,
    REQUEST_DENIED,
    UNKNOWN_ERROR
}