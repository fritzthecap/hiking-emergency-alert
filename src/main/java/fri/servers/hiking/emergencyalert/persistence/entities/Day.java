package fri.servers.hiking.emergencyalert.persistence.entities;

import java.util.Date;
import java.util.List;

/**
 * Hikes over several days may require end-time and route description for every day.
 */
public class Day
{
    private Date plannedHome; // timer begins alerting here
    private String route; // description text
    private List<String> routeImages; // paths of image files
    
    public Date getPlannedHome() {
        return plannedHome;
    }
    public void setPlannedHome(Date plannedHome) {
        this.plannedHome = plannedHome;
    }
    
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
}