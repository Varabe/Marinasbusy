package varabe.marinasbusy;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;

class TimeConverter {
    static final int MILLIS_IN_SECOND = 1000;
    static final int MILLIS_IN_DAY = 86400000;
    static private Calendar weekdayCalendar = Calendar.getInstance();

    private TimeZone currentTimezone;
    private int currentWeekday;
    private long currentTime;
    private int timeOffset; // TODO construct offsets depending on the timezone of EVENT

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
        currentTime = System.currentTimeMillis() + timeOffset;
        currentWeekday = getWeekday(currentTime);

    }
    long[] getEventTime(
            long startTime, long endTime, String rfcDuration,
            String timeZoneName, String rrule) {
        int gmtOffset;
        if (timeZoneName != null && TimeZone.getTimeZone(timeZoneName).equals(currentTimezone)) {
            // For reasons I don't understand it works like this. So if event timezone = UTC, then
            // its time is correct. If it equals real timezone, we need to add (GMT + N) offset
            gmtOffset = timeOffset;
        } else {
            gmtOffset = 0;
        }
        if (rrule != null) {
            int[] repetitionDays = getRepetitionDays(rrule);
            if (arrayContains(repetitionDays, currentWeekday)) {
                /*
                 * If rrule exists, then startTime is at the first day of repetition in current
                 * week. Therefore, we need to find the difference of days between ours and
                 * first day to calculate the real time of event.
                 */
                if (!(getWeekday(startTime + gmtOffset) == currentWeekday)) {
                    long dayOffset = Math.abs(repetitionDays[0] - currentWeekday);
                    startTime += dayOffset * MILLIS_IN_DAY;
                }
            }
        }
        if (rfcDuration != null) {
            endTime += convertRfcIntoMillis(rfcDuration) + startTime;
        }
        startTime += gmtOffset;
        endTime += gmtOffset;
        return new long[] {startTime, endTime};
    }
    private boolean arrayContains(int[] array, int item) {
        for (int i: array) {
            if (i == item) {
                return true;
            }
        }
        return false;
    }
    private int[] getRepetitionDays(String rrule) {
        // rrule == FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE
        String[] repetitionDaysAsStrings = rrule.split(";")[2].split("=")[1].split(",");
        int[] repetitionDaysAsInts = new int[repetitionDaysAsStrings.length];
        for (int i = 0; i < repetitionDaysAsStrings.length; i++) {
            repetitionDaysAsInts[i] = getWeekday(repetitionDaysAsStrings[i]);
        }
        return repetitionDaysAsInts;
    }
    private static int getWeekday(long milliseconds) {
        weekdayCalendar.setTimeInMillis(milliseconds);
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
        // TODO: Implement a switch statement that will define conversion factor depending on unitOfMeasure
        switch (unitOfMeasure) {
            case 'S':
                return duration * MILLIS_IN_SECOND;
            default:
                throw new UnsupportedOperationException(
                        "This method can only be used when RFC is in seconds. RFC: " + rfcTime);
        }
    }
}
