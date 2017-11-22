package varabe.marinasbusy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.provider.CalendarContract.Events;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static varabe.marinasbusy.MainActivity.CALENDAR_PREFERENCES;
import static varabe.marinasbusy.MainActivity.D;
import static varabe.marinasbusy.MainActivity.TAG;


class Event {
    private String title;
    private Time startTime;
    private Time endTime;
    private int[] weekdays;
    private int weekday = 0;
    Event(String title, long startTime, long endTime, String durationStr, String rrule) {
        long duration = TimeUtils.convertRfcIntoMillis(durationStr);
        this.title = title;
        if (rrule != null)
            this.weekdays = TimeUtils.getWeekdays(rrule);
        else
            this.weekday = TimeUtils.getWeekday(startTime);
        if (endTime == 0) {
            endTime = startTime + duration;
        }
        this.startTime = new Time(startTime);
        this.endTime = new Time(endTime);
    }
    public boolean isDuring(long time, int weekday) {
        return isAtWeekday(weekday) && isAtTime(time);
    }
    public String getTitle() {
        return title;
    }
    public long getDuration() {
        return endTime.getLongRepresentation() - startTime.getLongRepresentation();
    }
    public String getFormatTime() {
        return String.format("(%s - %s)", startTime, endTime);
    }
    private boolean isAtWeekday(int weekday) {
        if (this.weekday != 0)
            return this.weekday == weekday;
        else {
            for (int w: this.weekdays) {
                if (w == weekday)
                    return true;
            }
        }
        return false;
    }
    private boolean isAtTime(long timeInMilliseconds) {
        Time time = new Time(timeInMilliseconds);
        return (time.isAfter(this.startTime) && time.isBefore(endTime));
    }
}
class EventQuery {
    private static final String[] EVENT_PROJECTION = {
            Events.TITLE,
            Events.DTSTART,
            Events.DTEND,
            Events.DURATION,
            Events.EVENT_TIMEZONE,
            Events.RRULE,
    };
    private static final int
            PROJECTION_TITLE = 0,
            PROJECTION_DTSTART = 1,
            PROJECTION_DTEND = 2,
            PROJECTION_DURATION = 3,
            PROJECTION_EVENT_TIMEZONE = 4,
            PROJECTION_EVENT_RRULE = 5;
    private static final String querySelectionTemplate = String.format(
            "%s = %s AND %s = 0 AND ((%s > ?) OR (%s IS NOT NULL))",
            Events.AVAILABILITY,
            Events.AVAILABILITY_BUSY,
            Events.ALL_DAY,
            Events.DTSTART,
            Events.RRULE
    );
    @Nullable
    static Event getCurrent(Activity activity) {
        Event event;
        long currentTime = System.currentTimeMillis();
        int currentWeekday = TimeUtils.getWeekday(currentTime);
        Cursor cur = getQuery(currentTime, activity);
        while (cur.moveToNext()) {
            event = new Event(
                    cur.getString(PROJECTION_TITLE),
                    cur.getLong(PROJECTION_DTSTART),
                    cur.getLong(PROJECTION_DTEND),
                    cur.getString(PROJECTION_DURATION),
                    cur.getString(PROJECTION_EVENT_RRULE)
            );
            if (event.getDuration() < TimeUtils.MILLIS_IN_DAY) {
                if (event.isDuring(currentTime, currentWeekday)) {
                    return event;
                }
            }
        }
        return null;
    }
    private static Cursor getQuery(long currentTime, Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        String[] selectionArgs = {String.valueOf(currentTime - TimeUtils.MILLIS_IN_DAY)};
        String querySelection = querySelectionTemplate + getQuerySelectionArgs(activity);
        if (D) Log.d(TAG, "QuerySelection:" + querySelection);
        return cr.query(Events.CONTENT_URI, EVENT_PROJECTION, querySelection, selectionArgs, null);
    }
    private static String getQuerySelectionArgs(Activity activity) {
        String formattedSqlSelection = " AND (";
        String AND = " AND ";
        int INITIAL_LENGTH = formattedSqlSelection.length();
        Map<String, ?> prefsDict = activity.getSharedPreferences(
                CALENDAR_PREFERENCES,
                Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry: prefsDict.entrySet()) {
            if (entry.getValue().toString().equals("true")) {
                formattedSqlSelection += Events.CALENDAR_ID + "!=" + entry.getKey() + AND;
            }
        }
        if(formattedSqlSelection.length() == INITIAL_LENGTH) {
            return "";
        }
        else {
            return formattedSqlSelection.substring(0, formattedSqlSelection.length() - AND.length()) + ")";
        }
    }
}
class TimeUtils {
    static final int MILLIS_IN_DAY = 86400000;
    private static final int MILLIS_IN_SECOND = 1000;
    private static Calendar weekdayCalendar = Calendar.getInstance();
    static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

    static Date getDefaultTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.DAY_OF_MONTH, 8);
        calendar.set(Calendar.MONTH, 2);
        calendar.set(Calendar.YEAR, 5);
        return calendar.getTime();

    }
    static int[] getWeekdays(String rrule) {
        int weekday = 0;
        String[] stringRepetitionDays = rrule.split(";")[2].split("=")[1].split(",");
        int[] intRepetitionDays = new int[stringRepetitionDays.length];
        for (int i = 0; i < stringRepetitionDays.length; i++) {
            weekday = getWeekday(stringRepetitionDays[i]);
            intRepetitionDays[i] = weekday;
        }
        return intRepetitionDays;
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
    static int getWeekday(long milliseconds) {
        weekdayCalendar.setTimeInMillis(milliseconds);
        return weekdayCalendar.get(Calendar.DAY_OF_WEEK);
    }
    static long convertRfcIntoMillis(String rfcTime) throws UnsupportedOperationException {
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

class Time {
    // Produces a bug if event starts and ends on different days
    private int hours;
    private int minutes;
    private int seconds;
    private static int SECONDS_IN_HOUR = 3600;
    private static int SECONDS_IN_MINUTE = 60;

    Time(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
        seconds = calendar.get(Calendar.SECOND);
    }
    public String toString() {
        String minutes = this.minutes + "";
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        return String.format("%s:%s", hours+"", minutes);
    }
    boolean isAfter(Time time) {
        return getLongRepresentation() > time.getLongRepresentation();
    }
    boolean isBefore(Time time) {
        return getLongRepresentation() < time.getLongRepresentation();
    }
    long getLongRepresentation() {
        return (hours * SECONDS_IN_HOUR) + (minutes * SECONDS_IN_MINUTE) + seconds;
    }
}