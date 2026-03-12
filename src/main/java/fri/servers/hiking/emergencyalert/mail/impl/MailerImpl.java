package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
    private ActivationPolling activationPolling;
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
    public void sendActivation(Hike hike, Date plannedHome, int dayIndex, boolean remoteActivation) throws MailSendException {
        final Contact hikerContact = new Contact();
        hikerContact.setLastName(hike.getAlert().getNameOfHiker());
        hikerContact.setMailAddress(hike.getAlert().getMailConfiguration().getMailFromAddress());
        
        final Mail mail = new MailBuilder(hikerContact, hike)
                .buildActivationMail(plannedHome, dayIndex, remoteActivation);
        
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
    public boolean findAlertStopReply(
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
        final Mail alertStopReply = receiveConnection.searchNonSelfSentMailHavingMailId();
        return (alertStopReply != null);
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
    public void afterNextUnsuccessfulConfirmationPoll(Supplier<Boolean> pollingStopper) {
        if (confirmationPolling != null)
            confirmationPolling.afterNextUnsuccessfulConfirmationPoll(pollingStopper);
    }

    @Override
    public boolean isConfirmationPolling() {
        return isPolling(confirmationPolling);
    }

    @Override
    public void stopConfirmationPolling() {
        stopPolling(confirmationPolling, "alert confirmation");
        confirmationPolling = null;
    }
    

    @Override
    public void startActivationPolling(
            Consumer<Mail> toBeCalledWhenReceived, 
            String uniqueMailId,
            MailConfiguration mailConfiguration, 
            int pollingMinutes, 
            Date homeTime)
    {
        if (activationPolling != null)
            throw new IllegalStateException("Receive polling already started!");
        
        (activationPolling = newActivationPolling()).start(
                uniqueMailId,
                mailConfiguration,
                authenticator,
                pollingMinutes,
                homeTime,
                alertSendResults,
                toBeCalledWhenReceived);
    }

    @Override
    public boolean isActivationPolling() {
        return isPolling(activationPolling);
    }

    @Override
    public void stopActivationPolling() {
        stopPolling(activationPolling, "activation");
        activationPolling = null;
    }

    
    /** Factory method for MailConnectionCheck, to be overridden by unit-tests. */
    protected ConnectionCheck newConnectionCheck(MailConfiguration mailConfiguration) {
        return new ConnectionCheck(mailConfiguration);
    }

    /** Factory method for ConfirmationPolling, to be overridden by unit-tests. */
    protected ConfirmationPolling newConfirmationPolling() {
        return new ConfirmationPolling();
    }

    /** Factory method for ActivationPolling, to be overridden by unit-tests. */
    protected ActivationPolling newActivationPolling() {
        return new ActivationPolling();
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
     * the <code>uniqueMailId</code> from Hike can be contained in a self-alert mail too,
     * so collect and check against Message-IDs of all mails that were sent to own mail account,
     * and don't consider a self-alert to be an alert-confirmation!
     */
    private void sendMail(Mail mail, MailConfiguration mailConfiguration) throws MailSendException {
        final SendConnection sendConnection = newSendConnection(mailConfiguration, authenticator);
        final SendConnection.SendResult sendResult = sendConnection.send(mail);
        if (sendResult != null)
            alertSendResults.add(sendResult);
    }
    
    private boolean isPolling(AbstractPolling polling) {
        return polling != null && polling.isRunning();
    }

    private void stopPolling(AbstractPolling polling, String pollingType) {
        if (polling != null) {
            polling.stop();
            System.out.println("Polling for "+pollingType+" stopped at "+DateUtil.now4Log());
        }
    }

}