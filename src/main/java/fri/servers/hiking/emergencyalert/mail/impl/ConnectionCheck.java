package fri.servers.hiking.emergencyalert.mail.impl;

import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.MailSendException;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection.SendResult;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;
import jakarta.mail.Authenticator;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;

public class ConnectionCheck extends InboxVisitorConnection
{
    private Authenticator authenticator;

    /** With this constructor you would have to input the password each time again. */ 
    public ConnectionCheck(MailConfiguration mailConfiguration) {
        this(mailConfiguration, null);
    }
    
    /** Constructor that can avoid password input when authenticator is not null and valid. */ 
    public ConnectionCheck(MailConfiguration mailConfiguration, Authenticator authenticator) {
        super(
            mailConfiguration,
            authenticator,
            new Hike().uniqueMailId, /// the mail text that should be found
            DateUtil.addSeconds(DateUtil.now(), -1), // send will happen very fast without password input
            null);
    }
    
    /** 
     * Tries to send a test-mail to own mail address, then connects
     * to own store and checks whether the mail has arrived,
     * deletes it when found.
     */
    public boolean trySendAndReceive() throws MailException {
        this.authenticator = checkInbox();
        
        sendTestMail(authenticator);
        
        return receiveAndDeleteTestMail();
    }

    /** @return a valid authenticator. */
    public final Authenticator getValidAuthenticator() {
        return authenticator;
    }
    
    
    /** Delete the test-mail when found. */
    @Override
    protected void processFoundMessage(Message message) {
        try {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        catch (MessagingException e) {
            System.err.println(e.toString());
        }
    }
    
    /** Factory method for SendConnection, to be overridden by unit-tests. */
    protected SendConnection newSendConnection(MailConfiguration mailConfiguration, Authenticator authenticator) {
        return new SendConnection(mailConfiguration, authenticator);
    }

    
    private Authenticator checkInbox() throws MailReceiveException {
        System.out.println("Connecting to mail store ...");
        
        final InboxVisitor visitor = new InboxVisitor() {
            @Override
            public boolean visitInbox(Folder inbox) throws Exception {
                System.out.println("... connecting to mail store succeeded!");
                return false; // do not visit mails
            }
            @Override
            public boolean visitMail(Message mail) throws Exception {
                return false;
            }
        };
        
        final SessionWithAuthenticator sessionAndAuth = receive(visitor);
        
        // when no exception was thrown by receive(), we have a valid authenticator
        return sessionAndAuth.authenticator();    
    }

    private SendResult sendTestMail(Authenticator authenticator) throws MailSendException {
        System.out.println("Now sending a test-mail ...");
        
        final String from = mailConfiguration.getMailFromAdress();
        if (StringUtil.isEmpty(from))
            throw new IllegalArgumentException("Mail configuration is incomplete, having not from-address!");
        
        final Mail checkMail = new Mail(from, from, "Mail connection test", this.uniqueMailId, null, null, null);
        final SendConnection sendConnection = newSendConnection(mailConfiguration, authenticator);
        
        final SendResult sendResult = sendConnection.send(checkMail);
        
        System.out.println("... sending succeeded!");
        return sendResult;
    }
    
    private boolean receiveAndDeleteTestMail() throws MailReceiveException {
        System.out.println("Now trying to receive and delete sent message ... "+DateUtil.nowString(true));
        
        // poll until mail arrives at server
        boolean success = false;
        final int sleepSeconds = 2;
        final int maximumSeconds = Math.max(sleepSeconds, mailConfiguration.getMaximumConnectionTestSeconds());
        
        for (int done = 0; success == false && done <= maximumSeconds; done += sleepSeconds) {
            System.out.println("  ... receive attempt at "+DateUtil.nowString(true));
            
            success = (searchAlertConfirmation() != null); // searches for mail text containing uniqueMailId
            
            if (success == false)
                try { Thread.sleep(sleepSeconds * 1000); } catch (InterruptedException e) {}
        }
        System.out.println("... receive success is "+success+", at "+DateUtil.nowString(true));
        return success;
    }
}