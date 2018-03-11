package varabe.marinasbusy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Events;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.ical.compat.javautil.DateIterator;
import com.google.ical.compat.javautil.DateIteratorFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.icu.util.Calendar.MILLISECONDS_IN_DAY;
import static varabe.marinasbusy.MainActivity.CALENDAR_PREFERENCES;
import static varabe.marinasbusy.MainActivity.D;
import static varabe.marinasbusy.MainActivity.TAG;


class Event {
    public String getTitle() { return title; }
    public String getRdata() { return rdata; }
    public long getDuration() { return duration; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }

    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
    private String title, rdata;
    private long duration;
    private Date startDate, endDate;
    Event(String title, long startTime, String rawDuration, String rrule, String exrule, String rdate, String exdate) {
        this.title = title;
        this.duration = Duration.toMilliseconds(rawDuration);
        this.startDate = new Date(startTime);
        this.endDate = new Date(startTime + duration);
        this.rdata = formatRecurrenceData(rrule, exrule, rdate, exdate);
    }
    private String formatRecurrenceData(String rrule, String exrule, String exdate, String rdate) {
        if (rrule == null) rrule = "";
        else rrule = "RRULE:" + rrule;
        if (exrule == null) exrule = "";
        else exrule = "EXRULE:" + exrule;
        if (rdate == null) rdate = "";
        else rdate = "RDATE:" + rdate;
        if (exdate == null) exdate = "";
        else exdate = "EXDATE:" + exdate;
        return rrule + "\n" + exrule  + "\n" + rdate + "\n" + exdate;
    }
    public boolean isAt(Date time) {
        try {
            DateIterator iterator = DateIteratorFactory.createDateIterator(rdata, startDate, TIMEZONE, false);
            while(iterator.hasNext()) {
                Date start = iterator.next();
                Date end = new Date(start.getTime() + duration);
                if (start.before(time) && end.after(time)) return true;
                if (start.after(time)) return false;
            }
        } catch (ParseException e){
            e.printStackTrace(); // If it happens, I have no idea what to do
        }
        return false;
    }
    public String formatTime() {
        return String.format("(%s - %s)", timeFormatter.format(startDate), timeFormatter.format(endDate));
    }
}
class EventQuery {
    private static final String[] EVENT_PROJECTION = {
            Events.TITLE,
            Events.DTSTART,
            Events.DURATION,
            Events.RRULE,
            Events.EXRULE,
            Events.RDATE,
            Events.EXDATE,
    };
    private static final int
            PROJECTION_TITLE = 0,
            PROJECTION_DTSTART = 1,
            PROJECTION_DURATION = 2,
            PROJECTION_RRULE = 3,
            PROJECTION_EXRULE = 4,
            PROJECTION_RDATE = 5,
            PROJECTION_EXDATE = 6;
    private static final String querySelectionTemplate = ""; // Put any additional query args in here, like "ALL_DAY=0"
    @Nullable
    static Event getCurrent(Activity activity) {
        Date currentTime = new Date(System.currentTimeMillis());
        Cursor cur = getQuery(activity);
        Event event;
        while (cur.moveToNext()) {
            String title = cur.getString(PROJECTION_TITLE);
            if (D) Log.d(TAG, title);
            event = new Event(
                    title,
                    cur.getLong(PROJECTION_DTSTART),
                    cur.getString(PROJECTION_DURATION),
                    cur.getString(PROJECTION_RRULE),
                    cur.getString(PROJECTION_EXRULE),
                    cur.getString(PROJECTION_RDATE),
                    cur.getString(PROJECTION_EXDATE)
            );
            if (event.getDuration() < MILLISECONDS_IN_DAY && event.isAt(currentTime))
                    return event;
        }
        return null;
    }
    private static Cursor getQuery(Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        String[] selectionArgs = {};
        String querySelection = getCalendarQuerySelections(activity);
        if (D) Log.d(TAG, "QuerySelection: " + querySelection);
        return cr.query(Events.CONTENT_URI, EVENT_PROJECTION, querySelection, selectionArgs, null);
    }
    private static String getCalendarQuerySelections(Activity activity) {
        // Returns a SQL query string that selects all calendars chosen by the user in settings
        Map<String, ?> prefsDict = activity.getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE).getAll();
        ArrayList<String> calendarIds = getActiveCalendarIds(prefsDict);
        if (calendarIds.isEmpty())
            return "";
        else
            return constructCalendarQuerySelections(calendarIds);
    }
    private static ArrayList<String> getActiveCalendarIds(Map<String, ?> prefsDict) {
        ArrayList<String> calendarIds = new ArrayList<>();
        for (Map.Entry<String, ?> entry: prefsDict.entrySet()) {
//            Log.d(TAG, entry.getKey() + ":" + entry.getValue());
            Boolean isActive = (Boolean) entry.getValue();
            if (isActive)
                calendarIds.add(entry.getKey());
        }
        return calendarIds;
    }
    private static String constructCalendarQuerySelections(ArrayList<String> calendarIds) {
        String formattedSqlSelection = "(" + Events.CALENDAR_ID + "=" + calendarIds.remove(0);
        while(!calendarIds.isEmpty()) {
            formattedSqlSelection += " OR " + Events.CALENDAR_ID + "=" + calendarIds.remove(0);
        }
        return formattedSqlSelection + ")";
    }
}