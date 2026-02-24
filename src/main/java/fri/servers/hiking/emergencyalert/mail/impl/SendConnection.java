package fri.servers.hiking.emergencyalert.mail.impl;

import java.io.File;
import java.util.Date;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import jakarta.activation.CommandInfo;
import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class SendConnection extends MailSessionFactory
{
    public record SendResult(String messageId, Date sentDate)
    {
    }
    
    protected final MailConfiguration mailConfiguration;
    private Authenticator authenticator;
    
    public SendConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        this.mailConfiguration = mailConfiguration;
        this.authenticator = authenticator;
    }
    
    /**
     * Sends given mail, or throws MailSendException when not possible.
     * @param mail the mail to send, with from and to addresses and others.
     * @return the send-result, including a valid authenticator.
     * @throws MailSendException
     */
    public SendResult send(Mail mail) throws MailSendException {
        final SessionWithAuthenticator sessionAndAuth = newSession(mailConfiguration, authenticator, true);
        // only AFTER mail action it will be known whether authenticator was valid
        
        // trying to fix issue #24
        workaroundIssue24();
        
        Exception exception = null;
        SendResult sendResult = null;
        try  {
            final Message sendMessage = new MimeMessage(sessionAndAuth.session());
            sendMessage.setFrom(new InternetAddress(mail.from()));
            sendMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.to()));
            sendMessage.setSubject(mail.subject());
            
            final Multipart multipart = new MimeMultipart();
            
            final BodyPart textPart = new MimeBodyPart(); // add text
            textPart.setContent(mail.text(), mail.contentType() != null ? mail.contentType() : "text/plain");
            multipart.addBodyPart(textPart);
            
            if (mail.attachments() != null) {
                for (File attachment : mail.attachments()) {
                    final MimeBodyPart attachmentPart = new MimeBodyPart(); // add attachment
                    attachmentPart.attachFile(attachment);
                    multipart.addBodyPart(attachmentPart);
                }
            }
            
            sendMessage.setContent(multipart);
            
            Transport.send(sendMessage);
            
            sendResult = new SendResult(MessageUtil.messageId(sendMessage), sendMessage.getSentDate());
            // sent-date precision is seconds: "Mon, 19 Jan 2026 14:31:42 +0100 (CET)"
        }
        catch (Exception e) {
            exception = e;
        }
        
        if (exception != null)
            throw new MailSendException(exception);
        else
            this.authenticator = sessionAndAuth.authenticator();
        
        return sendResult;
    }

    
    private static boolean workedAround = false;
    
    /**
     * Error message <i>"no object DCH for MIME type multipart/mixed"</i>.<br/>
     * Bug #24: on Maven JAR assembly, the file jakarta-mail/META-INF/jakarta.mailcap
     * is overwritten by dsn/META-INF/jakarta.mailcap; it could also happen that
     * the second is overwritten by the first. Consequence is that one of the files
     * did not contribute its MIME-bindings.<br/>
     * Fixed this here by optionally adding entries copied from 
     * jakarta.mail-2.0.5.jar/META-INF/jakarta.mailcap and dsn-2.0.5.jar/META-INF/jakarta.mailcap
     * to the static default command-map.
     */
    private void workaroundIssue24() {
        if (workedAround)
            return;
        
        MailcapCommandMap mailcapCommandMap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        CommandInfo[] jakartaMailMultipartCommand = mailcapCommandMap.getAllCommands("multipart/*");
        if (jakartaMailMultipartCommand == null || jakartaMailMultipartCommand.length <= 0) {
            System.err.println("Fixing due to missing 'multipart/*' mailcap command ...");
            // following entries were copied from
            // .m2/repository/org/eclipse/angus/jakarta.mail/2.0.5/jakarta.mail-2.0.5.jar/META-INF/jakarta.mailcap
            mailcapCommandMap.addMailcap("text/plain;;        x-java-content-handler=org.eclipse.angus.mail.handlers.text_plain");
            mailcapCommandMap.addMailcap("text/html;;     x-java-content-handler=org.eclipse.angus.mail.handlers.text_html");
            mailcapCommandMap.addMailcap("text/xml;;      x-java-content-handler=org.eclipse.angus.mail.handlers.text_xml");
            mailcapCommandMap.addMailcap("multipart/*;;       x-java-content-handler=org.eclipse.angus.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
            mailcapCommandMap.addMailcap("message/rfc822;;    x-java-content-handler=org.eclipse.angus.mail.handlers.message_rfc822");
        }
        
        CommandInfo[] dsnDeliveryStatusCommand = mailcapCommandMap.getAllCommands("message/delivery-status");
        if (dsnDeliveryStatusCommand == null || dsnDeliveryStatusCommand.length <= 0) {
            System.err.println("Fixing due to missing 'message/delivery-status' mailcap command ...");
            // following entries were copied from
            // .m2/repository/org/eclipse/angus/dsn/2.0.5/dsn-2.0.5.jar/META-INF/jakarta.mailcap
            mailcapCommandMap.addMailcap("multipart/report;;  x-java-content-handler=org.eclipse.angus.mail.dsn.multipart_report");
            mailcapCommandMap.addMailcap("message/delivery-status;; x-java-content-handler=org.eclipse.angus.mail.dsn.message_deliverystatus");
            mailcapCommandMap.addMailcap("message/disposition-notification;; x-java-content-handler=org.eclipse.angus.mail.dsn.message_dispositionnotification");
            mailcapCommandMap.addMailcap("text/rfc822-headers;;   x-java-content-handler=org.eclipse.angus.mail.dsn.text_rfc822headers");
        }
        
        workedAround = true;
    }
}