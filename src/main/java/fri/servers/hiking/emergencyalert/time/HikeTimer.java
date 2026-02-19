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
    private AlertIntervalModel intervalModel;
    
    private Date nextOverdueAlertTime;
    
    /**
     * Prepares this timer with hike-data and starts it.
     * This can be called just once!
     * @param plannedBegin optional, the planned start date/time of Hike,
     *      no set-off event would be fired when null.
     * @param plannedHome required, the planned end date/time from Hike.
     * @param intervalModel source for the amount of minutes that should
     *      be waited until the next contact gets alerted about overdue.
     */
    public void start(
            final Date plannedBegin, 
            final Date plannedHome, 
            AlertIntervalModel intervalModel,
            EventDispatcher eventDispatcher)
    {
        assertStart(plannedHome);
        
        this.nextOverdueAlertTime = Objects.requireNonNull(plannedHome);
        this.intervalModel = intervalModel;
        this.dispatcher = Objects.requireNonNull(eventDispatcher);
        
        final TimerTask settingOff;
        if (plannedBegin != null) {
            settingOff = new TimerTask() {
                @Override
                public void run() {
                    dispatcher.dispatchEvent(Event.SETTING_OFF);
                }
                @Override
                public String toString() {
                    return "SettingOffTask";
                }
            };
        }
        else {
            settingOff = null;
        }
        
        super.start(scheduler -> {
            if (settingOff != null)
                scheduler.schedule(settingOff, plannedBegin);
            
            scheduler.schedule(createOverdueTask(), plannedHome);
        });
    }

    /**
     * Call given function (runnable) it in given seconds.
     * This will not be done if timer has been stopped meanwhile.
     * @param runnable what to execute.
     * @param inSeconds the interval seconds to wait, counted from now on.
     */
    public void runInSeconds(final Runnable runnable, final int inSeconds) {
        synchronizedOnScheduler(scheduler -> {
            scheduler.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                },
                DateUtil.addMinutes(DateUtil.now(), inSeconds)
            );
        });
    }
    
    public Date getNextOverdueAlertTime() {
        return nextOverdueAlertTime;
    }
    
    
    private void assertStart(Date plannedHome) {
        if (isRunning())
            throw new IllegalStateException(
                "Timer is running, can not start again, start-events on a running StateMachine would cause errors!");
        
        if (Objects.requireNonNull(plannedHome).before(DateUtil.now()))
            throw new IllegalArgumentException("Planned home-time is before current time: "+plannedHome);
    }
    
    private TimerTask createOverdueTask() {
        return new TimerTask() {
            @Override
            public void run() {
                // send an alert
                dispatcher.dispatchEvent(Event.OVERDUE_ALERT);
                
                // schedule next alert
                synchronizedOnScheduler(scheduler -> {
                    nextOverdueAlertTime = DateUtil.addMinutes(nextOverdueAlertTime, intervalModel.nextIntervalMinutes());
                    scheduler.schedule(createOverdueTask(), nextOverdueAlertTime);
                });
            }
        };
    }
}