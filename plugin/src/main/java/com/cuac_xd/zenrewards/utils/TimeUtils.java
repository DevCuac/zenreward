package com.cuac_xd.zenrewards.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty() || timeString.equals("0")) {
            return 0;
        }
        try {
            long duration = Long.parseLong(timeString.substring(0, timeString.length() - 1));
            char unit = timeString.charAt(timeString.length() - 1);
            switch (unit) {
                case 's': return duration * 1000;
                case 'm': return duration * 60 * 1000;
                case 'h': return duration * 60 * 60 * 1000;
                case 'd': return duration * 24 * 60 * 60 * 1000;
                default: return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "0s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}