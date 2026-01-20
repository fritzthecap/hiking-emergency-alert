package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import java.util.Objects;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import jakarta.mail.Authenticator;

/**
 * Data shipped between pages.
 */
public class Trolley
{
    public final StateMachine stateMachine;
    private Authenticator authenticator;
    private Hike hikeCopy;
    
    public Trolley(StateMachine stateMachine) {
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.hikeCopy = stateMachine.getHike().copy();
    }
    
    /** Whoever has a valid authenticator can pass it to other pages. */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    /** When not null, this is a valid authenticator. */
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    
    /** @return true when the hike was changed by the UI, done by comparison with a deep clone. */
    public boolean hikeChanged() {
        return hikeCopy.isEqual(stateMachine.getHike()) == false;
    }
}