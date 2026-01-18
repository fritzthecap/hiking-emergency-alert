package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Properties;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Turns a MailConfiguration into Properties usable for the Java mail API.
 * 
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
 */
public class MailProperties extends Properties
{
    public MailProperties(MailConfiguration mailConfiguration) {
        putSendProperties(mailConfiguration);
        putReceiveProperties(mailConfiguration);
        putCommonProperties(mailConfiguration);
    }
    
    public MailProperties(MailConfiguration mailConfiguration, boolean send) {
        if (send)
            putSendProperties(mailConfiguration);
        else
            putReceiveProperties(mailConfiguration);
        
        putCommonProperties(mailConfiguration, send);
    }

    private void putCommonProperties(MailConfiguration mailConfiguration) {
        put("mail.user", mailConfiguration.getMailUser());
        
        final String sendMailProtocol = mailConfiguration.getSendMailProtocol();
        put("mail."+sendMailProtocol+".host", mailConfiguration.getSendMailHost());
        put("mail.transport.protocol", mailConfiguration.getSendMailProtocol());
        
        final String receiveMailProtocol = mailConfiguration.getReceiveMailProtocol();
        put("mail."+receiveMailProtocol+".host", mailConfiguration.getReceiveMailHost());
        put("mail.store.protocol", mailConfiguration.getReceiveMailProtocol());
    }
    
    private void putCommonProperties(MailConfiguration mailConfiguration, boolean send) {
        put("mail.user", mailConfiguration.getMailUser());
        put("mail.host", send ? mailConfiguration.getSendMailHost() : mailConfiguration.getReceiveMailHost());
        put("mail.store.protocol", send ? mailConfiguration.getSendMailProtocol() : mailConfiguration.getReceiveMailProtocol());
    }
    
    private void putSendProperties(MailConfiguration mailConfiguration) {
        putSendProperties(
                mailConfiguration.getMailUser(),
                mailConfiguration.getSendMailProtocol(), 
                mailConfiguration.getSendMailHost(), 
                ""+mailConfiguration.getSendMailPort(), 
                mailConfiguration.getSendMailFromAccount());
    }
    
    private void putSendProperties(String mailUser, String protocol, String host, String port, String from) {
        putReceiveProperties(protocol, host, port);
        put("mail."+protocol+".from", StringUtil.isEmpty(from) ? mailUser : from);
    }
    
    private void putReceiveProperties(MailConfiguration mailConfiguration) {
        putReceiveProperties(
                mailConfiguration.getReceiveMailProtocol(), 
                mailConfiguration.getReceiveMailHost(), 
                ""+mailConfiguration.getReceiveMailPort());
    }
    
    private void putReceiveProperties(String protocol, String host, String port) {
        put("mail."+protocol+".host", host);
        put("mail."+protocol+".port", port);
    }
}