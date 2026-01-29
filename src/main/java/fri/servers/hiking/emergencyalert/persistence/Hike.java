package fri.servers.hiking.emergencyalert.persistence;

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

    /** Date-oriented suggestion for <code>plannedBegin</code> and <code>plannedHome</code>. */
    private final transient Date suggestedStart = DateUtil.eraseSeconds(DateUtil.addHours(DateUtil.now(), 1));
    private final transient Date suggestedEnd = DateUtil.addHours(suggestedStart, 12);
    
    private String route; // description text
    private List<String> routeImages = new ArrayList<>(); // paths of image files
    
    private Date plannedBegin = suggestedStart; // timer starts here
    private Date plannedHome = suggestedEnd; // timer begins alerting here
    
    private Alert alert = new Alert();
    
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
    
    public Alert getAlert() {
        return alert;
    }
    public void setAlert(Alert alert) {
        this.alert = alert;
    }
}