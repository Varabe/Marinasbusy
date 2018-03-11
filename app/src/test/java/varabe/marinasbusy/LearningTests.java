package varabe.marinasbusy;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.ical.compat.javautil.DateIterator;
import com.google.ical.compat.javautil.DateIteratorFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.TimeZone;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LearningTests {
    @Test
    public void DateIteratorFactory_worksAsExpected() throws Exception {
        // The Duration and Event classes were implemented here. Sadly, they left this class :(
        long startTime = Long.valueOf("1516626000000");
        String rawDuration = "P3000S";
        String rrule = "FREQ=WEEKLY;UNTIL=20180508T035959Z;WKST=MO;BYDAY=MO";
        String exrule = null;
        String exdate = null;
        String rdate = null;
    }
}