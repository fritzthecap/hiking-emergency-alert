package fri.servers.hiking.emergencyalert.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/** Provides unit test data. */
public class TestData
{
    public static Hike newHike() {
        return new TestData().buildHike();
    }

    public static Alert newAlert() {
        return new TestData().buildAlert();
    }
    
    protected Hike buildHike() {
        final Hike hike = new Hike();
        
        hike.setRoute("From Mount Everest to Kilimanjaro via Antarctica");
        hike.setRouteImages(List.of("C:\\hikes\\maps\\everest-to-kilimanjaro.png"));
        
        hike.setAlertIntervalMinutes(60);
        hike.setConfirmationPollingMinutes(2);
        
        final Date plannedBegin = 
                DateUtil.eraseSeconds( // no seconds for JSON round-trip test
                    DateUtil.addMinutes(1)); // begin must be at least 1 minute in future
        final Date plannedHome = DateUtil.addMinutes(plannedBegin, 1);
        hike.setPlannedBegin(plannedBegin);
        hike.setPlannedHome(plannedHome);
        
        hike.setAlert(buildAlert());
        
        return hike;
    }
    
    protected Alert buildAlert() {
        final String helpRequestTitle = "Hiking emergency - I need help!";
        final String addressOfHiker = "Hikerstreet 1, A-1234 Hikertown, Österreich";
        final String helpRequestText = 
            "I had an accident while hiking and need help."+
            "Below you find a description of my planned route.";
        final String[] procedureTodos = new String[] {
            "Try to reach me by phone for details: 123456789.",
            "If I do not respond, please call 112 (Europe) or 140 (Austria).",
            "Ask them for a mail-address to forward this emergency alert message.",
            "If there is none, tell them my hiking-trail from description below.",
            "Also tell them my hike start date and time."
        };
        final String passingToNextText = 
            "As you did not respond within 1 hour, another alert\n"+
            "has been sent to the next contact person.\n"+
            "You can ignore the mail that was recently sent to you.";

        final Alert alert = new Alert();
        alert.setHelpRequestTitle(helpRequestTitle);
        alert.setHelpRequestText(helpRequestText);
        alert.setAddressOfHiker(addressOfHiker);
        alert.setProcedureTodos(List.of(procedureTodos));
        alert.setPassingToNextText(passingToNextText);
        
        final Contact hikerContact = buildHikerContact();
        hikerContact.setAlertContacts(buildAlertContacts());
        alert.setHikerContact(hikerContact);
        
        alert.setMailConfiguration(buildMailConfiguration());
        
        return alert;
    }
    
    private Contact buildHikerContact() {
        final Contact hikerContact = new Contact();
        hikerContact.setFirstName("Me");
        hikerContact.setLastName("Myself");
        hikerContact.setMailAddress("me.myself@alert.org");
        return hikerContact;
    }

    private List<Contact> buildAlertContacts() {
        final List<Contact> list = new ArrayList<>();
        {
            final Contact contact = new Contact();
            contact.setFirstName("First");
            contact.setLastName("Person");
            contact.setMailAddress("first.person@alert.org");
            list.add(contact);
        }
        {
            final Contact contact = new Contact();
            contact.setFirstName("Second");
            contact.setLastName("Person");
            contact.setMailAddress("second.person@alert.org");
            list.add(contact);
        }
        return list;
    }
    
    private MailConfiguration buildMailConfiguration() {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        
        mailConfiguration.setReceiveMailProtocol("pop3");
        mailConfiguration.setReceiveMailHost("pop.company.country");
        mailConfiguration.setReceiveMailPort(110);
        mailConfiguration.setMailUser("me.myself@alert.org");
        
        mailConfiguration.setSendMailProtocol("smtp");
        mailConfiguration.setSendMailHost("smtp.company.country");
        mailConfiguration.setSendMailPort(25);
        mailConfiguration.setSendMailFromAccount("me.myself@alert.org");
        
        return mailConfiguration;
    }
}