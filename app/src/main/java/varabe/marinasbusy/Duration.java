package varabe.marinasbusy;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Duration {
    /*
     * Java.time.Duration works only for new API, which I didn't target
     * Even if I could use it, it works on slightly different RFC rules than calendar DB
     */
    public static final long
            MILLISECONDS_IN_SECOND = 1000,
            MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60,
            MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60,
            MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24,
            MILLISECONDS_IN_WEEK = MILLISECONDS_IN_DAY * 7;
    static long toMilliseconds(String rawDuration) {
        List<String> matches = new ArrayList<String>();
        Pattern PATTERN = Pattern.compile("\\d+([SMHDW])");
        Matcher matcher = PATTERN.matcher(rawDuration);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        int duration = 0;
        for (String match: matches) {
            duration += evaluateDurationPart(match);
        }
        return duration;
    }
    private static long evaluateDurationPart(String durationPart) {
        // Accepts strings like "3000S"
        int lastCharIndex = durationPart.length() - 1;
        char durationType = durationPart.charAt(lastCharIndex);
        int durationAmount = Integer.valueOf(durationPart.substring(0, lastCharIndex));
        return durationAmount * getDurationLengthCoefficient(durationType);
    }
    private static long getDurationLengthCoefficient(char durationType) {
        // TODO: Find better names for those concepts
        switch (durationType) {
            case ('S'): return MILLISECONDS_IN_SECOND;
            case ('M'): return MILLISECONDS_IN_MINUTE;
            case ('H'): return MILLISECONDS_IN_HOUR;
            case ('D'): return MILLISECONDS_IN_DAY;
            case ('W'): return MILLISECONDS_IN_WEEK;
            default: return 0;
        }
    }
}
