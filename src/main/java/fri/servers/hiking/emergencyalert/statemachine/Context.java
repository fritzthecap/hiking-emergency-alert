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
import fri.servers.hiking.emergencyalert.time.AlertIntervalModel;
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
        
        final Mailer theMailer =  Objects.requireNonNull(mailer);
        theMailer.stopConfirmationPolling();
        theMailer.stopActivationPolling();
        this.mailer = theMailer;
        
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
    
    /**
     * ACTIVATION event in HikerRegistered state,
     * starts the timer that observes the planned hike times and fires time-events.
     */
    public void activateHike() {
        new Validation().assertHike(hike);
        
        if (timer.isRunning())
            throw new IllegalStateException("Timer can not be started when already running!");
        
        try { // here the mail connection login dialog may show up
            if (mailer.ensureMailConnection(hike.getAlert().getMailConfiguration()) == false)
                throw new RuntimeException("Could not send and receive mail!");
        }
        catch (MailException e) {
            throw new RuntimeException(e); // assuming user is still at the computer and sees the error
        }
        
        final Date begin;
        // begin will fire SETTING_OFF event and MUST BE AFTER activation, else state-exception, see issue #30
        final Date plannedBegin = hike.getPlannedBegin();
        final Date nowPlus4Seconds = DateUtil.addSeconds(
                DateUtil.now(), 
                Math.max(4, hike.getAlert().getMailConfiguration().getMaximumConnectionTestSeconds())); 
        begin = (plannedBegin != null && plannedBegin.after(nowPlus4Seconds)) ? plannedBegin : nowPlus4Seconds;
        
        final Date home = hike.currentDay().getPlannedHome();
        
        if (hike.isRemoteActivation())
            startHikeTimerDeferred(begin, home);
        else
            startHikeTimer(begin, home);
    }
        
    /** @return true when timer is running, i.e. ACTIVATION already took place. */
    public boolean isRunning() {
        return timer.isRunning() || mailer.isActivationPolling() || mailer.isConfirmationPolling();
    }
    
    /** COMING_HOME or ALERT_CONFIRMED, stops all timers and thus all observations. */
    public void stop() {
        timer.stop();
        mailer.stopActivationPolling();
        mailer.stopConfirmationPolling();
    }
    
    /**
     * First OVERDUE_ALERT OnTheWay, checks whether hiker has stopped overdue alerts
     * by an activation reply. Mind that this can be called just once per hike day,
     * because the activation reply will be deleted from INBOX when found.
     * @return if found a confirmation mail (from hiker),
     *      returns FALSE when having more hike days, or TRUE when not,
     *      else returns null when no confirmation mail (from hiker) was found.
     */
    public Boolean alertsStoppedByHiker() {
        if (findAlertStopReply()) {
            stop();
            
            if (hike.hasMoreDays()) {
                timerContinue();
                return Boolean.FALSE; // stay OnTheWay
            }
            else {
                System.out.println("You have prevented alerts via mail, detected at "+DateUtil.now4Log());
                return Boolean.TRUE;
            }
        }
        return null;
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
                
                if (previousContact != null && // tell previous contact about skip, in 1 second
                        hike.getAlert().isUsePassingToNextMail() &&
                        previousContact.getMailAddress().equalsIgnoreCase(currentContact.getMailAddress()) == false)
                    timer.runInSeconds(() -> sendPassingToNext(previousContact), 1);
                
                if (isFirstCall)
                    mailer.startConfirmationPolling(
                            stateMachine,
                            hike.uniqueMailId,
                            hike.getAlert().getMailConfiguration(),
                            hike.getAlert().getConfirmationPollingMinutes());
                
                if (contactIndex < alertContacts.size()) // a next contact exists
                    System.out.println("Next contact will be alerted at "+DateUtil.toString(timer.getNextOverdueAlertTime()));
                else
                    System.out.println("Polling will be stopped at "+DateUtil.toString(timer.getNextOverdueAlertTime()));
            }
        }
        else { // here it is 1 hour after last contact, it makes no sense to poll anymore
            mailer.afterNextUnsuccessfulConfirmationPoll(() -> {
                stop();
                System.out.println("Having no more contacts to alert at "+DateUtil.now4Log());
                return Boolean.FALSE;
            });
        }
    }

    /**
     * ALERT_CONFIRMED event in OverdueAlert state, alert-confirmation mail arrived from polling.
     * @return true when mail came from hiker himself and hike has more days,
     *      false when mail came from a contact or there are no more hike days.
     */
    public boolean hikerConfirmedAndHavingMoreDays() {
        stop(); // stops timer and confirmation polling
        
        final Mail confirmation = (Mail) eventParameter;
        final boolean mailIsFromHiker =  confirmation.from().equalsIgnoreCase(
                hike.getAlert().getMailConfiguration().getMailFromAddress());
                
        if (mailIsFromHiker && hike.hasMoreDays()) {
            timerContinue(); // was replied by hiker himself, skip to next day
            return true;
        }
        
        userInterface.showConfirmMail(confirmation);
        return false; // no more hike days, or one of the contacts replied
    }

    /** 'Home Again' button pushed in OnTheWay state. */
    public void comingHomeInTime() {
        stop();
        System.out.println("You are back in time, congratulations! It is "+DateUtil.now4Log());
    }

    /** 'Home Again' button pushed in OverdueAlert state. */
    public void comingHomeTooLate() {
        stop();
        System.out.println(
                "You are back too late, it is "+DateUtil.nowString()+
                ". Mail has been sent already to "+contactIndex+" contact(s).");
    }

    // privates
    
    /** Called just on first hike-day when remote activation was chosen. */
    private void startHikeTimerDeferred(final Date begin, final Date home) {
        sendActivationMessage(home, 0, true); // throws exception when not sent
        
        mailer.startActivationPolling(
                (mail) -> {
                    mailer.stopActivationPolling();
                    startHikeTimer(begin, home);
                },
                hike.uniqueMailId,
                hike.getAlert().getMailConfiguration(),
                hike.getAlert().getConfirmationPollingMinutes(),
                home);
    }

    /** Called just on first hike-day, either from remote activation or when NO remote activation was chosen. */
    private void startHikeTimer(Date begin, Date home) {
        activationOutputs(begin, home); // just once per hike
        
        timerStart(begin, home, 0); // 0 is first day
        
        System.out.println("Do NOT terminate this application before you are back!");
        System.out.println("Wish you luck, please click 'Home Again' as soon as you are back.");
    }

    private void timerStart(Date begin, Date home, int dayIndex) {
        activationTime = DateUtil.now();
        
        sendActivationMessage(home, dayIndex, false);
        // this gives the hiker a chance to stop alerts at any time before overdue time
        
        timer.start(
                begin,
                home,
                new AlertIntervalModel(hike),
                stateMachine);
    }
    
    private void timerContinue() { // make sure stop() was called before!
        contactIndex = 0; // reset contact list to head
        
        final int dayIndex = hike.skipDay(); // switch to next hike day, 0-n
        
        final Date home = hike.currentDay().getPlannedHome();
        System.out.println("Skipping to next hike day "+(dayIndex + 1)+" that ends at "+DateUtil.toString(home));
        
        timerStart(null, home, dayIndex);
    }

    private void sendActivationMessage(Date home, int dayIndex, boolean remoteActivation) {
        final String mailType = (remoteActivation ? "remote " : "day "+(dayIndex + 1)+" ")+"activation";
        System.out.println("Trying to send "+mailType+" mail at "+DateUtil.now4Log());
        try {
            mailer.sendActivation(hike, home, dayIndex, remoteActivation);
            
            System.out.println("Sending succeeded at "+DateUtil.now4Log());
            if (remoteActivation)
                System.out.println("Do not forget to reply to this mail, else observation would NOT start!");
        }
        catch (MailSendException e) {
            if (remoteActivation) // Remote activation requires the actionvation-mail!
                throw new IllegalStateException(e);
            else
                System.out.println("Sending "+mailType+" mail failed, error was "+e);
                // As this is sent immediately when activating the hike, 
                // it is assumed that the mail connection works and no send-repeat is needed.
                // For follower days there may be other mails in INBOX that can be used to reply,
                // the needed MAIL-ID is the same for all.
        }
    }
    
    private boolean findAlertStopReply() {
        try {
            final boolean hikerPreventedAlerts = mailer.findAlertStopReply(
                    hike.getAlert().getMailConfiguration(),
                    hike.uniqueMailId,
                    activationTime);
            
            if (hikerPreventedAlerts)
                System.out.println("Hiker prevented alerts by replying to activation mail, detected at "+DateUtil.now4Log());
            
            return hikerPreventedAlerts;
        }
        catch (MailReceiveException e) {
            System.err.println("ERROR: failed to find activation reply, at "+DateUtil.now4Log()+", error: "+e.toString());
            return false;
        }
    }
    
    private boolean sendAlertMessage(Contact contact) {
        System.out.println("Trying to send alert mail to "+contact.getMailAddress()+" at "+DateUtil.now4Log());
        try {
            mailer.sendAlert(contact, hike);
            
            System.out.println("Sending succeeded!");
            return true;
        }
        catch (MailSendException e) {
            System.out.println("Sending alert mail failed, error was "+e);
            
            // repeat send attempt when delay is before next overdue alert time
            final int minutesBeforeNextOverdue = FAILURE_REPEAT_MINUTES + 2; // 2 minutes safety offset
            final Date repeatUntilDate = DateUtil.addMinutes(timer.getOverdueAlertTime(), -minutesBeforeNextOverdue);
            
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
        System.out.println("Trying to send passing-to-next mail to "+previousContact.getMailAddress()+" at "+DateUtil.now4Log());
        try {
            mailer.sendPassingToNext(previousContact, hike);
            
            System.out.println("Sending succeeded!");
        }
        catch (MailSendException e) {
            System.out.println("Sending passing-to-next mail failed, error was "+e);
            // as this is a follower mail in sendAlertMessage(), do not try to repeat sending! 
        }
    }
    
    private void activationOutputs(Date begin, Date home) {
        System.out.println("Emergency alerts would start at "+DateUtil.toString(home, true));
        
        final int pollingMinutes = hike.getAlert().getConfirmationPollingMinutes();
        System.out.println("Confirmation reply polling interval is "+pollingMinutes+" minutes.");
        System.out.println("First receive attempt would be at "+DateUtil.addMinutes(home, pollingMinutes)+".");
    }
}