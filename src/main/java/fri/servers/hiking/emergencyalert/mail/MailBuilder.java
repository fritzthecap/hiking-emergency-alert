package fri.servers.hiking.emergencyalert.mail;

import static fri.servers.hiking.emergencyalert.util.StringUtil.isEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import fri.servers.hiking.emergencyalert.Version;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.Platform;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Builds mail representations used by this application.
 */
public class MailBuilder
{
    private static final String CONTENT_TYPE = "text/plain; charset="+Platform.ENCODING;
    
    /** @return the 'from' address for mails to be sent to contacts. */
    public static String from(Hike hike) {
        String from = hike.getAlert().getMailConfiguration().getSendMailFromAccount();
        if (MailUtil.isMailAddress(from))
            return from;
        
        from = hike.getAlert().getMailConfiguration().getMailUser();
        if (MailUtil.isMailAddress(from)) 
            return from;
        
        throw new IllegalArgumentException(
                "Either SendMailFromAccount or MailUser must hold a valid 'from' mail address!");
    }
    
    private final Contact contact;
    private final Hike hike;
    
    public MailBuilder(Contact contact, Hike hike) {
        this.contact = contact;
        this.hike = hike;
    }
    
    /** This is sent when hike is overdue. */
    public Mail buildAlertMail() {
        final String subject = hike.getAlert().getHelpRequestTitle();
        final String text = buildAlertMailText(hike, contact);
        final List<File> attachments = buildAttachments(hike.getRouteImages());
        final Date sentDate = sent();
        
        return new Mail(from(), to(), subject, text, CONTENT_TYPE, attachments, sentDate);
    }

    /** This is sent when overdue contact did not respond in time. */
    public Mail buildPassingToNextMail() {
        final String subject = "FWD: "+hike.getAlert().getHelpRequestTitle();
        final Date sentDate = DateUtil.addSeconds(sent(), 1); 
        // as this mail gets sent immediately after an alert mail, 
        // avoid same sent-date by adding a second.
        
        final StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(getContactTitle(contact)+" !\n");
        textBuilder.append(hike.getAlert().getPassingToNextText()+"\n");
        footer(hike, textBuilder);

        return new Mail(from(), to(), subject, textBuilder.toString(), CONTENT_TYPE, null, sentDate);
    }

    
    private String from() {
        return MailBuilder.from(hike);
    }

    private String to() {
        return contact.getMailAddress();
    }

    /** 
     * Need explicit sent-date with seconds-precision for
     * identifying self-alerts, those are not confirmations!
     */
    private Date sent() {
        final Date sentDate = DateUtil.eraseMilliseconds(DateUtil.now());
        return sentDate;
    }

    private String buildAlertMailText(Hike hike, Contact contact) {
        final StringBuilder textBuilder = new StringBuilder();
                
        textBuilder.append(getContactTitle(contact)+" !\n");
        textBuilder.append(hike.getAlert().getHelpRequestText());
        textBuilder.append("\n\n");
        textBuilder.append("MAIL-ID: "+hike.uniqueMailId);
        textBuilder.append("\n\n");
        if (hike.getAlert().getProcedureTodos() != null) {
            for (int i = 0; i < hike.getAlert().getProcedureTodos().size(); i++) {
                final String procedureTodo = hike.getAlert().getProcedureTodos().get(i);
                textBuilder.append("("+(i + 1)+") "+procedureTodo+"\n");
            }
            textBuilder.append("\n");
        }
        if (hike.getRoute() != null)
            textBuilder.append("Route:\n"+hike.getRoute()+"\n");
        footer(hike, textBuilder);
        
        return textBuilder.toString();
    }

    private String getContactTitle(Contact contact) {
        return StringUtil.isNotEmpty(contact.getFirstName()) ? contact.getFirstName()
                : StringUtil.isNotEmpty(contact.getLastName()) ? contact.getLastName() 
                : contact.getMailAddress();
    }

    private List<File> buildAttachments(List<String> routeImages) {
        final List<File> attachments = new ArrayList<>();
        if (routeImages != null) {
            for (String routeImage : routeImages) {
                final File file = new File(routeImage);
                if (file.isFile())
                    attachments.add(file);
                else
                    System.err.println("Could not find attachment file "+routeImage);
            }
        }
        return attachments;
    }

    private void footer(Hike hike, final StringBuilder sb) {
        sb.append("\n----------------------------------------\n");
        sb.append(hike.getAlert().getNameOfHiker()+"\n");
        if (isEmpty(hike.getAlert().getAddressOfHiker()) == false)
            sb.append(hike.getAlert().getAddressOfHiker()+"\n");
        if (isEmpty(hike.getAlert().getMailOfHiker()) == false)
            sb.append(hike.getAlert().getMailOfHiker());
        sb.append("\n----------------------------------------\n");
        sb.append("Sent by Hiking-Emergency-Alert automaton version "+Version.get());
    }
}