package fri.servers.hiking.emergencyalert.mail.impl;

import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;

public class MailConnectionCheck extends ReceiveConnection
{
    private Authenticator authenticator;
    
    public MailConnectionCheck(MailConfiguration mailConfiguration) {
        super(mailConfiguration, null);
    }
    
    /** Tries to connect to the store of given mail connection. */
    public void tryToConnect() throws MailException {
        System.out.println("Trying to connect to mail server ...");
        
        final InboxVisitor visitor = new InboxVisitor() {
            @Override
            public boolean visitInbox(Folder inbox) throws Exception {
                System.out.println("Mail connection was established successfully!");
                return false; // do not visit mails
            }
            @Override
            public boolean visitMail(Message mail) throws Exception {
                return false;
            }
        };
        
        final SessionWithAuthenticator sessionAndAuth = receive(visitor);
        
        // when no exception was thrown by receive(), we have a valid authenticator
        authenticator = sessionAndAuth.authenticator();
    }
    
    /** @return a valid authenticator. */
    public final Authenticator getValidAuthenticator() {
        return authenticator;
    }
}