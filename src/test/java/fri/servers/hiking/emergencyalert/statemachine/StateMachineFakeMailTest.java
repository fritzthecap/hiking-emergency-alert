package fri.servers.hiking.emergencyalert.statemachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.TestData;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.time.ImpatientTimer;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import jakarta.mail.Authenticator;

/** Weak test that uses fake-mailing. */
class StateMachineFakeMailTest
{
    /** Checks the number of sent mails for a hike. */
    @Test
    void numberAndContentOfSentMails() throws Exception {
        final Hike hike = new TestData().newHike();
        hike.currentDay().setRouteImages(null); // attachment file would not be found
        final List<Contact> contacts = hike.getAlert().getAlertContacts();
        final int numberOfContacts = contacts.size();
        
        final List<Mail> sentMails = new ArrayList<>(); // collect sent mails
        final Mailer mailer = fakeMailer(sentMails);

        final HikeTimer timer = new ImpatientTimer();
        
        final UserInterface user = new UserInterface() {
            @Override
            public void showConfirmMail(Mail alertConfirmationMail) {
                // will not be called in this test
            }
        };
        
        final StateMachine stateMachine = new StateMachine(hike, mailer, timer, user);
        
        // simulate user-changes during running state-machine
        final Hike changedHike = new TestData().newHike();
        changedHike.currentDay().setRouteImages(null); // attachment file would not be found
        
        // do not change number of contacts, see numberOfContacts above!
        changedHike.getAlert().setAddressOfHiker("Walkerstreet 1, D-1234 Walkertown, Germany");
        user.registerHike(changedHike); // publish alert change
        
        changedHike.currentDay().setRoute("From Kilimanjaro to Mount Everest via Greenland");
        user.activateHike(changedHike); // publish hike change
        
        // from here on timer should spool further events, wait for it to terminate
        while (stateMachine.isRunning())
            Thread.sleep(1000);

        final int expectedNumberOfSentMails = (2 * numberOfContacts) - 1;
        // for every mail there is also a passingToNext mail, except for the first
        assertEquals(expectedNumberOfSentMails, sentMails.size());
        
        // assert mail to first contact
        assertEquals(
                new Mail(contacts.get(0).getMailAddress(), "Alert", changedHike.currentDay().getRoute()), 
                sentMails.get(0));
        // assert mail to second contact
        assertEquals(
                new Mail(contacts.get(1).getMailAddress(), "Alert", changedHike.currentDay().getRoute()), 
                sentMails.get(1));
        // assert passing-to-next mail to first contact after alert to second contact
        assertEquals(
                new Mail(contacts.get(0).getMailAddress(), "PassingToNext", changedHike.getAlert().getPassingToNextText()), 
                sentMails.get(2));
    }
    
    
    private Mailer fakeMailer(final List<Mail> sentMails) {
        return new Mailer()
        {
            private boolean polling = false;
            
            @Override
            public boolean ensureMailConnection(MailConfiguration mailConfiguration) throws MailException {
                return true;
            }
            @Override
            public void setCheckedAuthenticator(Authenticator authenticator) {
            }
            @Override
            public void sendAlert(Contact contact, Hike hike) throws MailSendException {
                sentMails.add(new Mail(
                        contact.getMailAddress(), "Alert", hike.currentDay().getRoute()));
            }
            @Override
            public void sendPassingToNext(Contact previousContact, Hike hike) throws MailSendException {
                sentMails.add(new Mail(
                        previousContact.getMailAddress(), "PassingToNext", hike.getAlert().getPassingToNextText()));
            }
            @Override
            public void startConfirmationPolling(
                    Mailer.EventDispatcher dispatcher,
                    String uniqueMessageIdentifier, 
                    MailConfiguration mailConfiguration,
                    int pollingMinutes)
            {
                polling = true;
                System.err.println("Confirmation polling starts ...");
            }
            @Override
            public void stopConfirmationPolling() {
                polling = false;
                System.err.println("Confirmation polling stopped");
            }
            @Override
            public boolean isPolling() {
                return polling;
            }
            @Override
            public void sendSetOff(Hike hike, Date plannedHome, int dayIndex) throws MailSendException {
            }
            @Override
            public boolean findSetOffResponse(
                    MailConfiguration mailConfiguration, 
                    String uniqueMailId, 
                    Date sentAfterDate) throws MailReceiveException {
                return false;
            }
        };
    }
}