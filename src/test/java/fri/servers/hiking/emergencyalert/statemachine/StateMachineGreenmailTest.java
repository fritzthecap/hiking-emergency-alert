package fri.servers.hiking.emergencyalert.statemachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.AbstractGreenmailTest;
import fri.servers.hiking.emergencyalert.mail.GreenmaiTestMailer;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailBuilder;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.mail.impl.MessageUtil;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.TestData;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.time.ImpatientTimer;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/** Integration test that uses realistic mailing. */
class StateMachineGreenmailTest extends AbstractGreenmailTest
{
    private static final String MAIL_USER = "some_user";
    private static final String MAIL_PASSWORD = "some_password";
    
    @BeforeEach
    public void setMailUser() {
        greenMail.setUser(MAIL_USER, MAIL_PASSWORD); // else "Unable to find 'some_user'"
    }
    
    @Test
    void hikeObservationWithMailsShouldWork() throws Exception {
        final Hike hike = TestData.newHike();
        hike.setRouteImages(null); // attachment file would not be found
        
        final Mailer mailer = new GreenmaiTestMailer(
                MAIL_USER, 
                MAIL_PASSWORD, 
                new ImpatientTimer().newTimer()); // must be another instance than HikeTimer!
        
        final HikeTimer timer = new ImpatientTimer();
        
        final UserInterface user = new UserInterface() {
            @Override
            public void showConfirmMail(Mail alertConfirmationMail) {
                // will not be called in this test
            }
        };
        
        final StateMachine stateMachine = new StateMachine(hike, mailer, timer, user);
        user.registerHike(hike); // publish alert change
        user.activateHike(hike); // publish hike change
        
        // from here on timer should spool further events, wait for stateMachine to terminate
        while (stateMachine.isRunning())
            Thread.sleep(1000);
        
        assertResults(hike);
    }
    
    private void assertResults(Hike hike) throws MessagingException, IOException {
        final MimeMessage[] receivedMails = greenMail.getReceivedMessages();
        
        final List<Contact> alertContacts = hike.getAlert().getHikerContact().getAlertContacts();
        final int expectedMails = (alertContacts.size() + 1); // plus 1 passing-to-next mail
        assertEquals(expectedMails, receivedMails.length);
        
        final String mailFrom = MailBuilder.from(hike);
        for (MimeMessage message : receivedMails) // all mails are from MailConfiguration
            assertEquals(mailFrom, MessageUtil.from(message));
        
        Arrays.sort( // by sent-date
                receivedMails, 
                (m1, m2) -> {
                    try {
                        return (int) (m1.getSentDate().getTime() - m2.getSentDate().getTime());
                    }
                    catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                });
        
        final MimeMessage firstMail = receivedMails[0]; // alert to "First Person"
        final MimeMessage secondMail = receivedMails[1]; // alert to "Second Person"
        final MimeMessage thirdMail = receivedMails[2]; // passing-to-next message to "First Person"
        
        final Contact firstContact = alertContacts.get(0);
        final Contact secondContact = alertContacts.get(1);
        
        assertEquals(firstContact.getMailAddress(), MessageUtil.to(firstMail));
        assertEquals(secondContact.getMailAddress(), MessageUtil.to(secondMail));
        assertEquals(firstContact.getMailAddress(), MessageUtil.to(thirdMail)); // passing-to-next message
        
        final String firstText = MessageUtil.textContent(firstMail).trim();
        final String secondText = MessageUtil.textContent(secondMail).trim();
        final String thirdText = MessageUtil.textContent(thirdMail).trim();
        
        assertTrue(firstText.startsWith(firstContact.getFirstName()));
        assertTrue(firstText.contains(hike.uniqueMailId));
        assertTrue(secondText.startsWith(secondContact.getFirstName()));
        assertTrue(secondText.contains(hike.uniqueMailId));
        assertTrue(thirdText.startsWith(firstContact.getFirstName()));
        assertFalse(thirdText.contains(hike.uniqueMailId)); // passing-to-next message doesn't have that
        
        assertTrue(replaceNewlines(firstText).contains(hike.getAlert().getHelpRequestText()));
        assertTrue(replaceNewlines(secondText).contains(hike.getAlert().getHelpRequestText()));
        assertTrue(replaceNewlines(thirdText).contains(hike.getAlert().getPassingToNextText()));
    }

    private String replaceNewlines(final String mailText) {
        return mailText.replace("\r\n", "\n"); // mails use WINDOWS newlines
    }
}