package varabe.marinasbusy;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CalendarContract.Events;
import android.support.annotation.Nullable;
import android.util.Log;

import static varabe.marinasbusy.MainActivity.TAG;


class Event {
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
    private static final String querySelection = String.format(
            "%s = %s AND %s = 0 AND ((%s > ?) OR (%s IS NOT NULL))",
            Events.AVAILABILITY,
            Events.AVAILABILITY_BUSY,
            Events.ALL_DAY,
            Events.DTSTART,
            Events.RRULE
    );
    public String title;
    public long startTime;
    public long endTime;

    public Event(String title, long startTime, long endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    @Nullable
    static Event getCurrent(Activity activity) {
        TimeConverter timeConverter = new TimeConverter();
        long currentTime = timeConverter.getCurrentTime();
        String title;
        long startTime;
        long endTime;
        Cursor cur = getEventQuery(currentTime, activity);
        while (cur.moveToNext()) {
            title = cur.getString(PROJECTION_TITLE);
            Log.d(TAG, title);
            long[] eventStartAndEnd = timeConverter.getEventTime(
                cur.getLong(PROJECTION_DTSTART),
                cur.getLong(PROJECTION_DTEND),
                cur.getString(PROJECTION_DURATION),
                cur.getString(PROJECTION_EVENT_TIMEZONE),
                cur.getString(PROJECTION_EVENT_RRULE)
            );
            startTime = eventStartAndEnd[0];
            endTime = eventStartAndEnd[1];
            if ((
                    endTime - startTime < TimeConverter.MILLIS_IN_DAY) &&
                    ((currentTime >= startTime) && (currentTime <= endTime))) {
                return new Event(title, startTime, endTime);
            }
        }
        return null;
    }
    private static Cursor getEventQuery(long currentTime, Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        String[] selectionArgs = {String.valueOf(currentTime - TimeConverter.MILLIS_IN_DAY)};
        return cr.query(Events.CONTENT_URI, EVENT_PROJECTION, querySelection, selectionArgs, null);
    }
}