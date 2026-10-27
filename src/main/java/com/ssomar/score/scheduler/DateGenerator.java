package com.ssomar.score.scheduler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


// WHEN format
// when:
// - {YEAR}:::{MONTH}:::{DAY}:::{HOUR}:::{MIN}:::{SEC}
// - 2024:::XX:::XX:::XX:::XX:::XX:::XX // In 2024
// - %%%%:::12:::[24,25,26]:::16:::XX:::XX  // Every years at december 24,25,26 16 hours
// - %%%:::%%:::%%:::04:::XX:::XX // Every 4 hours
//
// - {YEAR}:!:{WEEK}:!:{DAYSTRING}:!:{HOUR}:!:{MIN}:!:{SEC}
// - %%%:!:%%:!:MONDAY:!:XX:!:XX:!:XX // Every monday
// - %%%:!:42:!:XX:!:%%:!:XX:!:XX // Every hour of the week 42
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateGenerator {

    public static List<Long> generateNextValidTimestamps(Date startDate, List<String> dateFormats, long calculationTime, Date minDate, Date maxDate) {
        List<Long> timestamps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        int maxField = getMaxField(dateFormats);

        // 120 secondes
        long maxSearch = System.currentTimeMillis() + calculationTime;

        while (timestamps.size() < 100 && calendar.getTimeInMillis() < maxSearch && calendar.getTimeInMillis() < maxDate.getTime() && calendar.getTimeInMillis() > minDate.getTime()){
            for (String format : dateFormats) {
                if (timestamps.size() >= 100) break;
                if (isValid(calendar, format)) {
                    timestamps.add(calendar.getTimeInMillis());
                }
            }
            calendar.add(maxField, 1);
            //System.out.println("Calendar: "+calendar.getTimeInMillis());
        }

        return timestamps;
    }

    private static int getMaxField(List<String> dateFormats) {
        int maxField = Calendar.SECOND;
        for (String format : dateFormats) {
            int field = getMinimumFieldToAdvance(format);
            //System.out.println("Field: " + field+" of format: "+format);
            if (field > maxField) {
                maxField = field;
            }
        }
        return maxField;
    }

    private static int getMinimumFieldToAdvance(String format) {
        String[] parts = null;

        if (format.contains(":::")) {
            parts = format.split(":::");
        } else if (format.contains(":!:")) {
            parts = format.split(":!:");
        }
        if (!parts[5].equals("XX")) return Calendar.SECOND;
        if (!parts[4].equals("XX")) return Calendar.MINUTE;
        if (!parts[3].equals("XX")) return Calendar.HOUR_OF_DAY;
        if (!parts[2].equals("XX")) return Calendar.DAY_OF_WEEK;
        if (!parts[1].equals("XX")) return Calendar.WEEK_OF_YEAR;
        else return Calendar.YEAR;

    }

    private static boolean isValid(Calendar calendar, String format) {
        if (format.contains(":::")) {
            return isValidTripleColonFormat(calendar, format);
        } else if (format.contains(":!:")) {
            return isValidExclamationFormat(calendar, format);
        }
        return false;
    }

    private static boolean isValidTripleColonFormat(Calendar calendar, String format) {
        String[] parts = format.split(":::");
        int year = getFieldValue(parts[0], calendar.get(Calendar.YEAR));
        int month = getFieldValue(parts[1], calendar.get(Calendar.MONTH) + 1) - 1;
        int day = getFieldValue(parts[2], calendar.get(Calendar.DAY_OF_MONTH));
        int hour = getFieldValue(parts[3], calendar.get(Calendar.HOUR_OF_DAY));
        int minute = getFieldValue(parts[4], calendar.get(Calendar.MINUTE));
        int second = getFieldValue(parts[5], calendar.get(Calendar.SECOND));

        return calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.DAY_OF_MONTH) == day &&
                calendar.get(Calendar.HOUR_OF_DAY) == hour &&
                calendar.get(Calendar.MINUTE) == minute &&
                calendar.get(Calendar.SECOND) == second;
    }

    private static boolean isValidExclamationFormat(Calendar calendar, String format) {
        String[] parts = format.split(":!:");
        int year = getFieldValue(parts[0], calendar.get(Calendar.YEAR));
        int week = getFieldValue(parts[1], calendar.get(Calendar.WEEK_OF_YEAR));
        int dayOfWeek = getDayOfWeek(parts[2], calendar.get(Calendar.DAY_OF_WEEK));
        int hour = getFieldValue(parts[3], calendar.get(Calendar.HOUR_OF_DAY));
        int minute = getFieldValue(parts[4], calendar.get(Calendar.MINUTE));
        int second = getFieldValue(parts[5], calendar.get(Calendar.SECOND));

        return calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.WEEK_OF_YEAR) == week &&
                calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek &&
                calendar.get(Calendar.HOUR_OF_DAY) == hour &&
                calendar.get(Calendar.MINUTE) == minute &&
                calendar.get(Calendar.SECOND) == second;
    }

    private static int getDayOfWeek(String formatPart, int currentDayOfWeek) {
        if (formatPart.equals("%%%") || formatPart.equals("%%") || formatPart.equals("%") || formatPart.equals("%%%%")) {
            return currentDayOfWeek;
        } else if (formatPart.equals("XX")) {
            return Calendar.SUNDAY;
        } else {
            switch (formatPart.toUpperCase()) {
                case "MONDAY": return Calendar.MONDAY;
                case "TUESDAY": return Calendar.TUESDAY;
                case "WEDNESDAY": return Calendar.WEDNESDAY;
                case "THURSDAY": return Calendar.THURSDAY;
                case "FRIDAY": return Calendar.FRIDAY;
                case "SATURDAY": return Calendar.SATURDAY;
                case "SUNDAY": return Calendar.SUNDAY;
                default: return currentDayOfWeek;
            }
        }
    }

    private static int getFieldValue(String formatPart, int currentValue) {
        if (formatPart.equals("%%%") || formatPart.equals("%%%%") || formatPart.equals("%%") || formatPart.equals("%")) {
            return currentValue;
        } else if (formatPart.equals("XX")) {
            return 0;
        } else if (formatPart.startsWith("[")) {
            String[] values = formatPart.replace("[", "").replace("]", "").split(",");
            for (String value : values) {
                if (Integer.parseInt(value) == currentValue) {
                    return currentValue;
                }
            }
        } else {
            return Integer.parseInt(formatPart);
        }
        return -1;
    }

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date();

        List<String> dateFormats = new ArrayList<>();
        dateFormats.add("2024:::12:::[24,25,26]:::16:::XX:::XX");
        dateFormats.add("%%%%:::%%:::%%:::04:::XX:::XX");
        dateFormats.add("%%%%:!:%%:!:MONDAY:!:XX:!:XX:!:XX");
        dateFormats.add("%%%%:!:42:!:XX:!:%%:!:XX:!:XX");

        //List<Long> timestamps = generateNextValidTimestamps(currentDate, dateFormats);
        //for (Long timestamp : timestamps) {
        //    System.out.println(new Date(timestamp));
        //}
    }
}

