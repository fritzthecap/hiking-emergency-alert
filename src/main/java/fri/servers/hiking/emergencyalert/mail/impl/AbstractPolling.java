package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.Set;
import java.util.TimerTask;
import fri.servers.hiking.emergencyalert.mail.MailReceiveException;
import fri.servers.hiking.emergencyalert.mail.impl.SendConnection.SendResult;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.time.Scheduler;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

/**
 * Base polling functionality for mails coming from outside
 * and containing MAIL-ID.
 */
public abstract class AbstractPolling extends Scheduler
{
    private InboxVisitorConnection receiveConnection;
    private int pollingMinutes;
    
    public void start(
            String uniqueMailId, 
            MailConfiguration mailConfiguration, 
            Authenticator authenticator,
            int pollingMinutes,
            Set<SendConnection.SendResult> sendResultsLive)
    {
        if (receiveConnection != null)
            throw new IllegalStateException("Can not start polling again!");
        
        this.pollingMinutes = pollingMinutes;
        
        final Date pollingStartTime = DateUtil.now();
        
        this.receiveConnection = newInboxVisitorConnection(
                mailConfiguration, 
                authenticator, 
                uniqueMailId,
                pollingStartTime,
                sendResultsLive);
        
        final Date firstPolling = DateUtil.addMinutes(pollingStartTime, pollingMinutes);
        super.start(scheduler -> 
            scheduler.schedule(buildTask(), firstPolling));
        
        System.out.println(
                "Polling for "+pollingType()+" will start in "+pollingMinutes+
                " minute(s), now it is "+DateUtil.now4Log());
    }
    
    /**
     * This is for logging prints.
     * @return e.g. "alert confirmation" or "activation".
     */
    protected abstract String pollingType();
    
    /**
     * A suitable mail containing MAIL-ID has been received, 
     * do something with it. Polling stops now.
     */
    protected abstract void processConfirmation(Mail confirmation);
    
    /**
     * Called when no mail could be received and continuing to poll. To be overridden.
     * @return true when polling should continue, else false, this returns true.
     */
    protected boolean shouldContinuePolling() {
        return true;
    }

    /**
     * Factory method for InboxVisitorConnection, to be overridden by unit-tests.
     * Mind that this is called from constructor, so do not use instance fields!
     * @return a new InboxVisitorConnection.
     */
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
                sendResultsLive);
    }


    private TimerTask buildTask() {
        return new TimerTask() {
            @Override
            public void run() {
                receiveAlertConfirmation();
            }
        };
    }
    
    private void receiveAlertConfirmation() {
        try {
            final Mail mail = receiveConnection.searchNonSelfSentMailHavingMailId();
            
            if (mail != null) { // found an alert confirmation in INBOX
                System.out.println("Received "+pollingType()+" from "+mail.from()+" at "+DateUtil.now4Log());
                
                processConfirmation(mail);
            }
            else {
                continuePolling(null);
            }
        }
        catch (MailReceiveException e) {
            continuePolling(e);
        }
    }

    private void continuePolling(Exception e) {
        if (e != null)
            System.out.println("ERROR: "+e.toString());
        else
            System.out.println("Found no "+pollingType()+" at "+DateUtil.now4Log());

        if (shouldContinuePolling()) {
            System.out.println("Trying again in "+pollingMinutes+" minutes.");
            
            synchronizedOnScheduler(scheduler ->
                scheduler.schedule(buildTask(), DateUtil.addMinutes(DateUtil.now(), pollingMinutes))
            );
        }
    }
}