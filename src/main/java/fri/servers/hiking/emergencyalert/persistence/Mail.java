package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.util.Date;
import java.util.List;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * Record used by SendConnection.
 * @param from Sender's mail address.
 * @param to Recipient's mail address.
 * @param subject Topic.
 * @param text Content text.
 * @param contentType Content type of text part.
 * @param attachments Attached files.
 * @param sent Sent date.
 */
public record Mail(
        /** Sender's mail address. */
        String from,
        /** Recipient's mail address. */
        String to,
        /** Topic. */
        String subject,
        /** Content text. */
        String text,
        /** Content type of text part. */
        String contentType,
        /** Attached files. */
        List<File> attachments,
        /** Sent date and time. */
        Date sent)
{
    /** Constructor used by ReceivePolling. */
    public Mail(String from, String subject, Date sent) {
        this(from, null, subject, null, null, null, sent);
    }
    
    public Mail(String from, String subject, String text) {
        this(from, null, subject, text, null, null, null);
    }
    
    @Override
    public final String toString() {
        final StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("From:    "+from+"\n");
        textBuilder.append("To:      "+to+"\n");
        textBuilder.append("Sent:    "+DateUtil.toString(sent, true)+"\n");
        textBuilder.append("Subject: "+subject+"\n");
        return textBuilder.toString();
    }
}