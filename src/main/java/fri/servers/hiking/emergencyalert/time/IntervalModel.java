package fri.servers.hiking.emergencyalert.time;

import java.util.ArrayList;
import java.util.List;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;

public class IntervalModel
{
    private int alertIntervalMinutes;
    private final float alertIntervalShrinking;
    private final List<Integer> contactDetectionMinutes;
    private final boolean useContactDetectionMinutes;
    
    private int index = 0;

    public IntervalModel(Hike hike) {
        this.alertIntervalMinutes = hike.getAlertIntervalMinutes();
        this.alertIntervalShrinking = hike.getAlertIntervalShrinking();
        this.contactDetectionMinutes = new ArrayList<>();
        
        for (Contact contact : hike.getAlert().getNonAbsentContacts())
            contactDetectionMinutes.add((contact.getDetectionMinutes() > 0)
                    ? contact.getDetectionMinutes()
                    : alertIntervalMinutes);
        
        this.useContactDetectionMinutes = hike.isUseContactDetectionMinutes();
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
        
        // in any case shrink alertIntervalMinutes for the case that index runs out of bounds
        alertIntervalMinutes = Math.round((float) alertIntervalMinutes * alertIntervalShrinking);
        
        return minutes;
    }
}