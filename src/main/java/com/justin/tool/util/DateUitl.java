package com.justin.tool.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public final class DateUitl {

    private static String TIME_FORMAT = "HH:mm:ss.SSS";
    private static String GMT_FORMAT = "EEE, d MMM yyyy HH:mm:ss 'GMT'";

    public static String formatTime(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(d);
    }

    public static String formatGMT(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat(GMT_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }
}
