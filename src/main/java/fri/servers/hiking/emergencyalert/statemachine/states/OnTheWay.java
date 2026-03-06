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
        
        final boolean stoppedAndNoMoreHikeDays = (alertsStoppedByHiker == null);
        if (stoppedAndNoMoreHikeDays) // hiker is alive, hike finished
            return new HomeAgain(); // timer was stopped
        
        final boolean stoppedButMoreHikeDays = Boolean.TRUE.equals(alertsStoppedByHiker);
        if (stoppedButMoreHikeDays) // hiker is alive, hike continues
            return this; // timer was restarted
        
        context.sendAlertMessage();
        return new OverdueAlert();
    } 
}