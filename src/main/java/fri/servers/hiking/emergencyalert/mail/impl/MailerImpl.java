package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.HashSet;
import java.util.Set;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailBuilder;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection.SendResult;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

public class MailerImpl implements Mailer
{
    private Authenticator authenticator; // reuse successful authenticator for all actions
    private ConfirmationPolling confirmationPolling;
    private Set<SendResult> alertSendResults = new HashSet<>();
    
    @Override
    public void ensureMailConnection(MailConfiguration mailConfiguration) throws MailException {
        final MailConnectionCheck check = newMailConnectionCheck(mailConfiguration);
        try {
            check.tryToConnect(); // was successful when no exception was thrown
            this.authenticator = check.getValidAuthenticator(); // now we have a reusable password holder
        }
        catch (MailException e) {
            throw e;
        }
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
    protected MailConnectionCheck newMailConnectionCheck(MailConfiguration mailConfiguration) {
        return new MailConnectionCheck(mailConfiguration);
    }

    /** Factory method for ConfirmationPolling, to be overridden by unit-tests. */
    protected ConfirmationPolling newConfirmationPolling() {
        return new ConfirmationPolling();
    }

    /** Factory method for SendConnection, to be overridden by unit-tests. */
    protected SendConnection newSendConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        return new SendConnection(mailConfiguration, authenticator);
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