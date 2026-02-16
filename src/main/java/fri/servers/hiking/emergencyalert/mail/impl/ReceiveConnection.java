package fri.servers.hiking.emergencyalert.mail.impl;

import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Store;

public class ReceiveConnection extends MailSessionFactory
{
    /**
     * Clients that want to loop through INBOX implement
     * this interface and call <code>receive()</code>.
     */
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
     * @param inboxVisitor the object to loop through INBOX.
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
                
                // use indexes to avoid bulk mail loading
                // mails are counted from 1 to n, 1 is oldest, n is newest
                for (int i = messageCount; nextPlease && i > 0; i--)
                    nextPlease = inboxVisitor.visitMail(inbox.getMessage(i));
            }
        }
        catch (Exception e) {
            exception = e;
        }
        finally {
            closeInboxAndStore(inbox, store);
        }
        
        if (exception != null)
            throw new MailReceiveException(exception);
        else
            this.authenticator = sessionAndAuth.authenticator();
        
        return sessionAndAuth;
    }

    private void closeInboxAndStore(Folder inbox, Store store) {
        try {
            if (inbox != null)
                inbox.close(true); // true: expunge DELETED messages
        }
        catch (Exception e) { // ignore
            System.err.println("INBOX close failed: "+e.toString());
        }
        
        try {
            if (store != null)
                store.close();
        }
        catch (Exception e) { // ignore
            System.err.println("Store close failed: "+e.toString());
        }
    }
}