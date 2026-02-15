package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.util.Objects;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Validation carried out by the StateMachine on ACTIVATION event.
 * Does not include MailConfiguration validation, as this is done
 * directly by trying to connect.
 * <p/>
 * TODO: validation logic has been duplicated in UI classes
 *       on <code>validateFields()</code>!
 */
public class Validation
{
    /** Ensures that the given hike can be dispatched successfully */
    public void assertHike(Hike hike) {
        Objects.requireNonNull(hike);
        
        if (hike.getPlannedHome() == null)
            throw new IllegalArgumentException(
                    "Planned end of hike is missing!");
            
        if (hike.getPlannedHome().after(DateUtil.now()) == false)
            throw new IllegalArgumentException(
                    "Planned end of hike is before current time: "+DateUtil.toString(hike.getPlannedHome()));
            
        if (hike.getPlannedBegin() != null && false == hike.getPlannedHome().after(hike.getPlannedBegin()))
            throw new IllegalArgumentException(
                    "The hike's planned begin at "+DateUtil.toString(hike.getPlannedBegin())+
                    " is not before end at "+DateUtil.toString(hike.getPlannedHome()));
            
        if (hike.getAlert().getNonAbsentContacts().size() <= 0)
            throw new IllegalArgumentException(
                    "Having no contacts to notify about accident, either all are absent or list is empty!");
            
        if (false == hike.getAlert().isUseContactDetectionMinutes() && hike.getAlert().getAlertIntervalMinutes() <= 0)
            throw new IllegalArgumentException(
                    "The overdue alert interval must be greater zero when not using contact detection minutes!");
            
        if (hike.getAlert().getConfirmationPollingMinutes() <= 0)
            throw new IllegalArgumentException(
                    "The confirmation polling interval must be greater zero!");
        
        if (false == hike.getAlert().isUseContactDetectionMinutes() && 
                hike.getAlert().getAlertIntervalMinutes() <= hike.getAlert().getConfirmationPollingMinutes())
            throw new IllegalArgumentException(
                    "The overdue alert interval must be longer than the confirmation polling interval, but "+
                    hike.getAlert().getAlertIntervalMinutes()+" <= "+
                    hike.getAlert().getConfirmationPollingMinutes());
        
        if (StringUtil.isEmpty(hike.getRoute()) && hike.getRouteImages() == null)
            throw new IllegalArgumentException(
                    "The hiking route must be described either as text or by image!");
                
        if (StringUtil.isEmpty(hike.getAlert().getHelpRequestSubject()))
            throw new IllegalArgumentException(
                    "The subject for the overdue alert must not be empty!");
                
        if (StringUtil.isEmpty(hike.getAlert().getHelpRequestIntroduction()) && 
                hike.getAlert().getProcedureTodos().size() <= 0)
            throw new IllegalArgumentException(
                    "Either the overdue alert text or procedure steps must be given!");
                
        if (hike.getAlert().isUsePassingToNextMail() && StringUtil.isEmpty(hike.getAlert().getPassingToNextText()))
            throw new IllegalArgumentException(
                    "The passing-to-next text must not be empty!");
                
        if (hike.getRouteImages() != null)
            for (String routeImage : hike.getRouteImages())
                if (new File(routeImage).isFile() == false)
                    throw new IllegalArgumentException(
                            "Attachment file not found: "+routeImage);
    }
}