package fri.servers.hiking.emergencyalert.persistence;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import fri.servers.hiking.emergencyalert.Version;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Day;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.Platform;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Builds mail representations used by this application.
 */
public class MailBuilder
{
    public static final String MACRO_CONTACT = "$contact";
    public static final String MACRO_NEXT_CONTACT = "$nextContact";
    public static final String MACRO_ALL_CONTACTS = "$allContacts";
    public static final String MACRO_ME = "$me";
    public static final String MACRO_MY_PHONE = "$phone";
    public static final String MACRO_BEGIN_TIME = "$begin";
    public static final String MACRO_END_TIME = "$end";

    private static final String CONTENT_TYPE = "text/plain; charset="+Platform.ENCODING;
    
    private final Contact contact;
    private final Hike hike;
    
    public MailBuilder(Contact contact, Hike hike) {
        this.contact = contact;
        this.hike = hike;
    }

    
    /** This is sent when hike is activated. */
    public Mail buildSetOffMail(Date plannedHome) {
        final String subject = i18n("Your hike started!");
        
        final StringBuilder textBuilder = new StringBuilder();
        
        textBuilder.append(getContactTitle(contact)+" !\n\n");
        
        final String overdueDate = DateUtil.toString(plannedHome);
        textBuilder.append(
                i18n("You can block alert mails by responding to this mail before")+" "+overdueDate+".\n"+
                i18n("The MAIL-ID below must be contained as text or attachment.")+"\n"+
                i18n("Good luck!"));
        textBuilder.append("\n\n");
        textBuilder.append("MAIL-ID: "+hike.uniqueMailId);
        textBuilder.append("\n");
        footerBottom(textBuilder);
        
        return new Mail(from(), to(), subject, textBuilder.toString(), CONTENT_TYPE, null, null);
    }
    
    /** This is sent when hike is overdue. */
    public Mail buildAlertMail() {
        final String subject = subject();
        final String text = buildAlertMailText(-1);
        final List<File> attachments = buildAttachments(-1);
        
        return new Mail(from(), to(), subject, text, CONTENT_TYPE, attachments, null);
    }

    public String buildAlertMailText(int dayIndex) {
        final StringBuilder textBuilder = new StringBuilder();
                
        textBuilder.append(getContactTitle(contact)+" !\n\n");
        textBuilder.append(substitute(hike.getAlert().getHelpRequestIntroduction()));
        textBuilder.append("\n\n");
        textBuilder.append("MAIL-ID: "+hike.uniqueMailId);
        textBuilder.append("\n\n");
        
        if (hike.getAlert().getProcedureTodos() != null) {
            for (int i = 0; i < hike.getAlert().getProcedureTodos().size(); i++) {
                final String procedureTodo = hike.getAlert().getProcedureTodos().get(i);
                textBuilder.append("("+(i + 1)+") "+substitute(procedureTodo)+"\n");
            }
            textBuilder.append("\n");
        }
        
        final Day day = (dayIndex < 0) ? hike.currentDay() : hike.getDays().get(dayIndex);
        textBuilder.append(
                i18n("Route")+": "+
                (day.getRoute() != null ? "\n"+day.getRoute() : i18n("See attached images"))
                +"\n");
        
        footer(hike, textBuilder);
        
        return textBuilder.toString();
    }

    public List<File> buildAttachments(int dayIndex) {
        final Day day = (dayIndex < 0) ? hike.currentDay() : hike.getDays().get(dayIndex);
        final List<String> routeImages = day.getRouteImages();
        final List<File> attachments = new ArrayList<>();
        if (routeImages != null) {
            for (String routeImage : routeImages) {
                final File file = new File(routeImage);
                if (file.isFile())
                    attachments.add(file);
                else
                    System.err.println("ERROR: Could not find attachment file "+routeImage);
            }
        }
        return attachments;
    }

    /** This is sent when a contact did not respond in time. */
    public Mail buildPassingToNextMail() {
        final String subject = "FWD: "+subject();
        
        final StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(getContactTitle(contact)+" !\n\n");
        textBuilder.append(substitute(hike.getAlert().getPassingToNextText())+"\n");
        footer(hike, textBuilder);

        return new Mail(from(), to(), subject, textBuilder.toString(), CONTENT_TYPE, null, null);
    }

    
    private String subject() {
        return substitute(hike.getAlert().getHelpRequestSubject());
    }
    
    private String from() {
        return hike.getAlert().getMailConfiguration().getMailFromAddress();
    }

    private String to() {
        return contact.getMailAddress();
    }

    private String getContactTitle(Contact contact) {
        return StringUtil.isNotEmpty(contact.getFirstName()) ? contact.getFirstName()
                : StringUtil.isNotEmpty(contact.getLastName()) ? contact.getLastName() 
                : contact.getMailAddress();
    }

    private void footer(Hike hike, final StringBuilder textBuilder) {
        textBuilder.append("\n----------------------------------------\n");
        textBuilder.append(hike.getAlert().getNameOfHiker()+"\n"); // never null
        
        final String addressOfHiker = hike.getAlert().getAddressOfHiker();
        if (StringUtil.isNotEmpty(addressOfHiker))
            textBuilder.append(addressOfHiker+"\n");
        
        final String mailFromAddress = hike.getAlert().getMailConfiguration().getMailFromAddress();
        if (StringUtil.isNotEmpty(mailFromAddress))
            textBuilder.append(mailFromAddress);
        
        footerBottom(textBuilder);
    }
    
    private void footerBottom(StringBuilder textBuilder) {
        textBuilder.append("\n----------------------------------------\n");
        textBuilder.append(i18n("Sent by Hiking-Emergency-Alert automation ")+Version.get());
    }
    
    private String substitute(String text) {
        final String phoneNumber = hike.getAlert().getPhoneNumberOfHiker();
        final String plannedBegin = (hike.getPlannedBegin() != null) ? DateUtil.toString(hike.getPlannedBegin()) : "";
        return text
                .replace(MACRO_CONTACT, getContactName(this.contact))
                .replace(MACRO_NEXT_CONTACT, getNextContactName())
                .replace(MACRO_ALL_CONTACTS, getAllContactNames())
                .replace(MACRO_ME, hike.getAlert().getNameOfHiker()) // never null
                .replace(MACRO_MY_PHONE, StringUtil.isNotEmpty(phoneNumber) ? phoneNumber : "")
                .replace(MACRO_BEGIN_TIME, plannedBegin)
                .replace(MACRO_END_TIME, DateUtil.toString(hike.currentDay().getPlannedHome())); // never null
    }

    private String getContactName(Contact contact) {
        final String fullName = 
                (StringUtil.isNotEmpty(contact.getFirstName()) ? contact.getFirstName() : "")+
                " "+
                (StringUtil.isNotEmpty(contact.getLastName()) ? contact.getLastName() : "")
            .trim();
        return StringUtil.isNotEmpty(fullName) ? fullName : contact.getMailAddress();
    }

    private String getNextContactName() {
        boolean found = false;
        for (Contact c : hike.getAlert().getAlertContacts()) {
            if (this.contact == c)
                found = true;
            else if (found)
                return getContactName(c);
        }
        return ""; // last
    }
    
    private String getAllContactNames() {
        final String suffix = ", ";
        String all = "";
        for (Contact c : hike.getAlert().getAlertContacts())
            all += getContactName(c)+suffix;
        if (all.length() > 0)
            all = all.substring(0, all.length() - suffix.length());
        return all;
    }
}