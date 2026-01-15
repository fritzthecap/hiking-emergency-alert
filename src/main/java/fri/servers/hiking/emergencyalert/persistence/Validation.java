package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;

public class Validation
{
    public void assertHike(Hike hike) {
        Objects.requireNonNull(hike);
        
        if (hike.getPlannedBegin() == null || hike.getPlannedHome() == null)
            throw new IllegalArgumentException(
                    "Planned begin or end of hike is missing!");
            
        final Date now = DateUtil.eraseHours(DateUtil.now()); // day precision only
        if (hike.getPlannedBegin().before(now))
            throw new IllegalArgumentException(
                    "Can not start a hike with planned begin in past: "+hike.getPlannedBegin()+", now is "+now);
            
        if (hike.getPlannedHome().after(hike.getPlannedBegin()) == false)
            throw new IllegalArgumentException(
                    "The hike's planned begin at "+DateUtil.toString(hike.getPlannedBegin())+
                    " is not before end at "+DateUtil.toString(hike.getPlannedHome()));
            
        if (StringUtil.isEmpty(hike.getAlert().getHikerContact().getMailAddress()))
            throw new IllegalArgumentException(
                    "Having no mail address of hiker!");
        
        int nonAbsentCount = 0;
        for (Contact contact : hike.getAlert().getHikerContact().getAlertContacts())
            if (contact.isAbsent() == false)
                nonAbsentCount++;
        
        if (nonAbsentCount <= 0)
            throw new IllegalArgumentException(
                    "Having no contacts to notify about accident, either all are absent or list is empty!");
            
        if (hike.getAlertIntervalMinutes() <= 0)
            throw new IllegalArgumentException(
                    "The overdue alert interval must be greater zero!");
            
        if (hike.getConfirmationPollingMinutes() <= 0)
            throw new IllegalArgumentException(
                    "The confirmation polling interval must be greater zero!");
        
        if (hike.getAlertIntervalMinutes() <= hike.getConfirmationPollingMinutes())
            throw new IllegalArgumentException(
                    "The overdue alert interval must be longer than the confirmation polling interval, but "+
                    hike.getAlertIntervalMinutes()+" <= "+
                    hike.getConfirmationPollingMinutes());
        
        if (StringUtil.isEmpty(hike.getRoute()) && hike.getRouteImages() == null)
            throw new IllegalArgumentException(
                    "The hiking route must be described either as text or by image!");
                
        if (StringUtil.isEmpty(hike.getAlert().getHelpRequestTitle()))
            throw new IllegalArgumentException(
                    "The subject for the overdue alert must not be empty!");
                
        if (StringUtil.isEmpty(hike.getAlert().getPassingToNextText()))
            throw new IllegalArgumentException(
                    "The passing-to-next text must not be empty!");
                
        if (hike.getRouteImages() != null)
            for (String routeImage : hike.getRouteImages())
                if (new File(routeImage).isFile() == false)
                    throw new IllegalArgumentException(
                            "Attachment file not found: "+routeImage);
    }
}