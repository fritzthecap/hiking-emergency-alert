package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import java.util.Objects;
import javax.swing.JButton;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import jakarta.mail.Authenticator;

/**
 * Data shipped between pages.
 */
public class Trolley
{
    public final StateMachine stateMachine;
    private final JButton nextButton, previousButton;
    private final String hikeCopy;
    
    private Authenticator authenticator;
    
    public Trolley(StateMachine stateMachine, JButton nextButton, JButton previousButton) {
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.nextButton = nextButton;
        this.previousButton = previousButton;
        
        this.hikeCopy = hikeToJsonString(stateMachine.getHike());
    }
    
    /** @return true when the hike was changed by the UI, done by comparison with a deep clone. */
    public boolean isHikeChanged() {
        final String currentHikeCopy = hikeToJsonString(stateMachine.getHike());
        return this.hikeCopy.equals(currentHikeCopy) == false;
    }

    /** Whoever has a valid authenticator can pass it to other pages. */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    /** When not null, this is a valid authenticator. */
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    
    public void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
    }
    public void setPreviousEnabled(boolean enabled) {
        previousButton.setEnabled(enabled);
    }
    
    private String hikeToJsonString(Hike hike) {
        return new JsonGsonSerializer<Hike>().toJson(hike);
    }
}