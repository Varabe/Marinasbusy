package varabe.marinasbusy;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;

class Calendar {
    public int id, color, colorKey;
    public String name;
    Calendar(int id, String name, int color, int colorKey) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.colorKey = colorKey;
    }

}

class CalendarQuery {
    private static final String[] CALENDAR_PROJECTION = new String[] {
            Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.CALENDAR_COLOR,
            Calendars.CALENDAR_COLOR_KEY
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;
    private static final int PROJECTION_COLOR_KEY_INDEX = 3;

    static List<Calendar> getCalendars(Activity activity) {
        int id, color, colorKey;
        String name;
        Cursor cur = getQuery(activity);
        List<Calendar> calendars = new ArrayList<>();
        while (cur.moveToNext()) {
            id = cur.getInt(PROJECTION_ID_INDEX);
            name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            color = cur.getInt(PROJECTION_COLOR_INDEX);
            colorKey = cur.getInt(PROJECTION_COLOR_KEY_INDEX);
            calendars.add(new Calendar(id, name, color, colorKey));
        }
        return calendars;
    }
    private static Cursor getQuery(Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        return cr.query(uri, CALENDAR_PROJECTION, null, null, null);
    }
}