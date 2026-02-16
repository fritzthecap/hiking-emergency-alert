package fri.servers.hiking.emergencyalert.mail.impl;

import java.io.IOException;
import org.eclipse.angus.mail.dsn.DeliveryStatus;
import org.jsoup.Jsoup;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.ParseException;

public final class MessageUtil
{
    /** @return the "Message-ID" from given message, or empty string when null. */
    public static String messageId(Message message) throws MessagingException {
        return ((MimeMessage) message).getMessageID();
    }

    /** @return true when given message is not SEEN. */
    public static boolean isNew(Message message) throws MessagingException {
        return message.isSet(Flags.Flag.SEEN) == false;
    }

    /** @return mail addresses in the "from" property of given message as String. */
    public static String from(Message message) throws MessagingException {
        return join(message.getFrom());
    }
    
    /** @return mail addresses in the "from" property of given message as String. */
    public static String to(Message message) throws MessagingException {
        return join(message.getRecipients(RecipientType.TO));
    }

    /** Sets the DELETED flag onto the message, which deletes it when <code>close(true)</code> is called on its folder. */
    public static void deleteMessage(Message message) {
        try {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        catch (MessagingException e) {
            System.err.println(e.toString());
        }
    }
    
    /**
     * Loops through all parts of the given message in search of
     * content-type "message/delivery-status" and returns true
     * when it finds a recipient-notification named "Action" with value "failed".
     * @param message the mail to search for failed delivery-status.
     * @return true when delivery failed, else false.
     * 
     * @see https://www.rfc-editor.org/rfc/rfc3464#section-2.1
     * @see https://www.rfc-editor.org/rfc/rfc3464#section-2.3.3
     */
    public static boolean isDeliveryFailedMail(Message message) throws IOException, MessagingException {
        final DeliveryStatus deliveryStatus = deliveryStatus(message);
        if (deliveryStatus != null) {
            for (int i = 0; i < deliveryStatus.getRecipientDSNCount(); i++) {
                final InternetHeaders recipientDSN = deliveryStatus.getRecipientDSN(i);
                final String[] actionValues = recipientDSN.getHeader("Action");
                if (actionValues.length > 0 && 
                        actionValues[0] != null && 
                        "failed".equals(actionValues[0].toLowerCase()))
                    return true;
            }
        }
        return false;
    }
    
    /** @return the recursive text content of given part, tags from HTML-only mails will be removed. */
    public static String textContent(Message message) throws  IOException, MessagingException {
        final String textContent = textContent(message, false); // first try text/plain
        // HTML-only mails would deliver empty text now!
        return (textContent != null && textContent.length() > 0)
            ? textContent
            : textContent(message, true); // try to find text/html
    }
    
    /** @return the base content type of given part, e.g. "text/plain", without trailing extensions. */
    private static String contentType(Part part) throws ParseException, MessagingException {
        return new ContentType(part.getContentType()).getBaseType();
    }
    
    private static String textContent(Part part, boolean findHtml) throws  IOException, MessagingException {
        if (contentType(part).toLowerCase().startsWith("multipart/")) {
            final Multipart multiPart = (Multipart) part.getContent();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multiPart.getCount(); i++) {
                final String text = textContent(multiPart.getBodyPart(i), findHtml);
                if (text != null)
                    sb.append("\n"+text.trim());
            }
            return sb.toString();
        }
        else if (findHtml == false && part.isMimeType("text/plain") ||
                 findHtml == true && part.isMimeType("text/html")) {
            final String text = part.getContent().toString().trim();
            return findHtml ? Jsoup.parse(text).text() : text; // no HTML tags please
        }
        return null;
    }

    private static String join(final Address[] addresses) {
        String s = "";
        for (Address address :  addresses)
            if (address instanceof InternetAddress)
                s += ((InternetAddress) address).getAddress()+" ";
        return s.trim();
    }
    
    private static DeliveryStatus deliveryStatus(Part part) throws IOException, MessagingException {
        final String contentType = contentType(part).toLowerCase();
        if (contentType.startsWith("multipart/")) {
            final Multipart multiPart = (Multipart) part.getContent();
            for (int i = 0; i < multiPart.getCount(); i++) {
                final DeliveryStatus deliveryStatus = deliveryStatus(multiPart.getBodyPart(i));
                if (deliveryStatus != null)
                    return deliveryStatus;
            }
        }
        else if (contentType.startsWith("message/delivery-status")) {
            return (DeliveryStatus) part.getContent();
        }
        return null;
    }

    private MessageUtil() {} // do not instantiate
}