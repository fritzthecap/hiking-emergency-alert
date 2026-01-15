package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Properties;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Turns a MailConfiguration into Properties usable for the Java mail API.
 */
public class MailProperties extends Properties
{
    public MailProperties(MailConfiguration mailConfiguration, boolean send) {
        if (send)
            buildSendProperties(
                mailConfiguration.getMailUser(),
                mailConfiguration.getSendMailProtocol(), 
                mailConfiguration.getSendMailHost(), 
                ""+mailConfiguration.getSendMailPort(), 
                mailConfiguration.getSendMailFromAccount());
        else
            buildReceiveProperties(
                mailConfiguration.getReceiveMailProtocol(), 
                mailConfiguration.getReceiveMailHost(), 
                ""+mailConfiguration.getReceiveMailPort());
        
        put("mail.user", mailConfiguration.getMailUser());
        put("mail.host", send ? mailConfiguration.getSendMailHost() : mailConfiguration.getReceiveMailHost());
        put("mail.store.protocol", send ? mailConfiguration.getSendMailProtocol() : mailConfiguration.getReceiveMailProtocol());
    }
    
    private void buildSendProperties(String mailUser, String protocol, String host, String port, String from) {
        buildReceiveProperties(protocol, host, port);
        put("mail."+protocol+".from", StringUtil.isEmpty(from) ? mailUser : from);
    }
    
    private void buildReceiveProperties(String protocol, String host, String port) {
        put("mail."+protocol+".host", host);
        put("mail."+protocol+".port", port);
    }
}