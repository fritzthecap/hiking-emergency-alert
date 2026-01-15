package fri.servers.hiking.emergencyalert.mail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.impl.MailConnectionCheck;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.SwingUserInterface;

class MailConnectionCheckTest
{
    @Test
    @Disabled("because needs password dialog")
    void mailConnectionShouldWork() {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        
        mailConfiguration.setMailUser("fritz.ritzberger@chello.at");
        
        mailConfiguration.setSendMailFromAccount("fritz.ritzberger@chello.at");
        mailConfiguration.setSendMailProtocol("smtp");
        mailConfiguration.setSendMailHost("smtp.chello.at");
        mailConfiguration.setSendMailPort(25);
        
        mailConfiguration.setReceiveMailProtocol("imap");
        mailConfiguration.setReceiveMailHost("pop.chello.at");
        mailConfiguration.setReceiveMailPort(143);
        
        new SwingUserInterface(); // needed to initialize password dialog
        final MailConnectionCheck check = new MailConnectionCheck(mailConfiguration);
        
        assertDoesNotThrow(() -> check.tryToConnect());
    }
}