package varabe.marinasbusy;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.util.Log;

import java.util.Date;

import static varabe.marinasbusy.MainActivity.TAG;

class TimeConverter {
    static final int MILLIS_IN_SECOND = 1000;
    static final int MILLIS_IN_DAY = 86400000;
    static final boolean D = MainActivity.D;
    static private Calendar weekdayCalendar = Calendar.getInstance();
    static private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    private TimeZone currentTimezone;
    private int currentWeekday;
    private long currentTime;
    private int timeOffset; // TODO construct offsets with Date objects (they can do that)

    public TimeZone getCurrentTimezone() {
        return currentTimezone;
    }

    public int getCurrentWeekday() {
        return currentWeekday;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    TimeConverter() {
        currentTimezone = TimeZone.getDefault();
        timeOffset = currentTimezone.getRawOffset();
        currentTime = System.currentTimeMillis();
        currentWeekday = getWeekday(currentTime);
        if (D) Log.d(TAG, String.format("Created TimeConverter. Current time: %s", currentTime+""));
    }
    static String formatTime(Date date) {
        return timeFormatter.format(date);
    }
    long[] getEventTime(long startTime, long endTime, String rfcDuration, String rrule) {
        if(D) Log.d(TAG, String.format("(before conversion) STIME:%s::ETIME:%s", startTime, endTime));
        if (rrule != null) {
            if (D) Log.d(TAG, "RRULE:" + rrule);
            int lastWeekday = getLastWeekday(rrule, currentWeekday);
            if(D) Log.d(TAG, lastWeekday+"");
            /*
              * If rrule exists, then startTime is at the last day of repetition in current
              * week. Therefore, we need to find the difference of days between ours and
              * last day to calculate the real time of event.
              */
            if (lastWeekday != 0 && !(getWeekday(startTime + timeOffset) == currentWeekday)) {
                long dayOffset = Math.abs(lastWeekday - currentWeekday);
                startTime += dayOffset * MILLIS_IN_DAY;
            }
        }
        if (rfcDuration != null)
            endTime += convertRfcIntoMillis(rfcDuration) + startTime;
        if(D) Log.d(TAG, String.format("(after conversion) STIME:%s::ETIME:%s", startTime, endTime));
        return new long[] {startTime, endTime};
    }
    private int getLastWeekday(String rrule, int currentWeekday) {
        // rrule == FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE
        int weekday, lastWeekday = 0;
        String[] repetitionDaysAsStrings = rrule.split(";")[2].split("=")[1].split(",");
        for (String weekdayStr: repetitionDaysAsStrings) {
            weekday = getWeekday(weekdayStr);
            if (weekday == currentWeekday) {
                if (lastWeekday == 0) {
                    return weekday;
                } else {
                    return lastWeekday;
                }
            }
            lastWeekday = weekday;
        }
        return 0;
    }
    private static int getWeekday(long milliseconds) {
        weekdayCalendar.setTimeInMillis(milliseconds);
        return weekdayCalendar.get(Calendar.DAY_OF_WEEK);
    }
    private static int getWeekday(Date date) {
        weekdayCalendar.setTime(date);
        return weekdayCalendar.get(Calendar.DAY_OF_WEEK);
    }
    private static int getWeekday(String firstLetters) {
        switch (firstLetters) {
            case "MO":
                return Calendar.MONDAY;
            case "TU":
                return Calendar.TUESDAY;
            case "WE":
                return Calendar.WEDNESDAY;
            case "TH":
                return Calendar.THURSDAY;
            case "FR":
                return Calendar.FRIDAY;
            case "SA":
                return Calendar.SATURDAY;
            case "SU":
                return Calendar.SUNDAY;
            default:
                return 0;
        }
    }
    private static long convertRfcIntoMillis(String rfcTime) throws UnsupportedOperationException {
        if (rfcTime == null) {
            return 0;
        }
        Character unitOfMeasure = rfcTime.charAt(rfcTime.length()-1); // Seconds/Minutes/Hours
        String rfcTimeDigitsOnly = rfcTime.substring(1, rfcTime.length() - 1);
        long duration = Long.parseLong(rfcTimeDigitsOnly);
        switch (unitOfMeasure) {
            case 'S':
                return duration * MILLIS_IN_SECOND;
            default:
                throw new UnsupportedOperationException(
                        "This method can only be used when RFC is in seconds. RFC: " + rfcTime);
        }
    }
}
