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
    Event(String title, long startTime, long endTime, String rawDuration, String rrule) {
        long duration = RFC.getDuration(rawDuration);
        this.title = title;
        if (rrule == null)
            this.weekday = RFC.getWeekday(startTime);
        else {
            this.weekdays = RFC.getWeekdays(rrule);
            endTime = startTime + duration;
        }
        this.startTime = new Time(startTime);
        this.endTime = new Time(endTime);
    }
    boolean isDuring(long time, int weekday) {
        return isAtWeekday(weekday) && isAtTime(time);
    }
    String getTitle() {
        return title;
    }
    String getFormatTime() {
        return String.format("(%s - %s)", startTime, endTime);
    }
    long getDuration() {
        return endTime.getInt() - startTime.getInt();
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
            Events.RRULE,
    };
    private static final int
            PROJECTION_TITLE = 0,
            PROJECTION_DTSTART = 1,
            PROJECTION_DTEND = 2,
            PROJECTION_DURATION = 3,
            PROJECTION_EVENT_RRULE = 4;
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
        int currentWeekday = RFC.getWeekday(currentTime);
        Cursor cur = getQuery(currentTime, activity);
        while (cur.moveToNext()) {
            event = new Event(
                    cur.getString(PROJECTION_TITLE),
                    cur.getLong(PROJECTION_DTSTART),
                    cur.getLong(PROJECTION_DTEND),
                    cur.getString(PROJECTION_DURATION),
                    cur.getString(PROJECTION_EVENT_RRULE)
            );
            if (event.getDuration() < RFC.MILLIS_IN_DAY) {
                if (event.isDuring(currentTime, currentWeekday)) {
                    return event;
                }
            }
        }
        return null;
    }
    private static Cursor getQuery(long currentTime, Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        String[] selectionArgs = {String.valueOf(currentTime - RFC.MILLIS_IN_DAY)};
        String querySelection = querySelectionTemplate + getQuerySelectionArgs(activity);
        if (D) Log.d(TAG, "QuerySelection: " + querySelection);
        return cr.query(Events.CONTENT_URI, EVENT_PROJECTION, querySelection, selectionArgs, null);
    }
    private static String getQuerySelectionArgs(Activity activity) {
        // TODO: Refactor this piece of crap
        String formattedSqlSelection = " AND (";
        String AND = " AND ";
        int INITIAL_LENGTH = formattedSqlSelection.length();
        Map<String, ?> prefsDict = activity.getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry: prefsDict.entrySet()) {
            Boolean value = (Boolean) entry.getValue();
            if (!value) {
                formattedSqlSelection += Events.CALENDAR_ID + "!=" + entry.getKey() + AND;
            }
        }
        if(formattedSqlSelection.length() == INITIAL_LENGTH)
            return "";
        else
            // We remove the last " AND " and add the closing parentheses
            return formattedSqlSelection.substring(0, formattedSqlSelection.length() - AND.length()) + ")";
    }
}