package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;

/**
 * Turns a MailConfiguration into Properties usable for the Java mail API.
 * 
 * @see https://jakarta.ee/specifications/mail/2.1/apidocs/jakarta.mail/jakarta/mail/package-summary
 * 
 * @see https://jakarta.ee/specifications/mail/1.6/apidocs/index.html
 * @see https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary
 * @see https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/pop3/package-summary
 * @see https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/imap/package-summary
 * 
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
 * @see https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
 * 
 * @see https://mailtrap.io/blog/starttls-ssl-tls/
 */
public class MailProperties extends Properties
{
    // instance implementation
    
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
        put(mailProtocol+"writetimeout", ""+timeoutMillis);
    }
    
    private void putCustomProperties(List<List<String>> customProperties) {
        if (customProperties != null)
            for (List<String> tuple : customProperties)
                if (tuple.size() == 2 && tuple.get(0) != null && tuple.get(1) != null)
                    put(tuple.get(0), tuple.get(1));
    }
    

    // static implementation, sort orders
    
    private static final List<List<String>> sortedCustomProperties = List.of(
            List.of("mail.debug", "true"),
            
            List.of("mail.smtp.user", "your name"), // would override "mail.user"
            List.of("mail.smtp.auth", "true"), // default is false
            List.of("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"),
            List.of("mail.smtp.socketFactory.fallback", "false"), // default is true
            List.of("mail.smtp.ssl.enable", "true"), // default is false for "smtp", true for "smtps"
            List.of("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"),
            List.of("mail.smtp.ssl.trust", "*"),
            List.of("mail.smtp.ssl.checkserveridentity", "true"), // default is false
            List.of("mail.smtp.starttls.enable", "true"), // default is false
            
            List.of("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory"),
            List.of("mail.imaps.socketFactory.fallback", "false"), // default is true
            List.of("mail.imaps.ssl.enable", "true"), // default is false for "imap", true for "imaps"
            List.of("mail.imaps.ssl.trust", "*"),
            List.of("mail.imaps.ssl.checkserveridentity", "true"), // default is false
            List.of("mail.imaps.starttls.enable", "true"), // default is false
            
            List.of("mail.pop3s.socketFactory.class", "javax.net.ssl.SSLSocketFactory"),
            List.of("mail.pop3s.socketFactory.fallback", "false"), // default is true
            List.of("mail.pop3s.ssl.enable", "true"),
            List.of("mail.pop3s.ssl.trust", "*"),
            List.of("mail.pop3s.ssl.checkserveridentity", "true"), // default is false
            List.of("mail.pop3s.starttls.enable", "true") // default is false
        );
    
    private static final List<String> sortedCustomPropertyNames;
    
    static {
        sortedCustomPropertyNames = new ArrayList<>(sortedCustomProperties.size());
        
        for (List<String> tuple : sortedCustomProperties)
            sortedCustomPropertyNames.add(tuple.get(0));
    }
    
    /** Additional properties needed for secure mail, as a new clone of the static properties. */
    public static Properties customProperties() {
        final Properties customProperties = new Properties();
        for (List<String> tuple : sortedCustomProperties)
            customProperties.put(tuple.get(0), tuple.get(1));
        return customProperties;
    }

    public static Comparator<String> customPropertiesSorter() {
        return new Comparator<>() {
            @Override
            public int compare(String name1, String name2) {
                return sortedCustomPropertyNames.indexOf(name1) - sortedCustomPropertyNames.indexOf(name2);
            }
        };
    }
    
    private static final List<String> sortedCorePropertyNames = List.of(
            "mail.user", //login user with password
            "mail.transport.protocol", // smtp, smtps
            "mail.store.protocol", // imap, pop3, imaps, pop3s
            "mail.smtp.host", 
            "mail.smtp.port", // 465, or 587, or 2525
            "mail.smtp.from", 
            "mail.smtps.host", 
            "mail.smtps.port", 
            "mail.smtps.from", 
            "mail.imap.host", 
            "mail.imap.port", 
            "mail.imaps.host", 
            "mail.imaps.port", 
            "mail.pop3.host", 
            "mail.pop3.port", 
            "mail.pop3s.host", 
            "mail.pop3s.port", 
            "mail.smtp.timeout",
            "mail.smtp.connectiontimeout", 
            "mail.smtp.writetimeout", 
            "mail.smtps.timeout",
            "mail.smtps.connectiontimeout", 
            "mail.smtps.writetimeout", 
            "mail.imap.timeout", 
            "mail.imap.connectiontimeout", 
            "mail.imap.writetimeout",
            "mail.imaps.timeout", 
            "mail.imaps.connectiontimeout", 
            "mail.imaps.writetimeout",
            "mail.pop3.timeout", 
            "mail.pop3.connectiontimeout", 
            "mail.pop3.writetimeout",
            "mail.pop3s.timeout", 
            "mail.pop3s.connectiontimeout", 
            "mail.pop3s.writetimeout"
        );
    
    public static Comparator<String> corePropertiesSorter() {
        return new Comparator<>() {
            @Override
            public int compare(String name1, String name2) {
                return sortedCorePropertyNames.indexOf(name1) - sortedCorePropertyNames.indexOf(name2);
            }
        };
    }
}