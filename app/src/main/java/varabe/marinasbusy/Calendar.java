package varabe.marinasbusy;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static varabe.marinasbusy.MainActivity.TAG;

class CalendarData {
    public int id, color;
    public String name;

    CalendarData(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}

class CalendarQuery {
    private static final String[] CALENDAR_PROJECTION = new String[]{
            Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.CALENDAR_COLOR,
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;

    static List<CalendarData> getCalendars(Activity activity) {
        Cursor cur = getQuery(activity);
        List<CalendarData> calendars = new ArrayList<>();
        while (cur.moveToNext()) {
            Log.d(TAG, cur.getString(PROJECTION_ID_INDEX) + ":" + cur.getString(PROJECTION_DISPLAY_NAME_INDEX));
            calendars.add(new CalendarData(
                    cur.getInt(PROJECTION_ID_INDEX),
                    cur.getString(PROJECTION_DISPLAY_NAME_INDEX),
                    cur.getInt(PROJECTION_COLOR_INDEX)
            ));
        }
        return calendars;
    }

    private static Cursor getQuery(Activity activity) {
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        return cr.query(uri, CALENDAR_PROJECTION, null, null, null);
    }
}