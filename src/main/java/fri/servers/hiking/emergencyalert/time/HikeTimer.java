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
    private IntervalModel intervalModel;
    
    private Date nextOverdueAlertTime;
    
    /**
     * Prepares this timer with hike-data and starts it.
     * This can be called just once!
     * @param plannedBegin the planned start date/time from Hike.
     * @param plannedHome the planned end date/time from Hike
     * @param alertIntervalMinutes the amount of minutes that
     *      should be waited until the next contact gets messaged.
     * @param alertIntervalShrinking between 0.3 and 1.0,
     *      after every alert this will be applied on alertIntervalMinutes.
     */
    public void start(
            Date plannedBegin, 
            final Date plannedHome, 
            IntervalModel intervalModel,
            EventDispatcher eventDispatcher)
    {
        assertStart(plannedHome);
        
        final Date begin = (plannedBegin != null) ? plannedBegin : DateUtil.now();
        
        this.nextOverdueAlertTime = plannedHome;
        this.intervalModel = intervalModel;
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
            scheduler.schedule(settingOff, begin);
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
            @Override
            public String toString() {
                return "OverdueTask";
            }
        };
    }
}