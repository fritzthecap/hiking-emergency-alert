package fri.servers.hiking.emergencyalert.ui;

import java.util.Objects;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.persistence.Alert;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import jakarta.mail.Authenticator;

/**
 * User-interface that provides hike and alert data. 
 * It fires REGISTRATION, ACTIVATION and COMING_HOME events.
 */
public abstract class UserInterface
{
    /** Accessible to sub-classes only. */
    protected static Authenticator interactiveAuthenticator;
    
    /**
     * Call this to get a password dialog. 
     * You must have called at least <code>new SwingUserInterface)</code>
     * before to get a non-null interactive authenticator!
     */
    public static Authenticator getInteractiveAthenticator() {
        return interactiveAuthenticator;
    }
        
    /** Gateway to StateMachine. */
    public interface EventDispatcher
    {
        /** Tell the StateMachine to dispatch given event and parameter. */
        void dispatchEvent(Event event, Object parameter);
    }
    
    private EventDispatcher eventDispatcher;
    
    /**
     * Required call after constructor for using this class.
     * @param eventDispatcher the StateMachine.
     */
    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
    }

    /** Sub-classes must implement notification about an alert confirmation. */
    public abstract void showConfirmMail(Mail alertConfirmationMail);
    
    /** Gives the StateMachine the hiker's personal data. */
    public void registerAlert(Alert alert) {
        eventDispatcher.dispatchEvent(Event.REGISTRATION, alert);
    }
    
    /** Gives the StateMachine the hike-data. */
    public void activateHike(Hike hike) {
        eventDispatcher.dispatchEvent(Event.ACTIVATION, hike);
    }
    
    /** Stops the StateMachine. */
    public void comingHome() {
        eventDispatcher.dispatchEvent(Event.COMING_HOME, null);
    }
}