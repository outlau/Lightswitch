package com.example.outlau.lightswitch;

/**
 * Created by outlau on 1/26/17.
 */

import java.util.Calendar;

public class Globals {

    public static int arcCounter = 0;
    public static int hour = 0;
    public static int minute = 0;
    public static Calendar calendar;
    public static Calendar startupCalendar;
    public static int isPM = 0;

    public static boolean isBelow = false;
    public static int startingProgress = 0;

    public static boolean connected = true;
    public static String[] errormsgs = {"Have you connected to wi-fi?", "Is the server on?"};
    public static int errorMsgCount = 0;

    public static boolean isTimerSet = false;
}
