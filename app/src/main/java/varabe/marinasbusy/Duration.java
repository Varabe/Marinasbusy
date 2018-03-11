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
    private static int getDurationLengthCoefficient(char durationType) {
        // TODO: Find better names for those concepts
        switch (durationType) {
            case ('S'): return 1000;
            case ('M'): return 60000;
            case ('H'): return 360000;
            case ('D'): return 8640000;
            case ('W'): return 60480000;
            default: return 0;
        }
    }
}
