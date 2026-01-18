package fri.servers.hiking.emergencyalert.mail.impl;

import java.io.File;
import java.util.Date;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
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
        
        Exception exception = null;
        SendResult sendResult = null;
        try  {
            final Message sendMessage = new MimeMessage(sessionAndAuth.session());
            sendMessage.setFrom(new InternetAddress(mail.from()));
            sendMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.to()));
            sendMessage.setSubject(mail.subject());
            if (mail.sent() != null)
                sendMessage.setSentDate(mail.sent());
            
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
            
            sendResult = new SendResult(
                    MessageUtil.messageId(sendMessage), 
                    DateUtil.eraseMilliseconds(sendMessage.getSentDate()));
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
}