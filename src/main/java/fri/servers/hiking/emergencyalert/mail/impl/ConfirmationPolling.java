package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import jakarta.mail.Authenticator;

/**
 * After the hike's home-time arrived, this polls
 * for an alert-confirmation mail coming from outside.
 * Fires an ALERT_CONFIRMED event when received a confirmation
 * and stops polling then.
 */
public final class ConfirmationPolling extends AbstractPolling
{
    private Mailer.EventDispatcher eventDispatcher;
    private Supplier<Boolean> pollingStopper;
    
    public void start(
            String uniqueMailId, 
            MailConfiguration mailConfiguration, 
            Authenticator authenticator,
            int pollingMinutes,
            Set<SendConnection.SendResult> sendResultsLive,
            Mailer.EventDispatcher eventDispatcher)
    {
        super.start(uniqueMailId, mailConfiguration, authenticator, pollingMinutes, sendResultsLive);
        
        this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
    }
    
    public synchronized void afterNextUnsuccessfulConfirmationPoll(Supplier<Boolean> pollingStopper) {
        this.pollingStopper = pollingStopper;
    }
    
    @Override
    protected final String pollingType() {
        return "alert confirmation";
    }
    
    @Override
    protected final void processConfirmation(Mail confirmation) {
        eventDispatcher.dispatchEvent(Event.ALERT_CONFIRMED, confirmation);
    }
    
    @Override
    protected final synchronized boolean shouldContinuePolling() {
        return (pollingStopper != null) 
                ? pollingStopper.get() 
                : super.shouldContinuePolling();
    }
}