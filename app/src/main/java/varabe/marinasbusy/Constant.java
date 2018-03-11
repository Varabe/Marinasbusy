package varabe.marinasbusy;

import java.util.TimeZone;

/**
 * Created by varabe on 3/11/18.
 */

public class Constant {
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final long
            MILLISECONDS_IN_SECOND = 1000,
            MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60,
            MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60,
            MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24,
            MILLISECONDS_IN_WEEK = MILLISECONDS_IN_DAY * 7;
}
