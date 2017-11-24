package varabe.marinasbusy;

class Time {
    // Produces a bug if event starts and ends on different days
    private int hours;
    private int minutes;
    private int seconds;
    private static int SECONDS_IN_HOUR = 3600;
    private static int SECONDS_IN_MINUTE = 60;

    Time(long milliseconds) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        hours = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        minutes = calendar.get(java.util.Calendar.MINUTE);
        seconds = calendar.get(java.util.Calendar.SECOND);
    }
    public String toString() {
        String minutes = this.minutes + "";
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        return String.format("%s:%s", hours+"", minutes);
    }
    boolean isAfter(Time time) {
        return this.getInt() > time.getInt();
    }
    boolean isBefore(Time time) {
        return this.getInt() < time.getInt();
    }
    long getInt() {
        return (hours * SECONDS_IN_HOUR) + (minutes * SECONDS_IN_MINUTE) + seconds;
    }
}
