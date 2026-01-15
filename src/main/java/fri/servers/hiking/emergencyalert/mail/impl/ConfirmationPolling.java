package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.mail.impl.ReceiveConnection.InboxVisitor;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection.SendResult;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import fri.servers.hiking.emergencyalert.time.Scheduler;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;

/**
 * Polls on mail-connection for an alert-confirmation mail
 * coming from outside.
 */
public class ConfirmationPolling extends Scheduler
{
    private Mailer.EventDispatcher eventDispatcher;
    private InboxVisitorConnection receiveConnection;
    private int pollingMinutes;
    
    public void start(
            String uniqueMailId, 
            MailConfiguration mailConfiguration, 
            Authenticator authenticator,
            int pollingMinutes,
            Set<SendConnection.SendResult> sendResultsLive,
            Mailer.EventDispatcher eventDispatcher)
    {
        if (receiveConnection != null)
            throw new IllegalStateException("Can not start polling again!");
        
        this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
        this.pollingMinutes = pollingMinutes;
        
        final Date pollingStartTime = DateUtil.now();
        
        this.receiveConnection = newInboxVisitorConnection(
                mailConfiguration, 
                authenticator, 
                uniqueMailId,
                pollingStartTime,
                sendResultsLive);
        
        final Date firstPolling = DateUtil.addMinutes(pollingStartTime, pollingMinutes);
        super.start(scheduler -> 
            scheduler.schedule(buildTask(), firstPolling));
        
        System.out.println(
                "Polling for alert confirmations will start in "+pollingMinutes+
                " minute(s), now it is "+DateUtil.nowString());
    }
    
    /** Factory method for InboxVisitorConnection, to be overridden by unit-tests. */
    protected InboxVisitorConnection newInboxVisitorConnection(
            MailConfiguration mailConfiguration,
            Authenticator authenticator, 
            String uniqueMailId, 
            Date pollingStartTime, 
            Set<SendResult> sendResultsLive)
    {
        return new InboxVisitorConnection(
                mailConfiguration,
                authenticator, 
                uniqueMailId, 
                pollingStartTime, 
                sendResultsLive);
    }


    public static class InboxVisitorConnection extends ReceiveConnection implements InboxVisitor
    {
        private final String uniqueMailId;
        private final Date pollingStartTime;
        private final Set<SendConnection.SendResult> sendResultsLive;
        
        private Mail found;
        
        public InboxVisitorConnection(
                MailConfiguration mailConfiguration, 
                Authenticator authenticator,
                String uniqueMailId,
                Date pollingStartTime,
                Set<SendConnection.SendResult> sendResultsLive)
        {
            super(mailConfiguration, authenticator);
            
            this.uniqueMailId = Objects.requireNonNull(uniqueMailId);
            this.pollingStartTime = pollingStartTime;
            this.sendResultsLive = sendResultsLive;
        }

        public Mail searchAlertConfirmation() throws MailReceiveException {
            System.out.println("Polling tries to receive an alert confirmation at "+DateUtil.nowString()+" ...");
            receive(this);
            return found;
        }
        
        @Override
        public boolean visitInbox(Folder inbox) throws Exception {
            return true;// continue to mail messages
        }
        
        @Override
        public boolean visitMail(Message message) throws Exception {
            final Date sentDate = DateUtil.eraseMilliseconds(message.getSentDate());
            if (sentDate.after(pollingStartTime)) { // must be a new mail
                final String messageId = MessageUtil.messageId(message);
                final String text = MessageUtil.textContent(message); // fetches text also from attached mails
                
                if (isAlertConfirmation(sentDate, messageId, text)) {
                    found = new Mail(
                                MessageUtil.from(message), 
                                message.getSubject(), 
                                sentDate);
                    return false; // breaks mail loop
                }
            }
            return true; // continues mail loop
        }

        private boolean isAlertConfirmation(Date sentDate, String messageId, String text) {
            if (text.contains(uniqueMailId) == false)
                return false; // uniqueMailId is NOT in text
            
            // check whether mail has been sent by MailerImpl, such can not be an alert-confirmation
            for (SendConnection.SendResult selfSentResult : sendResultsLive) {
                final boolean selfSentDate = sentDate.equals(selfSentResult.sentDate());
                final boolean selfSentMessageId = selfSentResult.messageId().equals(messageId);
                if (selfSentDate || selfSentMessageId)
                    return false; // either messageId or sentDate are identical with a self-sent mail
            }
            
            return true; // found alert-confirmation
        }
    }
    

    private TimerTask buildTask() {
        return new TimerTask() {
            @Override
            public void run() {
                receiveAlertConfirmation();
            }
        };
    }
    
    private void receiveAlertConfirmation() {
        try {
            final Mail confirmation = receiveConnection.searchAlertConfirmation();
            if (confirmation != null) { // found an alert confirmation in INBOX
                System.out.println("Received alert confirmation from "+confirmation.from()+" at "+DateUtil.nowString());
                eventDispatcher.dispatchEvent(Event.ALERT_CONFIRMED, confirmation);
            }
            else {
                continuePolling(null);
            }
        }
        catch (MailReceiveException e) {
            continuePolling(e);
        }
    }

    private void continuePolling(Exception e) {
        final String errorMessage = (e != null) ? (e.toString()+"\n") : "";
        System.out.println(
                errorMessage+
                "Found no alert confirmation at "+DateUtil.nowString()+
                ", continue polling in "+pollingMinutes+" minutes.");
        
        synchronizedOnScheduler(scheduler ->
            scheduler.schedule(buildTask(), DateUtil.addMinutes(DateUtil.now(), pollingMinutes)));
    }
}