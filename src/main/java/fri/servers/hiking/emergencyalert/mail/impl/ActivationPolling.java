package fri.servers.hiking.emergencyalert.mail.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

/**
 * When the hiker has decided to activate the hike remotely,
 * this polling stops at least 1 minute before the hike's home-time.
 * This happens just on the first day of a hike, not on any subsequent.
 */
public final class ActivationPolling extends AbstractPolling
{
    private Consumer<Mail> toBeCalledWhenReceived;
    private Date stopTime;
    
    public void start(
            String uniqueMailId, 
            MailConfiguration mailConfiguration, 
            Authenticator authenticator,
            int pollingMinutes,
            Date homeTime,
            Set<SendConnection.SendResult> sendResultsLive,
            Consumer<Mail> toBeCalledWhenReceived)
    {
        super.start(uniqueMailId, mailConfiguration, authenticator, pollingMinutes, sendResultsLive);
        
        this.stopTime = DateUtil.addMinutes(homeTime, -(pollingMinutes + 1)); // stop before home-time
        this.toBeCalledWhenReceived = Objects.requireNonNull(toBeCalledWhenReceived);
    }
    
    @Override
    protected final String pollingType() {
        return "activation mail";
    }
    
    @Override
    protected final void processConfirmation(Mail confirmation) {
        toBeCalledWhenReceived.accept(confirmation);
    }
    
    @Override
    protected final boolean shouldContinuePolling() {
        return DateUtil.now().before(stopTime);
    }
}