package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.List;
import java.util.Properties;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;

/**
 * Turns a MailConfiguration into Properties usable for the Java mail API.
 * 
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
 */
public class MailProperties extends Properties
{
    /** Additional properties needed for secure mail. */
    public static Properties customProperties() {
        final Properties CUSTOM_PROPERTIES = new Properties();
        
        CUSTOM_PROPERTIES.put("mail.debug", "true");
        
        CUSTOM_PROPERTIES.put("mail.smtp.username", "user.name");
        CUSTOM_PROPERTIES.put("mail.smtp.auth", "true");
        
        CUSTOM_PROPERTIES.put("mail.smtp.port", "465 for SSL, 587 for STARTTLS");
        CUSTOM_PROPERTIES.put("mail.smtp.starttls.enable", "true");
        
        CUSTOM_PROPERTIES.put("mail.smtp.ssl.enable", "true");
        CUSTOM_PROPERTIES.put("mail.smtp.ssl.protocols", "TLSv1.3");
        CUSTOM_PROPERTIES.put("mail.smtp.ssl.trust", "*");
        CUSTOM_PROPERTIES.put("mail.smtp.socketFactory.port", "465");
        CUSTOM_PROPERTIES.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        CUSTOM_PROPERTIES.put("mail.smtp.socketFactory.fallback", "false");
        
        CUSTOM_PROPERTIES.put("mail.imaps.host", "secure.imap.host");
        CUSTOM_PROPERTIES.put("mail.imaps.port", "993");
        CUSTOM_PROPERTIES.put("mail.imaps.ssl.enable", "true");
        CUSTOM_PROPERTIES.put("mail.imaps.ssl.trust", "*");
        
        CUSTOM_PROPERTIES.put("mail.pop3s.host", "secure.pop.host");
        CUSTOM_PROPERTIES.put("mail.pop3s.port", "995");
        CUSTOM_PROPERTIES.put("mail.pop3s.ssl.enable", "true");
        CUSTOM_PROPERTIES.put("mail.pop3s.ssl.trust", "*");

        return CUSTOM_PROPERTIES;
    }
    
    
    /** Constructor for configuration editing. Custom properties DO NOT get merged in! */
    public MailProperties(MailConfiguration mailConfiguration) {
        putSendProperties(mailConfiguration);
        putReceiveProperties(mailConfiguration);
        putCommonProperties(mailConfiguration);
        // no custom properties here!
    }

    /** Constructor for sending and receiving. Custom properties get merged in. */
    public MailProperties(MailConfiguration mailConfiguration, boolean send) {
        if (send)
            putSendProperties(mailConfiguration);
        else
            putReceiveProperties(mailConfiguration);
        
        putCommonProperties(mailConfiguration);
        putCustomProperties(mailConfiguration.getCustomProperties());
        // custom  may overwrite core properties
    }

    private void putCommonProperties(MailConfiguration mailConfiguration) {
        put("mail.user", mailConfiguration.getMailUser());
        put("mail.transport.protocol", mailConfiguration.getSendMailProtocol());
        put("mail.store.protocol", mailConfiguration.getReceiveMailProtocol());
        
    }
    
    private void putSendProperties(MailConfiguration mailConfiguration) {
        final String sendProtocol = mailConfiguration.getSendMailProtocol();
        putProtocolProperties(
                sendProtocol,
                mailConfiguration.getSendMailHost(),
                mailConfiguration.getSendMailPort(),
                mailConfiguration.getMaximumConnectionTestSeconds());
        
        final String mailFromAccount = mailConfiguration.getSendMailFromAccount();
        final String mailUser = mailConfiguration.getMailUser();
        final String sendFrom = MailUtil.isMailAddress(mailFromAccount) ? mailFromAccount
                : MailUtil.isMailAddress(mailUser) ? mailUser : null;
        if (sendFrom != null)
            put("mail."+sendProtocol+".from", sendFrom);
    }
    
    private void putReceiveProperties(MailConfiguration mailConfiguration) {
        putProtocolProperties(
                mailConfiguration.getReceiveMailProtocol(), 
                mailConfiguration.getReceiveMailHost(), 
                mailConfiguration.getReceiveMailPort(),
                mailConfiguration.getMaximumConnectionTestSeconds());
    }
    
    private void putProtocolProperties(String protocol, String host, int port, int timeoutSeconds) {
        final String mailProtocol = "mail."+protocol+".";
        put(mailProtocol+"host", host);
        put(mailProtocol+"port", ""+port);
        
        final long timeoutMillis = timeoutSeconds * 1000;
        put(mailProtocol+"timeout", ""+timeoutMillis);
        put(mailProtocol+"connectiontimeout", ""+timeoutMillis);
    }
    
    private void putCustomProperties(List<List<String>> customProperties) {
        if (customProperties != null)
            for (List<String> tuple : customProperties)
                if (tuple.size() == 2 && tuple.get(0) != null && tuple.get(1) != null)
                    put(tuple.get(0), tuple.get(1));
    }
}