package fri.servers.hiking.emergencyalert.time;

import java.util.ArrayList;
import java.util.List;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;

/**
 * Calculation of alert times from a Hike.
 */
public class AlertIntervalModel
{
    private int alertIntervalMinutes;
    private final float alertIntervalShrinking;
    private final List<Integer> contactDetectionMinutes;
    private final boolean useContactDetectionMinutes;
    
    private int index = 0;

    /** Constructor for dispatching a Hike. */
    public AlertIntervalModel(Hike hike) {
        this(
            hike.getAlert().getAlertIntervalMinutes(),
            hike.getAlert().getAlertIntervalShrinking(),
            new ArrayList<>(),
            hike.getAlert().isUseContactDetectionMinutes()
        );
        
        if (useContactDetectionMinutes)
            for (Contact contact : hike.getAlert().getNonAbsentContacts())
                contactDetectionMinutes.add((contact.getDetectionMinutes() > 0)
                        ? contact.getDetectionMinutes()
                        : alertIntervalMinutes);
        
    }
    
    /** Constructor for validation of UI fields. */
    public AlertIntervalModel(int alertIntervalMinutes, float alertIntervalShrinking) {
        this(alertIntervalMinutes, alertIntervalShrinking, null, false);
    }
    
    private AlertIntervalModel(
            int alertIntervalMinutes,
            float alertIntervalShrinking,
            List<Integer> contactDetectionMinutes,
            boolean useContactDetectionMinutes)
    {
        this.alertIntervalMinutes = alertIntervalMinutes;
        this.alertIntervalShrinking = alertIntervalShrinking;
        this.contactDetectionMinutes = contactDetectionMinutes;
        this.useContactDetectionMinutes = useContactDetectionMinutes;
    }

    /** Every call skips to a possibly different minutes amount. */
    public int nextIntervalMinutes() {
        final int minutes = nextIntervalMinutes(true);
        
        if (alertIntervalShrinking > 0f) {
            // in any case shrink alertIntervalMinutes for the case that contact-index runs out of bounds
            final int minutesToSubtract = Math.round((float) alertIntervalMinutes * alertIntervalShrinking);
            alertIntervalMinutes = Math.max(1, Math.round((float) alertIntervalMinutes - minutesToSubtract));
            // never get smaller than one minute
        }
        
        return minutes;
    }
    
    /** The next minutes amount, without skipping to next. */
    public int pendingIntervalMinutes() {
        return nextIntervalMinutes(false);
    }
    
    private int nextIntervalMinutes(boolean increment) {
        final int minutes;
        if (useContactDetectionMinutes && index < contactDetectionMinutes.size()) {
            minutes = contactDetectionMinutes.get(index);
            if (increment)
                index++;
        }
        else {
            minutes = alertIntervalMinutes;
        }
        return minutes;
    }
}