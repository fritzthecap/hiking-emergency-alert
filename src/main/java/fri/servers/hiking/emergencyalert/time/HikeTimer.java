package fri.servers.hiking.emergencyalert.time;

import java.util.Date;
import java.util.Objects;
import java.util.TimerTask;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/** 
 * Starts a timer thread with tasks for dispatching begin- and home-times
 * (SETTING_OFF, OVERDUE_ALERT) to the given event-dispatcher.
 * When home-time has passed, starts to call the dispatcher with OVERDUE_ALERT
 * all <code>messageIntervalMinutes</code> until <code>stop()</code> gets called.
 * <p/>
 * There is also a dispatch-failure method (mail exception) that repeats a
 * given action until it throws no exception any more.
 */
public class HikeTimer extends Scheduler
{
    /** Clients implement this to receive timer events. */
    public interface EventDispatcher
    {
        void dispatchEvent(Event event);
    }
    
    private EventDispatcher dispatcher;
    
    private int messageIntervalMinutes;
    private Date nextOverdueAlertTime;
    
    /**
     * Prepares this timer with hike-data and starts it.
     * This can be called just once!
     * @param plannedBegin the planned start date/time from Hike.
     * @param plannedHome the planned end date/time from Hike
     * @param messageIntervalMinutes the amount of minutes that
     *      should be waited until the next contact gets messaged.
     */
    public void start(final Date plannedBegin, final Date plannedHome, int messageIntervalMinutes, EventDispatcher eventDispatcher) {
        assertStart(plannedBegin, plannedHome, messageIntervalMinutes); // excludes nulls
        
        this.nextOverdueAlertTime = plannedHome;
        this.messageIntervalMinutes = messageIntervalMinutes;
        this.dispatcher = Objects.requireNonNull(eventDispatcher);
        
        final TimerTask settingOff = new TimerTask() {
            @Override
            public void run() {
                dispatcher.dispatchEvent(Event.SETTING_OFF);
            }
            @Override
            public String toString() {
                return "SettingOffTask";
            }
        };
        
        super.start(scheduler -> {
            scheduler.schedule(settingOff, plannedBegin);
            scheduler.schedule(createOverdueTask(), plannedHome);
        });
    }

    /**
     * Mail sending failed, repeat it in given minute intervals until no exception
     * is thrown any more, or timer has been stopped.
     * @param runnable what to repeat.
     * @param repeatMinutes the repeat interval minutes.
     */
    public void repeatFailedEvent(final Runnable runnable, final int repeatMinutes) {
        synchronizedOnScheduler(scheduler -> {
            scheduler.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                },
                DateUtil.addMinutes(DateUtil.now(), repeatMinutes)
            );
        });
    }
    
    
    private void assertStart(Date plannedBegin, Date plannedHome, long messageIntervalMinutes) {
        if (isRunning())
            throw new IllegalStateException(
                "Timer is running, can not start again, start-events on a running StateMachine would cause errors!");
        
        Objects.requireNonNull(plannedBegin);
        
        if (Objects.requireNonNull(plannedHome).before(DateUtil.now()))
            throw new IllegalArgumentException("Planned home-time is before current time: "+plannedHome);
        
        if (messageIntervalMinutes <= 0)
            throw new IllegalArgumentException("Invalid messageIntervalMinutes: "+messageIntervalMinutes);
    }
    
    private TimerTask createOverdueTask() {
        return new TimerTask() {
            @Override
            public void run() {
                dispatcher.dispatchEvent(Event.OVERDUE_ALERT);
                
                synchronizedOnScheduler(scheduler -> {
                    nextOverdueAlertTime = DateUtil.addMinutes(nextOverdueAlertTime, messageIntervalMinutes);
                    scheduler.schedule(createOverdueTask(), nextOverdueAlertTime);
                });
            }
            @Override
            public String toString() {
                return "OverdueTask";
            }
        };
    }
}