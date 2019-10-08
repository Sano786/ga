/*
 * USER: mslm
 * DATE: 3/11/2017
 */

package com.gedder.gedderalarm.db;

/** Contains the schema for our application's database. */

public class AlarmClockDBSchema {

    /** Contains data for each alarm clock. */

    public static final class AlarmClockTable {
        public static final String TABLE_NAME = "alarmClocks";

        public static final class Columns {
            public static final String ID                  = "_id";
            public static final String UUID                = "uuid";
            public static final String REQUEST_CODE        = "requestCode";
            public static final String ORIGIN_ID           = "originId";
            public static final String ORIGIN_ADDRESS      = "originAddress";
            public static final String DESTINATION_ID      = "destinationId";
            public static final String DESTINATION_ADDRESS = "destinationAddress";
            public static final String TRAVEL_MODE         = "travelMode";
            public static final String TRANSIT_MODE        = "transitMode";
            public static final String REPEAT_DAYS         = "repeatDays";
            public static final String ALARM_DAY           = "alarmDay";
            public static final String ALARM_HOUR          = "alarmHour";
            public static final String ALARM_MINUTE        = "alarmMinute";
            public static final String ALARM_TIME          = "alarmTime";
            public static final String ARRIVAL_DAY         = "arrivalDay";
            public static final String ARRIVAL_HOUR        = "arrivalHour";
            public static final String ARRIVAL_MINUTE      = "arrivalMinute";
            public static final String ARRIVAL_TIME        = "arrivalTime";
            public static final String PREP_HOUR           = "prepHour";
            public static final String PREP_MINUTE         = "prepMinute";
            public static final String PREP_TIME           = "prepTime";
            public static final String ALARM_SET           = "alarmSet";
            public static final String GEDDER_SET          = "gedderSet";
        }
    }
}
