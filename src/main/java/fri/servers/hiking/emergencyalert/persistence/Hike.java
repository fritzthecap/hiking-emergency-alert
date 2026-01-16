package fri.servers.hiking.emergencyalert.persistence;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Top of the hiking data hierarchy. */
public class Hike
{
    /** 
     * Will be in mail text, identifies mails referring to a certain Hike.
     * Would be something like "ace5f4c0-b02b-4d8c-acc9-0bb7e8702560" (length 36).
     * Is transient to avoid GSON serialization and deserialization.
     */
    public final transient String uniqueMailId = UUID.randomUUID().toString();

    private String route; // description text
    private List<String> routeImages; // paths of image files
    private Date plannedBegin; // timer starts here
    private Date plannedHome; // timer begins alerting here
    private Integer alertIntervalMinutes = 60; // alerting interval
    private Float alertIntervalShrinking = 1.0f; // how the interval gets smaller over time
    private int confirmationPollingMinutes = 2; // confirmation polling interval
    private Alert alert;
    
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public List<String> getRouteImages() {
        return routeImages;
    }
    public void setRouteImages(List<String> routeImages) {
        this.routeImages = routeImages;
    }
    public Date getPlannedBegin() {
        return plannedBegin;
    }
    public void setPlannedBegin(Date plannedBegin) {
        this.plannedBegin = plannedBegin;
    }
    public Date getPlannedHome() {
        return plannedHome;
    }
    public void setPlannedHome(Date plannedHome) {
        this.plannedHome = plannedHome;
    }
    
    public Integer getAlertIntervalMinutes() {
        return alertIntervalMinutes;
    }
    public void setAlertIntervalMinutes(Integer messageIntervalMinutes) {
        this.alertIntervalMinutes = messageIntervalMinutes;
    }
    public Float getAlertIntervalShrinking() {
        return alertIntervalShrinking;
    }
    public void setAlertIntervalShrinking(Float alertIntervalShrinking) {
        this.alertIntervalShrinking = alertIntervalShrinking;
    }
    public int getConfirmationPollingMinutes() {
        return confirmationPollingMinutes;
    }
    public void setConfirmationPollingMinutes(int confirmationPollingMinutes) {
        this.confirmationPollingMinutes = confirmationPollingMinutes;
    }
    
    public Alert getAlert() {
        return alert;
    }
    public void setAlert(Alert alert) {
        this.alert = alert;
    }
}