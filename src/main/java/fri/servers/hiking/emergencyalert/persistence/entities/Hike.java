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

    // properties visible in JSON
    
    private Date plannedBegin = DateUtil.eraseSeconds(DateUtil.now());
    
    private boolean remoteActivation; // = false by default
    
    private transient int dayIndex; // transient would not get persisted
    private List<Day> days = new ArrayList<>();
    
    {   // make sure there is at least one day in list
        final Day day = new Day();
        day.setPlannedHome(DateUtil.addHours(plannedBegin, 12));
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
    
    public boolean isRemoteActivation() {
        return remoteActivation;
    }
    public void setRemoteActivation(boolean remoteActivation) {
        this.remoteActivation = remoteActivation;
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
    /** 
     * Skips current to the next day if there is any.
     * @return the new index 0-n, or the old when no more days.
     */
    public int skipDay() {
        if (hasMoreDays())
            dayIndex++;
        return dayIndex;
    }
    /** @return the day at index <code>dayIndex</code>. */
    public Day currentDay() {
        return days.get(dayIndex);
    }
    /** @return the last day in list. */
    public Day lastDay() {
        return days.get(days.size() - 1);
    }
}