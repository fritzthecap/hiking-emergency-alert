package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Properties;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import jakarta.mail.Authenticator;
import jakarta.mail.Session;

public class MailSessionFactory
{
    /** Return object for newSession() call.*/
    public record SessionWithAuthenticator(Session session, Authenticator authenticator)
    {
    }
    
    /** 
     * Builds mail-Properties for send OR receive and allocates a mail-Session.
     * @param mailConfiguration the mail properties.
     * @param authenticatorOrNull optional, an already valid authenticator.
     * @param send Session will be used to send (SMTP) when true, or receive (POP, IMAP) when false.
     * @return a mail Session object, never null.
     */
    protected SessionWithAuthenticator newSession(
            MailConfiguration mailConfiguration, 
            Authenticator authenticatorOrNull, 
            boolean send)
    {
        final Properties mailProperties = new MailProperties(mailConfiguration, send);
        
        // some mail servers deny SMTP access without login, so do authentication in any case
        final Authenticator authenticator = (authenticatorOrNull == null)
                ? UserInterface.getInteractiveAthenticator()
                : authenticatorOrNull;
                
        return new SessionWithAuthenticator(
                Session.getInstance(mailProperties, authenticator),
                authenticator);
    }
}