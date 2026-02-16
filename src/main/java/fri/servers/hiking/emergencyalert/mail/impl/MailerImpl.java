package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.MailBuilder;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

public class MailerImpl implements Mailer
{
    private Authenticator authenticator; // reuse successful authenticator for all actions
    private ConfirmationPolling confirmationPolling;
    private Set<SendConnection.SendResult> alertSendResults = new HashSet<>();
    
    @Override
    public boolean ensureMailConnection(MailConfiguration mailConfiguration) throws MailException {
        if (authenticator != null) // has been set by UserInterface
            return true;
        
        final ConnectionCheck check = newConnectionCheck(mailConfiguration);
        try {
            final boolean roundTripDone = check.trySendAndReceive(); // true when mail was deleted
            authenticator = check.getValidAuthenticator(); // now we have a reusable password holder
            
            return roundTripDone;
        }
        catch (MailException e) {
            throw e;
        }
    }
    
    @Override
    public void setCheckedAuthenticator(Authenticator authenticator) {
        this.authenticator = Objects.requireNonNull(authenticator);
    }
    
    @Override
    public void sendSetOff(Hike hike, Date plannedHome) throws MailSendException {
        final Contact hikerContact = new Contact();
        hikerContact.setLastName(hike.getAlert().getNameOfHiker());
        hikerContact.setMailAddress(hike.getAlert().getMailConfiguration().getMailFromAddress());
        final Mail mail = new MailBuilder(hikerContact, hike).buildSetOffMail(plannedHome);
        sendMail(mail, hike.getAlert().getMailConfiguration());
    }
    
    @Override
    public void sendAlert(Contact contact, Hike hike) throws MailSendException {
        final Mail mail = new MailBuilder(contact, hike).buildAlertMail();
        sendMail(mail, hike.getAlert().getMailConfiguration());
    }

    @Override
    public void sendPassingToNext(Contact previousContact, Hike hike) throws MailSendException {
        final Mail mail = new MailBuilder(previousContact, hike).buildPassingToNextMail();
        sendMail(mail, hike.getAlert().getMailConfiguration());
    }

    @Override
    public boolean findSetOffResponse(
            MailConfiguration mailConfiguration, 
            String uniqueMailId, 
            Date sentAfterDate) throws MailReceiveException
    {
        final InboxVisitorConnection receiveConnection = newInboxVisitorConnection(
                mailConfiguration, 
                authenticator,
                uniqueMailId,
                sentAfterDate,
                alertSendResults);
        final Mail setOffResponse = receiveConnection.searchExternalMailHavingMailId();
        return (setOffResponse != null);
    }
    
    @Override
    public void startConfirmationPolling(
            Mailer.EventDispatcher eventDispatcher,
            String uniqueMailId, 
            MailConfiguration mailConfiguration,
            int pollingMinutes)
    {
        if (confirmationPolling != null)
            throw new IllegalStateException("Receive polling already started!");
        
        (confirmationPolling = newConfirmationPolling()).start(
                uniqueMailId,
                mailConfiguration,
                authenticator,
                pollingMinutes,
                alertSendResults,
                eventDispatcher);
    }

    @Override
    public boolean isPolling() {
        return confirmationPolling != null && confirmationPolling.isRunning();
    }

    @Override
    public void stopConfirmationPolling() {
        if (confirmationPolling != null) {
            confirmationPolling.stop();
            confirmationPolling = null;
            System.out.println("Polling for alert confirmations stopped at "+DateUtil.nowString());
        }
    }
    
    /** Factory method for MailConnectionCheck, to be overridden by unit-tests. */
    protected ConnectionCheck newConnectionCheck(MailConfiguration mailConfiguration) {
        return new ConnectionCheck(mailConfiguration);
    }

    /** Factory method for ConfirmationPolling, to be overridden by unit-tests. */
    protected ConfirmationPolling newConfirmationPolling() {
        return new ConfirmationPolling();
    }

    /** Factory method for SendConnection, to be overridden by unit-tests. */
    protected SendConnection newSendConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        return new SendConnection(mailConfiguration, authenticator);
    }
    
    /** Factory method for InboxVisitorConnection, to be overridden by unit-tests. */
    protected InboxVisitorConnection newInboxVisitorConnection(
            MailConfiguration mailConfiguration,
            Authenticator authenticator, 
            String uniqueMailId, 
            Date sentAfterDate, 
            Set<SendConnection.SendResult> sendResultsLive)
    {
        return new InboxVisitorConnection(
                mailConfiguration,
                authenticator, 
                uniqueMailId, 
                sentAfterDate, 
                sendResultsLive);
    }

    
    /**
     * Responsibility of <code>alertSendResults</code> in polling:
     * the <code>uniqueMailId</code> from Hike can be contained in a self-alert mail too, so collect
     * and check against Message-IDs of all mails that were sent to own mail account,
     * and don't consider an self-alert to be an alert-confirmation!
     */
    private void sendMail(Mail mail, MailConfiguration mailConfiguration) throws MailSendException {
        final SendConnection sendConnection = newSendConnection(mailConfiguration, authenticator);
        final SendConnection.SendResult sendResult = sendConnection.send(mail);
        if (sendResult != null)
            alertSendResults.add(sendResult);
    }
}