package fri.servers.hiking.emergencyalert.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil
{
    public static final String DATE_FORMAT_MINUTES = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_SECONDS = "yyyy-MM-dd HH:mm:ss";
    
    /** @return the millisecond-precise now-date. */
    public static Date now() {
        return new Date();
    }

    /** @return the minute-precise now-date as text. */
    public static String nowString() {
        return nowString(false);
    }
    
    /** @return the minute- or second-precise now-date as text. */
    public static String nowString(boolean withSeconds) {
        return DateUtil.toString(DateUtil.now(), withSeconds);
    }
    
    /** @return the minute of given date. */
    public static int getMinute(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }
    
    /** @return the now-date with given minutes added. */
    public static Date addMinutes(int minutes) {
        return addMinutes(DateUtil.now(), minutes);
    }
    
    /** @return a date with given minutes added to given date. */
    public static Date addMinutes(Date date, int minutes) {
        return addSeconds(date, minutes * 60);
    }
    
    /** @return a date with given seconds added to given date. */
    public static Date addSeconds(Date date, int seconds) {
        final long nextMillis = date.getTime() + ((long) seconds * 1000L);
        return new Date(nextMillis);
    }

    /** @return a date string according to DATE_FORMAT_MINUTES. */
    public static String toString(Date date) {
        return toString(date, false);
    }

    /** @return a date string according to DATE_FORMAT_MINUTES or DATE_FORMAT_SECONDS. */
    public static String toString(Date date, boolean withSeconds) {
        final DateFormat format = new SimpleDateFormat(withSeconds ? DATE_FORMAT_SECONDS : DATE_FORMAT_MINUTES);
        return format.format(date);
    }
    
    /** @return a date with zero hours, the day at 00:00 o'clock. */
    public static Date eraseHours(Date date) {
        return erase(date, 
                Calendar.HOUR_OF_DAY,
                Calendar.MINUTE,
                Calendar.SECOND, 
                Calendar.MILLISECOND);
    }

    /** @return a date with zero seconds. */
    public static Date eraseSeconds(Date date) {
        return erase(date, 
                Calendar.SECOND, 
                Calendar.MILLISECOND);
    }

    /** @return a date with zero milliseconds. */
    public static Date eraseMilliseconds(Date date) {
        return erase(date, Calendar.MILLISECOND);
    }
    
    private static Date erase(Date date, int... calendarFieldNumbers) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for (int calendarFieldNumber : calendarFieldNumbers)
            calendar.set(calendarFieldNumber, 0);
        return calendar.getTime();
    }
    
//  /** @return a date with given parameters. */
//  public static Date date(int year, int month1ToN, int day1ToN, int hour, int minute) {
//      final LocalDateTime dateTime = LocalDateTime.of(year, month1ToN, day1ToN, hour, minute);
//      return Date.from(
//              dateTime
//                  .atZone(ZoneId.systemDefault())
//                  .toInstant()
//          );
//  }
//  
//  /** @return minute-precise now-date with given minutes added. */
//  public static Date date(int addMinutes) {
//      final Calendar calendar = Calendar.getInstance();
//      final int year = calendar.get(Calendar.YEAR);
//      final int month = calendar.get(Calendar.MONTH) + 1;
//      final int day = calendar.get(Calendar.DAY_OF_MONTH);
//      final int hour = calendar.get(Calendar.HOUR_OF_DAY);
//      final int minute = calendar.get(Calendar.MINUTE);
//      return date(year, month, day, hour, minute + addMinutes);
//  }
  
    
    private DateUtil() {} // do not instantiate
}
