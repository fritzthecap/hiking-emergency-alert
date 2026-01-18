package fri.servers.hiking.emergencyalert.mail;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.impl.ConnectionCheck;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.SwingUserInterface;

class MailConnectionCheckTest
{
    record HostPortProtocol(String host, int port, String protocol)
    {
    }
    
    @Test
    @Disabled("because needs password dialog")
    void mailConnectionShouldWork() {
        boolean popTest = driveTest(new HostPortProtocol("pop.chello.at", 110, "pop3"));
        boolean imapTest = driveTest(new HostPortProtocol("pop.chello.at", 143, "imap"));
        assertTrue(popTest);
        assertTrue(imapTest);
    }
    
    private boolean driveTest(HostPortProtocol test) {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        
        mailConfiguration.setMailUser("fritz.ritzberger@chello.at");
        
        mailConfiguration.setSendMailFromAccount("fritz.ritzberger@chello.at");
        mailConfiguration.setSendMailProtocol("smtp");
        mailConfiguration.setSendMailHost("smtp.chello.at");
        mailConfiguration.setSendMailPort(25);
        
        mailConfiguration.setReceiveMailProtocol(test.protocol);
        mailConfiguration.setReceiveMailHost(test.host);
        mailConfiguration.setReceiveMailPort(test.port);
        
        new SwingUserInterface(); // needed to initialize password dialog
        final ConnectionCheck check = new ConnectionCheck(mailConfiguration);
        
        try {
            final boolean success = check.trySendAndReceive();
            return success;
        }
        catch (MailException e) {
            e.printStackTrace();
            return false;
        }
    }

}