package fri.servers.hiking.emergencyalert.persistence.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/** Top of the hiking data hierarchy. */
public class Hike
{
    /** 
     * MAIL-ID, will be in mail text, identifies mails referring to a certain Hike.
     * Would be something like "ace5f4c0-b02b-4d8c-acc9-0bb7e8702560" (length 36).
     * Is transient to avoid GSON serialization and deserialization.
     */
    public final transient String uniqueMailId = UUID.randomUUID().toString();

    /** Suggestions for <code>plannedBegin</code> and <code>plannedHome</code>. */
    private final transient Date suggestedStart = DateUtil.eraseSeconds(DateUtil.now());
    private final transient Date suggestedEnd = DateUtil.addHours(suggestedStart, 12);
    
    // properties visible in JSON
    private Date plannedBegin = suggestedStart;
    
    private transient int dayIndex; // transient would not get persisted
    private List<Day> days = new ArrayList<>();
    
    {   // make sure there is at least one day in list
        final Day day = new Day();
        day.setPlannedHome(suggestedEnd);
        days.add(day);
    }
    
    private Alert alert = new Alert();
    
    public Date getPlannedBegin() {
        return plannedBegin;
    }
    public void setPlannedBegin(Date plannedBegin) {
        this.plannedBegin = plannedBegin;
    }
    
    public Alert getAlert() {
        return alert;
    }
    public void setAlert(Alert alert) {
        this.alert = alert;
    }
    
    public List<Day> getDays() {
        return days;
    }
    public void setDays(List<Day> days) {
        this.days = days;
    }
    
    /** @return true when current day is not the last. */
    public boolean hasMoreDays() {
        return dayIndex < days.size() - 1;
    }
    /** @return the day at index <code>dayIndex</code>. */
    public Day currentDay() {
        return days.get(dayIndex);
    }
    /** @return the last day in list. */
    public Day lastDay() {
        return days.get(days.size() - 1);
    }
    /** Skips current to the next day if there is any. */
    public void skipDay() {
        if (hasMoreDays())
            dayIndex++;
    }
}