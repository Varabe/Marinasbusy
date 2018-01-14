package varabe.marinasbusy;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static varabe.marinasbusy.MainActivity.TAG;

class RFC {
    //  RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR; (Common RRULE form)
    static final int MILLIS_IN_DAY = 86400000;
    static final int MILLIS_IN_SECOND = 1000;
    private static Pattern BYDAY = Pattern.compile("BYDAY=.+(;|$)");
    private static java.util.Calendar weekdayCalendar = java.util.Calendar.getInstance();

    static int[] getWeekdays(String rrule) {
        String[] recurrenceDaysAsLetters = getRecurrenceDaysAsLetters(rrule);
        int[] recurrenceDays = new int[recurrenceDaysAsLetters.length];
        for (int i = 0; i < recurrenceDaysAsLetters.length; i++) {
            recurrenceDays[i] = getWeekday(recurrenceDaysAsLetters[i]);
        }
        return recurrenceDays;
    }
    private static String[] getRecurrenceDaysAsLetters(String rrule) {
        Matcher matcher = BYDAY.matcher(rrule);
        if (!matcher.find()) {
            return new String[] {};
        }
        else {
            String bydayExpression = matcher.group(0); // BYDAY=MO,TU,FR
            return bydayExpression.split("=")[1].split(",");
        }
    }
    private static int getWeekday(String firstLetters) {
        switch (firstLetters) {
            case "MO":
                return java.util.Calendar.MONDAY;
            case "TU":
                return java.util.Calendar.TUESDAY;
            case "WE":
                return java.util.Calendar.WEDNESDAY;
            case "TH":
                return java.util.Calendar.THURSDAY;
            case "FR":
                return java.util.Calendar.FRIDAY;
            case "SA":
                return java.util.Calendar.SATURDAY;
            case "SU":
                return java.util.Calendar.SUNDAY;
            default:
                return 0;
        }
    }
    static int getWeekday(long milliseconds) {
        weekdayCalendar.setTimeInMillis(milliseconds);
        return weekdayCalendar.get(java.util.Calendar.DAY_OF_WEEK);
    }

    static long getDuration(String rawDuration) {
        if (rawDuration == null)
            return 0;
        else
            try {
                return getDurationInMillis(rawDuration);
            } catch (UnsupportedOperationException e) {
                throw new UnsupportedOperationException(e.getMessage() + "RFC: " + rawDuration);
            }
    }
    private static long getDurationInMillis(String rawDuration) {
        int lastCharacterIndex = rawDuration.length() - 1;
        int digitBeginningIndex = 1; // The first character is always 'P', which is followed by digits
        char measureUnit = rawDuration.charAt(lastCharacterIndex);
        String digitsOnly = rawDuration.substring(digitBeginningIndex, lastCharacterIndex);
        long duration = Long.parseLong(digitsOnly);
        return getMeasureUnitValue(measureUnit) * duration;
    }
    private static long getMeasureUnitValue(char measureUnit) {
        switch (measureUnit) {
            case 'S':
                return MILLIS_IN_SECOND;
            case 'M':
                ; // Minutes
            case 'H':
                ; // Hours
            case 'D':
                ; // Days
            case 'W':
                ; // Weeks
            default:
                throw new UnsupportedOperationException("This method can only be used when RFC is in seconds.");
        }
    }
}