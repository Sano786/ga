/*
 * USER: mslm
 * DATE: 3/31/2017
 */

package com.gedder.gedderalarm.util.except;

/** Thrown when Google Maps API returns an error message. */

public class GoogleMapsAPIException extends RuntimeException {
    public GoogleMapsAPIException() {
        super();
    }

    public GoogleMapsAPIException(String message) {
        super(message);
    }
}
