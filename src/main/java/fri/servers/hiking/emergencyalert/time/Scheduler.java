package fri.servers.hiking.emergencyalert.time;

import java.util.Timer;
import java.util.function.Consumer;

/**
 * Synchronizes all accesses to scheduler, so that both 
 * application and TimerTask threads can access it concurrently.
 */
public class Scheduler
{
    private Timer scheduler;
    private final Object schedulerLock = new Object();
    
    /** Timer startup. */
    protected final void start(Consumer<Timer> firstJob) {
        synchronized(schedulerLock) { // do NOT use synchronizedOnScheduler() here, scheduler is null!
            scheduler = newTimer();
            firstJob.accept(scheduler);
        }
    }

    /** Factory method for the <code>java.util.Timer<code>, to be overridden by unit-tests. */
    protected Timer newTimer() {
        return new Timer();
    }

    /** Cancels all scheduled tasks sets timer null. */
    public final void stop() {
        synchronized(schedulerLock) {
            if (scheduler != null) {
                scheduler.cancel();
                scheduler.purge();
                scheduler = null;
            }
        }
    }
    
    /** @return true if this timer is still running, stop() was never called. */
    public final boolean isRunning() {
        synchronized(schedulerLock) {
            return scheduler != null;
        }
    }
    
    /** Any access of the scheduler (Timer) should go through this method, it synchronizes and avoids null. */
    public final void synchronizedOnScheduler(Consumer<Timer> scheduleClient) {
        synchronized(schedulerLock) {
            if (scheduler != null)
                scheduleClient.accept(scheduler);
        }
    }
}