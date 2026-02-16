package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.impl.ReceiveConnection.InboxVisitor;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;

public class InboxVisitorConnection extends ReceiveConnection implements InboxVisitor
{
    protected final String uniqueMailId;
    protected final Date minimumSentTime;
    private final Set<SendConnection.SendResult> sendResultsLive;
    
    private Mail found;
    
    public InboxVisitorConnection(
            MailConfiguration mailConfiguration, 
            Authenticator authenticator,
            String uniqueMailId,
            Date minimumSentTime,
            Set<SendConnection.SendResult> sendResultsLive)
    {
        super(mailConfiguration, authenticator);
        
        this.uniqueMailId = Objects.requireNonNull(uniqueMailId);
        this.minimumSentTime = minimumSentTime;
        this.sendResultsLive = sendResultsLive;
    }

    /**
     * Searches in INBOX for a mail with MAIL-ID (<code>hike.uniqueMailId</code>) which was 
     * sent after <code>minimumSentTime</code> and is none of the mails in <code>sendResultsLive</code>.
     * @return mail-information, or null if no mail found.
     * @throws MailReceiveException when mail connection fails.
     */
    public Mail searchExternalMailHavingMailId() throws MailReceiveException {
        receive(this);
        return found;
    }
    
    /** Implements InboxVisitor and always returns true. */
    @Override
    public boolean visitInbox() throws Exception {
        return true; // continue to mail messages
    }
    
    /** Implements InboxVisitor to search for an alert-confirmation mail. */
    @Override
    public boolean visitMail(Message message) throws Exception {
        final Date sentDate = DateUtil.eraseMilliseconds(message.getSentDate());
        final boolean interesting = sentDate.after(minimumSentTime);
        
        if (interesting) { // must be a new mail
            final String messageId = MessageUtil.messageId(message);
            final String text = MessageUtil.textContent(message); // fetches text also from attached mails
            
            if (containsMailIdAndWasNotSelfSent(sentDate, messageId, text) && 
                    MessageUtil.isDeliveryFailedMail(message) == false)
            {
                found = new Mail(
                            MessageUtil.from(message), 
                            message.getSubject(), 
                            sentDate);
                processFoundMessage(message);
                
                return false; // breaks mail loop
            }
        }
        
        return interesting; // true continues mail loop, false breaks it
    }


    private boolean containsMailIdAndWasNotSelfSent(Date sentDate, String messageId, String text) {
        if (text.contains(uniqueMailId) == false)
            return false; // uniqueMailId is NOT in text
        
        // check whether mail has been sent by MailerImpl, such can not be an alert-confirmation
        if (sendResultsLive != null)
            for (SendConnection.SendResult selfSentResult : sendResultsLive) {
                final boolean selfSentDate = sentDate.equals(selfSentResult.sentDate());
                final boolean selfSentMessageId = selfSentResult.messageId().equals(messageId);
                if (selfSentDate || selfSentMessageId)
                    return false; // either messageId or sentDate are identical with a self-sent mail
            }
        
        return true; // found alert-confirmation
    }
    
    /**
     * Deletes given message when its "From" is the hiker's "From", else does nothing.
     * @param message the found message with uniqueMailId,
     *      while still being in receive-loop with an open message store.
     */
    private void processFoundMessage(Message message) {
        try {
            final String messageFrom = MessageUtil.from(message);
            final String hikerFrom = mailConfiguration.getMailFromAddress();
            if (hikerFrom.equalsIgnoreCase(messageFrom))
                MessageUtil.deleteMessage(message);
        }
        catch (MessagingException e) {
            System.err.println("ERROR: Could not delete message, error was "+e.toString());
        }
    }
}