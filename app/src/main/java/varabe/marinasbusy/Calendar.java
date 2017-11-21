package varabe.marinasbusy;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;

class MyCalendar {
    public int id, color;
    public String name;
    MyCalendar(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

}

class CalendarQuery {
    private static final String[] CALENDAR_PROJECTION = new String[] {
            Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;

    static List<MyCalendar> getCalendars(Activity activity) {
        int id, color;
        String name;
        Cursor cur = getQuery(activity);
        List<MyCalendar> calendars = new ArrayList<>();
        while (cur.moveToNext()) {
            id = cur.getInt(PROJECTION_ID_INDEX);
            name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            color = cur.getInt(PROJECTION_COLOR_INDEX);
            calendars.add(new MyCalendar(id, name, color));
        }
        return calendars;
    }
    private static Cursor getQuery(Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        return cr.query(uri, CALENDAR_PROJECTION, null, null, null);
    }
}