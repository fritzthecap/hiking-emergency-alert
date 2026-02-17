package fri.servers.hiking.emergencyalert.statemachine;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.Validation;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
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
    private Date activationTime;
    
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
    
    // exposing private fields to StateMachine
    
    /** @return the observed Hike. */
    Hike getHike() {
        return hike;
    }
    
    /** @return the user interface. */
    UserInterface getUserInterface() {
        return userInterface;
    }
    
    /** @return the mailer. */
    Mailer getMailer() {
        return mailer;
    }
    
    // event- and service-methods

    /** REGISTRATION, ACTIVATION, called when a registration- or activation-event arrives. */
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
            if (mailer.ensureMailConnection(hike.getAlert().getMailConfiguration()) == false)
                throw new RuntimeException("Could not send and receive mail!");
        }
        catch (MailException e) {
            throw new RuntimeException(e); // assuming user is still at the computer and sees the error
        }
        
        activationOutputs(); // just once per hike
        
        final Date begin = (hike.getPlannedBegin() != null) 
                ? hike.getPlannedBegin()
                : DateUtil.now(); // make sure SET_OFF event is fired
        final Date home = hike.currentDay().getPlannedHome();
        timerStart(begin, home);
    }

    /** @return true when timer is running, i.e. ACTIVATION already took place. */
    public boolean isRunning() {
        return timer.isRunning() || mailer.isPolling();
    }
    
    /** COMING_HOME or ALERT_CONFIRMED, stops all timers and thus all observations. */
    public void stop() {
        System.out.println("Timer stopped at "+DateUtil.nowString());
        timer.stop();
        mailer.stopConfirmationPolling();
    }
    
    /**
     * ON_THE_WAY, checks whether hiker has stopped overdue alerts by a set-off response.
     * Mind that this can be called just once per hike day, because the set-off mail will
     * be deleted when found.
     */
    public boolean alertsStoppedByHiker() {
        if (findSetOffResponse()) {
            timerContinue();
            System.out.println("Hiker stopped alerts by replying to the set-off mail, detected this at "+DateUtil.nowString());
            return true;
        }
        return false;
    }
    
    /**
     * OVERDUE_ALERT, sends an alert message to current contact.
     * Gets called by default in 1-hour intervals.
     * <ol>
     * <li>Set next (or first) contact as current</li>
     * <li>Send alert message to current contact</li>
     * <li>When sending was successful, send message to the previous contact
     *      (when exists) that the next contact has been alerted successfully</li>
     * <li>Initializes confirmation polling</li>
     * </ol>
     */
    public void sendAlertMessage() {
        final List<Contact> alertContacts = hike.getAlert().getNonAbsentContacts();
        final boolean isFirstCall = (contactIndex == 0);
        
        if (contactIndex < alertContacts.size()) { // having a next non-absent contact
            final Contact previousContact = (isFirstCall ? null : alertContacts.get(contactIndex - 1));
            final Contact currentContact = alertContacts.get(contactIndex);
            
            if (sendAlertMessage(currentContact)) { // mail sending worked
                contactIndex++; // skip to next contact
                
                if (previousContact != null && hike.getAlert().isUsePassingToNextMail())
                    // tell previous contact about skip, in 1 second
                    timer.runInSeconds(() -> sendPassingToNext(previousContact), 1);
                
                if (isFirstCall) {
                    mailer.startConfirmationPolling(
                            stateMachine,
                            hike.uniqueMailId,
                            hike.getAlert().getMailConfiguration(),
                            hike.getAlert().getConfirmationPollingMinutes());
                }
            }
        }
        else { // here it is 1 hour after last contact, it makes no sense to poll anymore
            stop();
            System.out.println("Having no more contacts to alert at "+DateUtil.nowString());
        }
    }

    /** ALERT_CONFIRMED, alert-confirmation mail arrived from polling. */
    public void alertConfirmed() {
        stop();
        
        final Mail confirmation = (Mail) eventParameter;
        userInterface.showConfirmMail(confirmation);
        
        if (confirmation.from().equalsIgnoreCase(hike.getAlert().getMailConfiguration().getMailFromAddress()))
            timerContinue(); // was sent by hiker himself, so skip to next day when existing
    }

    /** 'Home Again' button pushed in OnTheWay state. */
    public void comingHomeInTime() {
        stop();
        System.out.println("You are back in time, congratulations! It is "+DateUtil.nowString());
    }

    /** 'Home Again' button pushed in OverdueAlert state. */
    public void comingHomeTooLate() {
        stop();
        System.out.println(
                "You are back too late, it is "+DateUtil.nowString()+
                ". Mail has been sent already to "+contactIndex+" contact(s).");
    }

    
    private void timerStart(Date plannedBegin, Date plannedHome) {
        activationTime = DateUtil.now();
        
        sendSetOffMessage(plannedHome); // give the hiker a chance to stop alerts before overdue time
        
        timer.start(
                plannedBegin,
                plannedHome,
                new IntervalModel(hike),
                stateMachine);
    }
    
    private void timerContinue() {
        if (hike.hasMoreDays()) { // check for further hike days, see issue #1 
            System.out.println("Skipping to next hike day at "+DateUtil.nowString());
            
            contactIndex = 0; // reset contact list to head
            hike.skipDay(); // switch to next hike day
            
            timerStart(null, hike.currentDay().getPlannedHome());
        }
    }

    private void sendSetOffMessage(Date plannedHome) {
        System.out.println("Trying to send set-off mail at "+DateUtil.nowString());
        try {
            mailer.sendSetOff(hike, plannedHome);
            
            System.out.println("Sending succeeded!");
        }
        catch (MailSendException e) {
            System.out.println("Sending set-off mail failed, error was "+e);
            // as this is sent immediately after activating the hike, 
            // it is assumed that the mail connection works and no send-repeat is needed
        }
    }
    
    private boolean findSetOffResponse() {
        try {
            return mailer.findSetOffResponse(
                    hike.getAlert().getMailConfiguration(),
                    hike.uniqueMailId,
                    activationTime);
        }
        catch (MailReceiveException e) {
            System.err.println("ERROR: failed to find set-off response, at "+DateUtil.nowString()+", error: "+e.toString());
            return false;
        }
    }
    
    private boolean sendAlertMessage(Contact contact) {
        System.out.println("Trying to send alert mail to "+contact.getMailAddress()+" at "+DateUtil.nowString());
        try {
            mailer.sendAlert(contact, hike);
            
            System.out.println("Sending succeeded!");
            return true;
        }
        catch (MailSendException e) {
            System.out.println("Sending alert mail failed, error was "+e);
            
            // repeat send attempt when delay is before next overdue alert time
            final int minutesBeforeNextOverdue = FAILURE_REPEAT_MINUTES + 2; // 2 minutes safety offset
            final Date repeatUntilDate = DateUtil.addMinutes(timer.getNextOverdueAlertTime(), -minutesBeforeNextOverdue);
            
            if (DateUtil.now().before(repeatUntilDate)) {
                System.out.println("Will repeat send attempt in "+FAILURE_REPEAT_MINUTES+" minutes.");
                
                final Runnable runnable = () -> sendAlertMessage(); // contactIndex needs to be increased! 
                timer.runInSeconds(runnable, FAILURE_REPEAT_MINUTES * 60);
                // this will NOT repeat when scheduler has been stopped meanwhile!
            }
            
            return false;
        }
    }
    
    private void sendPassingToNext(Contact previousContact) {
        System.out.println("Trying to send passing-to-next mail to "+previousContact.getMailAddress()+" at "+DateUtil.nowString());
        try {
            mailer.sendPassingToNext(previousContact, hike);
            
            System.out.println("Sending succeeded!");
        }
        catch (MailSendException e) {
            System.out.println("Sending passing-to-next mail failed, error was "+e);
            // as this is a follower mail in sendAlertMessage(), do not try to repeat sending! 
        }
    }
    
    private void activationOutputs() {
        if (hike.getPlannedBegin() != null)
            System.out.println("Planned hike set-off is "+DateUtil.toString(hike.getPlannedBegin()));
        System.out.println("Emergency alerts would start at "+DateUtil.toString(hike.currentDay().getPlannedHome()));
        System.out.println("Do NOT terminate this application before you are back!");
        System.out.println("Wish you luck, please click 'Home Again' as soon as you are back.");
    }
}