package fri.servers.hiking.emergencyalert.statemachine;

import java.util.List;
import java.util.Objects;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.Validation;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.time.IntervalModel;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/** One context for one state-machine. Most of business logic is here. */
public class Context
{
    /** Repeat interval for sending failed mails. */
    private static final int FAILURE_REPEAT_MINUTES = 5;
    
    private final StateMachine stateMachine;
    private final Mailer mailer;
    private final HikeTimer timer;
    private final UserInterface userInterface;
    
    private Hike hike;
    private Event event;
    private Object eventParameter;
    
    private int contactIndex = 0;
    
    protected Context(Hike hike, StateMachine stateMachine, Mailer mailer, HikeTimer timer, UserInterface user) {
        this.hike = Objects.requireNonNull(hike);
        this.stateMachine = Objects.requireNonNull(stateMachine);
        (this.mailer = Objects.requireNonNull(mailer)).stopConfirmationPolling();
        (this.timer = Objects.requireNonNull(timer)).stop(); // make sure it is not running due to crash
        (this.userInterface = Objects.requireNonNull(user)).setEventDispatcher(stateMachine);
    }
    
    protected Context(Context context) {
        this(context.hike, context.stateMachine, context.mailer, context.timer, context.userInterface);
    }
    
    /** StateMachine visible only. */
    Event getEvent() {
        return event;
    }
    /** StateMachine visible only. */
    void setEvent(Event event) {
        this.event = event;
    }

    /** StateMachine received a parameter for pending event. */
    void setEventParameter(Object parameter) {
        eventParameter = parameter;
    }
    
    // exposing private fields
    
    /** @return the observed Hike. */
    public Hike getHike() {
        return hike;
    }
    
    /** @return the user interface. */
    public UserInterface getUserInterface() {
        return userInterface;
    }
    
    // event methods

    /** REGISTRATION, ACTIVATION, called when an registration- or activation-event arrives. */
    public void updateHike() {
        final Hike updatedHike = (Hike) Objects.requireNonNull(eventParameter);
        hike = updatedHike;
        System.out.println("Updated Hike. MAIL-ID will be "+hike.uniqueMailId);
    }
    
    /** ACTIVATION, starts the timer that observes the planned hike times and fires time-events. */
    public void startHikeTimer() {
        new Validation().assertHike(hike);
        
        if (timer.isRunning())
            throw new IllegalStateException("Timer can be started just once!");
        
        try { // here the mail connection login dialog may show up
            mailer.ensureMailConnection(hike.getAlert().getMailConfiguration());
        }
        catch (MailException e) {
            throw new RuntimeException(e); // assuming user is still at the computer and sees the error
        }
        
        System.out.println("Planned hike set-off is "+DateUtil.toString(hike.getPlannedBegin()));
        System.out.println("Emergency alerts will start at "+DateUtil.toString(hike.getPlannedHome()));
        System.out.println("Do NOT terminate this application before you are back!");
        System.out.println("Wish you luck, please click 'Home Again' as soon as you are back.");
        timer.start(
                hike.getPlannedBegin(),
                hike.getPlannedHome(), 
                new IntervalModel(hike),
                stateMachine);
    }

    /** COMING_HOME or ALERT_CONFIRMED, stops all timers and thus all observations. */
    public void stop() {
        System.out.println("Timer stopped at "+DateUtil.nowString());
        timer.stop();
        mailer.stopConfirmationPolling();
    }
    
    /** @return true when timer is already running, i.e. ACTIVATION took place. */
    public boolean isRunning() {
        return timer.isRunning() || mailer.isPolling();
    }
    
    /**
     * OVERDUE_ALERT, sends an alert message to current contact.
     * This gets called by default in 1 hour intervals.
     * <ol>
     * <li>Set next (or first) contact as current</li>
     * <li>Send alert message to current contact</li>
     * <li>When sending was successful, send message to the previous contact
     *      (when exists) that the next contact has been alerted successfully</li>
     * <li>Initializes confirmation polling</li>
     * </ol>
     */
    public void sendAlertMessage() {
        final boolean isFirstCall = (contactIndex == 0);
        final List<Contact> alertContacts = hike.getAlert().getNonAbsentContacts();
        
        if (alertContacts.size() > contactIndex) { // having a next non-absent contact
            final Contact previousContact = (isFirstCall ? null : alertContacts.get(contactIndex - 1));
            final Contact currentContact = alertContacts.get(contactIndex);
            
            if (sendAlertMessage(currentContact)) { // mail sending worked
                contactIndex++; // skip to next contact
                
                if (previousContact != null) // tell previous contact about skip
                    sendPassingToNext(previousContact); // safe against exceptions
                
                if (isFirstCall) {
                    mailer.startConfirmationPolling(
                            stateMachine,
                            hike.uniqueMailId,
                            hike.getAlert().getMailConfiguration(),
                            hike.getConfirmationPollingMinutes());
                }
            }
        }
        else { // being 1 hour after last contact, no more contacts, stop timers
            System.out.println("Having no more contacts to alert at "+DateUtil.nowString());
            
            stop(); // 1 hour after last contact, it makes no sense to poll anymore
        }
    }

    /** ALERT_CONFIRMED, passes the alert-confirmation mail to user. */
    public void alertConfirmed() {
        userInterface.showConfirmMail((Mail) eventParameter);
    }

    
    private boolean sendAlertMessage(final Contact contact) {
        try {
            System.out.println("Trying to send alert to "+contact.getMailAddress()+" at "+DateUtil.nowString());
            mailer.sendAlert(contact, hike);
            System.out.println("Sending succeeded!");
            return true;
        }
        catch (MailSendException e) {
            failureRepeat("alert", () -> sendAlertMessage(contact), e);
            return false;
        }
    }
    
    private void sendPassingToNext(final Contact previousContact) {
        try {
            System.out.println("Trying to send passing-to-next to "+previousContact.getMailAddress()+" at "+DateUtil.nowString());
            mailer.sendPassingToNext(previousContact, hike);
            System.out.println("Sending succeeded!");
        }
        catch (MailSendException e) {
            failureRepeat("passing-to-next", () -> sendPassingToNext(previousContact), e);
        }
    }

    private void failureRepeat(String mailType, Runnable runnable, MailSendException e) {
        System.out.println("Sending "+mailType+" failed, repeating in "+FAILURE_REPEAT_MINUTES+" minutes, error was "+e);
        timer.repeatFailedEvent( // this will not be queued when timer is no more running
            runnable,
            FAILURE_REPEAT_MINUTES
        );
    }
}