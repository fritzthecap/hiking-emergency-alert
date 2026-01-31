package fri.servers.hiking.emergencyalert.mail;

import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import com.icegreen.greenmail.util.ServerSetupTest;
import fri.servers.hiking.emergencyalert.mail.impl.ConfirmationPolling;
import fri.servers.hiking.emergencyalert.mail.impl.InboxVisitorConnection;
import fri.servers.hiking.emergencyalert.mail.impl.ConnectionCheck;
import fri.servers.hiking.emergencyalert.mail.impl.MailSessionFactory.SessionWithAuthenticator;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection.SendResult;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

/** Overrides all mail Session allocations. Uses the given timer for polling. */
public class GreenmailTestMailer extends MailerImpl
{
    private final String user;
    private final String password;
    private final Timer pollingTimer;
    
    public GreenmailTestMailer(String user, String password, Timer pollingTimer) {
        this.user = user;
        this.password = password;
        this.pollingTimer = pollingTimer;
    }
    
    @Override
    protected ConnectionCheck newConnectionCheck(MailConfiguration mailConfiguration) {
        return new ConnectionCheck(mailConfiguration) // a receive connection
        {
            @Override
            protected SessionWithAuthenticator newSession(
                    MailConfiguration mailConfiguration, 
                    Authenticator authenticatorOrNull,
                    boolean send) {
                return send ? newSendSession() : newReceiveSession();
            }
            
            @Override
            protected SendConnection newSendConnection(
                    MailConfiguration mailConfiguration, 
                    Authenticator authenticator) {
                return GreenmailTestMailer.this.newSendConnection(mailConfiguration, authenticator);
            }
        };
    }
    
    @Override
    protected ConfirmationPolling newConfirmationPolling() {
        return new ConfirmationPolling() // a receive connection
        {
            @Override
            protected InboxVisitorConnection newInboxVisitorConnection(
                    MailConfiguration mailConfiguration,
                    Authenticator authenticator, 
                    String uniqueMailId,
                    Date pollingStartTime,
                    Set<SendResult> sendResultsLive)
            {
                return new InboxVisitorConnection(
                        mailConfiguration,
                        authenticator, 
                        uniqueMailId,
                        pollingStartTime,
                        sendResultsLive)
                {
                    @Override
                    protected SessionWithAuthenticator newSession(
                            MailConfiguration mailConfiguration, 
                            Authenticator authenticatorOrNull,
                            boolean send) {
                        return newReceiveSession();
                    }
                };
            }
            
            @Override
            protected Timer newTimer() {
                return pollingTimer;
            }
        };
    }

    @Override
    protected SendConnection newSendConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        return new SendConnection(mailConfiguration, authenticator) // a send connection
        {
            @Override
            protected SessionWithAuthenticator newSession(
                    MailConfiguration mailConfiguration, 
                    Authenticator authenticatorOrNull,
                    boolean send) {
                return newSendSession();
            }
        };
    }
    
    private SessionWithAuthenticator newSendSession() {
        return authenticatedSession(ServerSetupTest.SMTP.configureJavaMailSessionProperties(null, false));
    }

    private SessionWithAuthenticator newReceiveSession() {
        return authenticatedSession(ServerSetupTest.POP3.configureJavaMailSessionProperties(null, false));
    }

    private SessionWithAuthenticator authenticatedSession(Properties mailProperties) {
        mailProperties.setProperty("mail.user", user);
        
        final Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        mailProperties.getProperty("mail.user"),
                        password);
            }
        };
        return new SessionWithAuthenticator(
                Session.getInstance(mailProperties, authenticator),
                authenticator);
    }
}