package fri.servers.hiking.emergencyalert.mail.impl;

import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;

public class ReceiveConnection extends MailSessionFactory
{
    public interface InboxVisitor
    {
        /** @return true when visitor also wants to visit mails in INBOX. */
        boolean visitInbox(Folder inbox) throws Exception;
        
        /** @return true when visitor wants to visit the next mail in INBOX. */
        boolean visitMail(Message mail) throws Exception;
    }
    
    protected final MailConfiguration mailConfiguration;
    private Authenticator authenticator;
    
    public ReceiveConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        this.mailConfiguration = mailConfiguration;
        this.authenticator = authenticator;
    }
    
    /**
     * Tries to connect to the configured mail-store and loop through mails.
     * @param inboxVisitor the obect to loop through INBOX.
     * @return the used mail session and its authenticator.
     * @throws MailReceiveException when connection fails.
     */
    public SessionWithAuthenticator receive(InboxVisitor inboxVisitor) throws MailReceiveException {
        final SessionWithAuthenticator sessionAndAuth = newSession(mailConfiguration, authenticator, false);
        // only AFTER mail action it will be known whether authenticator was valid
        
        Store store = null;
        Folder inbox = null;
        Exception exception = null;
        try  {
            store = sessionAndAuth.session().getStore();
            store.connect();
            
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            
            if (inboxVisitor.visitInbox(inbox)) {
                final int messageCount = inbox.getMessageCount();
                boolean nextPlease = true;
                for (int i = 0; nextPlease && i < messageCount; i++) // use indexes to avoid bulk mail loading
                    nextPlease = inboxVisitor.visitMail(inbox.getMessage(i + 1)); // messages start at index 1
            }
        }
        catch (Exception e) {
            exception = e;
        }
        finally {
            try {
                if (inbox != null)
                    inbox.close(true); // true: expunge DELETED messages
                if (store != null)
                    store.close();
            }
            catch (MessagingException e) { // ignore
            }
        }
        
        if (exception != null)
            throw new MailReceiveException(exception);
        else
            this.authenticator = sessionAndAuth.authenticator();
        
        return sessionAndAuth;
    }
}