package fri.servers.hiking.emergencyalert.mail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

public abstract class AbstractGreenmailTest
{
    protected static GreenMail greenMail;
    
    @BeforeAll
    public static void startInternalMailServer() {
        greenMail = new GreenMail(ServerSetupTest.SMTP_POP3_IMAP);
        greenMail.start(); // else java.net.ConnectException: Connection refused
    }
    
    @AfterEach
    public void removeAllMailsFromServer() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }
    
    @AfterAll
    public static void stopInternalMailServer() throws FolderException {
        greenMail.stop();
    }
}