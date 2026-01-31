package fri.servers.hiking.emergencyalert.statemachine;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.AbstractGreenmailTest;
import fri.servers.hiking.emergencyalert.mail.GreenmailTestMailer;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailBuilder;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.mail.impl.MessageUtil;
import fri.servers.hiking.emergencyalert.persistence.TestData;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.states.OverdueAlert;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.time.ImpatientTimer;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/** Integration test that uses realistic mailing. */
class StateMachineGreenmailTest extends AbstractGreenmailTest
{
    private static final String MAIL_USER = TestData.ME_MYSELF;
    private static final String MAIL_PASSWORD = "me_myself_password";
    
    @BeforeEach
    public void setMailUser() {
        greenMail.setUser(MAIL_USER, MAIL_PASSWORD); // else "Unable to find 'some_user'"
    }
    
    @Test
    void hikeObservationWithMailsShouldWork() throws Exception {
        final Hike hike = new TestData().newHike();
        hike.setRouteImages(null); // attachment file would not be found
        
        final Mailer mailer = new GreenmailTestMailer(
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
        user.registerHike(hike); // save data
        user.activateHike(hike); // save data and start timer
        
        // from here on timer should spool further events, wait for stateMachine to terminate
        while (stateMachine.isRunning())
            Thread.sleep(1000);
        
        // no confirmation mail was sent/received in this test, so end state is "Having no more contacts"
        assertEquals(OverdueAlert.class, stateMachine.getState().getClass());
        
        assertResults(hike);
    }
    
    private void assertResults(Hike hike) throws MessagingException, IOException {
        final MimeMessage[] mailsToHiker = 
                greenMail.getReceivedMessagesForDomain(TestData.ME_MYSELF); // got no mails
        final MimeMessage[] mailsToFirstPerson = 
                greenMail.getReceivedMessagesForDomain(TestData.FIRST_PERSON); // got 2
        final MimeMessage[] mailsToSecondPerson = 
                greenMail.getReceivedMessagesForDomain(TestData.SECOND_PERSON); // got 1
        
        // assert number of sent mails
        final List<Contact> alertContacts = hike.getAlert().getAlertContacts();
        final int expectedMails = (alertContacts.size() * 2 - 1); // -1: last gets no passing-to-next mail
        assertEquals(
                expectedMails, 
                mailsToHiker.length + mailsToFirstPerson.length + mailsToSecondPerson.length);
        assertEquals(0, mailsToHiker.length);
        assertEquals(2, mailsToFirstPerson.length);
        assertEquals(1, mailsToSecondPerson.length);
        
        final MimeMessage firstMail = mailsToFirstPerson[0]; // alert to "First Person"
        final MimeMessage secondMail = mailsToSecondPerson[0]; // alert to "Second Person"
        final MimeMessage thirdMail = mailsToFirstPerson[1]; // passing-to-next message to "First Person"
        
        // assert 'from' address
        final String hikerMail = hike.getAlert().getMailConfiguration().getMailFromAddress();
        assertEquals(hikerMail, MessageUtil.from(firstMail));
        assertEquals(hikerMail, MessageUtil.from(secondMail));
        assertEquals(hikerMail, MessageUtil.from(thirdMail));
        
        // all sent dates must be distinct
        assertNotEquals(firstMail.getSentDate(), secondMail.getSentDate());
        assertNotEquals(firstMail.getSentDate(), thirdMail.getSentDate());
        assertNotEquals(secondMail.getSentDate(), thirdMail.getSentDate());
        
        // check correctness of 'to' addresses
        final Contact firstContact = alertContacts.get(0);
        final Contact secondContact = alertContacts.get(1);
        
        assertEquals(firstContact.getMailAddress(), MessageUtil.to(firstMail));
        assertEquals(secondContact.getMailAddress(), MessageUtil.to(secondMail));
        assertEquals(firstContact.getMailAddress(), MessageUtil.to(thirdMail)); // passing-to-next message
        
        // check correctness of mail texts
        final String firstText = MessageUtil.textContent(firstMail).trim();
        final String secondText = MessageUtil.textContent(secondMail).trim();
        final String thirdText = MessageUtil.textContent(thirdMail).trim();
        
        // Mail starts with "First!"
        assertTrue(firstText.startsWith(firstContact.getFirstName()));
        assertTrue(firstText.contains(hike.uniqueMailId));
        assertTrue(secondText.startsWith(secondContact.getFirstName()));
        assertTrue(secondText.contains(hike.uniqueMailId));
        assertTrue(thirdText.startsWith(firstContact.getFirstName()));
        assertFalse(thirdText.contains(hike.uniqueMailId)); // passing-to-next message doesn't have that
        
        assertTrue(replaceNewlines(firstText).contains(hike.getAlert().getHelpRequestIntroduction()));
        assertTrue(replaceNewlines(secondText).contains(hike.getAlert().getHelpRequestIntroduction()));
        assertTrue(replaceNewlines(thirdText).contains(hike.getAlert().getPassingToNextText()));
    }

    private String replaceNewlines(final String mailText) {
        return mailText.replace("\r\n", "\n"); // mails use WINDOWS newlines
    }
}