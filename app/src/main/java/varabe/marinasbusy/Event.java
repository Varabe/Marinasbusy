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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static varabe.marinasbusy.MainActivity.CALENDAR_PREFERENCES;
import static varabe.marinasbusy.MainActivity.D;
import static varabe.marinasbusy.MainActivity.TAG;


class Event {
    public String getTitle() { return title; }
    public String getRdata() { return rdata; }
    public long getDuration() { return duration; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
    private String title, rdata;
    private long duration;
    private Date startDate, endDate;
    Event(String title, long startTime, String rawDuration, String rrule, String exrule, String rdate, String exdate) {
        this.title = title;
        this.duration = Duration.toMilliseconds(rawDuration);
        this.startDate = new Date(startTime); // MIGHT CAUSE AN ERROR, because we don't consider the timezone. To consider it, use Calendar and get Date from it
        this.endDate = new Date(startTime + duration); // Same cause of error might apply
//        Log.d(TAG, startDate + "----" + endDate);
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
            DateIterator iterator = DateIteratorFactory.createDateIterator(rdata, startDate, Constant.TIMEZONE, false);
            while(iterator.hasNext()) {
                Date start = iterator.next();
                Log.d(TAG, start+"");
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
        Log.d(TAG, currentTime.toString());
        Cursor cur = getQuery(activity);
        Event event;
        while (cur.moveToNext()) {
            String title = cur.getString(PROJECTION_TITLE);
            Log.d(TAG, title);
//            if (D && title != null) Log.d(TAG, title + ":" + cur.getLong(PROJECTION_DTSTART) + ":" + cur.getString(PROJECTION_DURATION) + ":" + cur.getString(PROJECTION_EVENT_RRULE) + ":" + cur.getString(PROJECTION_EXRULE) + ":" + cur.getString(PROJECTION_RDATE) + ":" + cur.getString(PROJECTION_EXDATE)); // Amazing logging, I know
            event = new Event(
                    title,
                    cur.getLong(PROJECTION_DTSTART),
                    cur.getString(PROJECTION_DURATION),
                    cur.getString(PROJECTION_RRULE),
                    cur.getString(PROJECTION_EXRULE),
                    cur.getString(PROJECTION_RDATE),
                    cur.getString(PROJECTION_EXDATE)
            );
            if (event.getDuration() < Constant.MILLISECONDS_IN_DAY && event.isAt(currentTime))
                    return event;
        }
        return null;
    }
    private static Cursor getQuery(Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        String[] selectionArgs = {};
        String querySelection = getQuerySelectionArgs(activity);
        if (D) Log.d(TAG, "QuerySelection: " + querySelection);
        return cr.query(Events.CONTENT_URI, EVENT_PROJECTION, querySelection, selectionArgs, null);
    }
    private static String getQuerySelectionArgs(Activity activity) {
        // TODO: Refactor this piece of crap
        String formattedSqlSelection = "(";
        String OR = " OR ";
        int INITIAL_LENGTH = formattedSqlSelection.length();
        Map<String, ?> prefsDict = activity.getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry: prefsDict.entrySet()) {
//            Log.d(TAG, entry.getKey() + ":" + entry.getValue());
            Boolean value = (Boolean) entry.getValue();
            if (value) {
                formattedSqlSelection += Events.CALENDAR_ID + "=" + entry.getKey() + OR;
            }
        }
        if(formattedSqlSelection.length() == INITIAL_LENGTH)
            return "";
        else
            // We remove the last " AND " and add the closing parentheses
            return formattedSqlSelection.substring(0, formattedSqlSelection.length() - OR.length()) + ")";
    }
}