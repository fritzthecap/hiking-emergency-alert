package fri.servers.hiking.emergencyalert.mail;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.impl.ConnectionCheck;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.SwingUserInterface;
import jakarta.mail.Authenticator;

class ConnectionCheckTest
{
    record HostPortProtocol(String host, int port, String protocol)
    {
    }
    
    @Test
    @Disabled("because needs password dialog")
    void mailConnectionShouldWork() {
        new SwingUserInterface(); // needed to initialize password dialog
        
        Result popTest = driveTest(new HostPortProtocol("pop.chello.at", 110, "pop3"), null);
        Result imapTest = driveTest(new HostPortProtocol("pop.chello.at", 143, "imap"), popTest.authenticator());
        
        assertTrue(popTest.success());
        assertTrue(imapTest.success());
    }
    
    
    private record Result(Authenticator authenticator, boolean success)
    {
    }
    
    private Result driveTest(HostPortProtocol receiveConfiguration, Authenticator authenticator) {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        
        mailConfiguration.setMailUser("fritz.ritzberger@chello.at");
        
        mailConfiguration.setSendMailFromAccount("fritz.ritzberger@chello.at");
        mailConfiguration.setSendMailProtocol("smtp");
        mailConfiguration.setSendMailHost("smtp.chello.at");
        mailConfiguration.setSendMailPort(25);
        
        mailConfiguration.setReceiveMailProtocol(receiveConfiguration.protocol);
        mailConfiguration.setReceiveMailHost(receiveConfiguration.host);
        mailConfiguration.setReceiveMailPort(receiveConfiguration.port);
        
        final ConnectionCheck check = new ConnectionCheck(mailConfiguration, authenticator);
        
        try {
            final boolean success = check.trySendAndReceive();
            return new Result(check.getValidAuthenticator(), success);
        }
        catch (MailException e) {
            e.printStackTrace();
            return new Result(null, false);
        }
    }
}