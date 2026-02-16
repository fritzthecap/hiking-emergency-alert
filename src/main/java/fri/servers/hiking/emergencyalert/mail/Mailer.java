package fri.servers.hiking.emergencyalert.mail;

import java.util.Date;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import jakarta.mail.Authenticator;

/**
 * Messaging communication responsibilities.
 * Sends alert mails to contacts. Receives, by own INBOX polling,
 * mails that contain the <code>uniqueMailId</code> from Hike.
 */
public interface Mailer
{
    /** Clients implement this to receive mailer events. */
    public interface EventDispatcher
    {
        void dispatchEvent(Event event, Object parameter);
    }

    /**
     * Performs a connection test against given configuration. 
     * Validates the authenticator and thus must be called before any other method here!
     * @param mailConfiguration the MailConfiguration from Hike Alert.
     * @throws MailException when mail connection failed.
     */
    public boolean ensureMailConnection(MailConfiguration mailConfiguration) throws MailException;
    
    /**
     * This is for the user interface that wants to avoid repeated password dialogs.
     * A Mailer lives as long as its StateMachine lives.
     * @param authenticator a predefined positively tested authenticator to use instead of 
     *      repeatedly calling <code>ensureMailConnection()</code>.
     */
    public void setCheckedAuthenticator(Authenticator authenticator);

    /** Sends a set-off mail to the hiker, containing the MAIL-ID of the hike. */
    public void sendSetOff(Hike hike, Date plannedHome) throws MailSendException;

    /**
     * Finds and deletes a response to the set-off mail.
     * @param sentAfterDate the minimum "Sent"-time of the mail.
     * @return true when a response to the set-off mail can be found in INBOX, 
     *      containing the MAIL-ID of the hike.
     */
    public boolean findSetOffResponse(
            MailConfiguration mailConfiguration, 
            String uniqueMailId, 
            Date sentAfterDate) throws MailReceiveException;

    /**
     * Sends a mail, built from given hike, to given contact.
     * @param contact where to send the mail to.
     * @param hike data for building the mail text and route-attachment.
     */
    public void sendAlert(Contact contact, Hike hike) throws MailSendException;

    /**
     * Sends a passing-mail, built from given hike, to given previous contact.
     * @param contact where to send the mail to.
     * @param alert contains the <code>passingToNextText</code> and mail-configuration.
     */
    public void sendPassingToNext(Contact previousContact, Hike hike) throws MailSendException;
    
    /**
     * Tries to receive an alert-confirmation-mail containing the 
     * <code>uniqueMailId</code> of given hike.
     * @param eventDispatcher the StateMachine that will receive Event.ALERT_CONFIRMED.
     * @param uniqueMailId the <code>uniqueMailId</code> or "MAIL-ID" from Hike.
     * @param mailConfiguration the MailConfiguration from Hike Alert.
     * @param pollingMinutes the number of minutes to poll on INBOX for alert confirmations.
     */
    public void startConfirmationPolling(
            EventDispatcher eventDispatcher,
            String uniqueMailId, 
            MailConfiguration mailConfiguration,
            int pollingMinutes);

    /** Stops receive-polling, or does nothing when not polling. */
    public void stopConfirmationPolling();

    /** @return true when receive-polling is still running. */
    public boolean isPolling();
}