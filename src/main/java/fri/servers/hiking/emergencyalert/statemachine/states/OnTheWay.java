package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class OnTheWay extends AbstractState
{
    /** Clicked "Home Again" before first OVERDUE_ALERT. */
    @Override
    public AbstractState comingHome(Context context) {
        context.comingHomeInTime();
        return new HomeAgain();
    }
    
    /** The first OVERDUE_ALERT event arrives, at hike's home-time. */
    @Override
    public AbstractState overdueAlert(Context context) {
        final Boolean alertsStoppedByHiker = context.alertsStoppedByHiker();
        final boolean notStopped = (alertsStoppedByHiker == null);
        if (notStopped) {
            context.sendAlertMessage();
            return new OverdueAlert();
        }
        
        final boolean noMoreHikeDays = Boolean.TRUE.equals(alertsStoppedByHiker);
        if (noMoreHikeDays) // hiker is alive, hike finished
            return new HomeAgain(); // timer was stopped
        
        return this; // hiker is alive, hike continues, timer was restarted
    } 
}