package fri.servers.hiking.emergencyalert.time;

import java.util.ArrayList;
import java.util.List;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;

/**
 * Calculation of alert times from a Hike.
 */
public class IntervalModel
{
    private int alertIntervalMinutes;
    private final float alertIntervalShrinking;
    private final List<Integer> contactDetectionMinutes;
    private final boolean useContactDetectionMinutes;
    
    private int index = 0;

    public IntervalModel(Hike hike) {
        this.alertIntervalMinutes = hike.getAlert().getAlertIntervalMinutes();
        this.alertIntervalShrinking = hike.getAlert().getAlertIntervalShrinking();
        this.contactDetectionMinutes = new ArrayList<>();
        this.useContactDetectionMinutes = hike.getAlert().isUseContactDetectionMinutes();
        
        if (useContactDetectionMinutes)
            for (Contact contact : hike.getAlert().getNonAbsentContacts())
                contactDetectionMinutes.add((contact.getDetectionMinutes() > 0)
                        ? contact.getDetectionMinutes()
                        : alertIntervalMinutes);
        
    }
    
    /** Every call may deliver a different minutes amount. */
    public int nextIntervalMinutes() {
        final int minutes;
        if (useContactDetectionMinutes && index < contactDetectionMinutes.size()) {
            minutes = contactDetectionMinutes.get(index);
            index++;
        }
        else {
            minutes = alertIntervalMinutes;
        }
        
        // in any case shrink alertIntervalMinutes for the case that contact-index runs out of bounds
        final int minutesToSubtract = Math.round((float) alertIntervalMinutes * alertIntervalShrinking);
        alertIntervalMinutes = Math.max(1, Math.round((float) alertIntervalMinutes - minutesToSubtract));
        // never get smaller than one minute
        
        return minutes;
    }
}