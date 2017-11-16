//package varabe.marinasbusy;
//
//import android.provider.CalendarContract;
//
//import java.util.List;
//
//class Calendar {
//    private static final String[] CALENDAR_PROJECTION = new String[] {
//            Calendars._ID,
//            Calendars.CALENDAR_DISPLAY_NAME,
//            CalendarContract.Calendars.CALENDAR_COLOR,
//    };
//    // The indices for the projection array above.
//    private static final int PROJECTION_ID_INDEX = 0;
//    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
//    private static final int PROJECTION_COLOR_INDEX = 2;
//
//    private List<Calendar> getCalendars() {
//        int id, color;
//        String name;
//        Cursor cur = getCalendarQuery();
//        List<Calendar> calendars = new ArrayList<Calendar>();
//        while (cur.moveToNext()) {
//            id = cur.getInt(PROJECTION_ID_INDEX);
//            name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
//            color = cur.getInt(PROJECTION_COLOR_INDEX);
//            calendars.add(new Calendar(id, name, color));
//        }
//        return calendars;
//    }
//
//    private Cursor getCalendarQuery() {
//        ContentResolver cr = getContentResolver();
//        Uri uri = Calendars.CONTENT_URI;
//        String selection = String.format("%s is not NULL", Calendars._ID);
//        String[] selectionArgs = new String[]{};
//        return cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
//    }
//}
