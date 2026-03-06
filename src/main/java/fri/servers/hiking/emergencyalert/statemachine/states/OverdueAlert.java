package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class OverdueAlert extends AbstractState
{
    /** Clicked "Home Again" after first OVERDUE_ALERT. */
    @Override
    public AbstractState comingHome(Context context) {
        context.comingHomeTooLate();
        return new HomeAgain();
    }
    
    /** A subsequent (not the first) OVERDUE_ALERT arrived. */
    @Override
    public AbstractState overdueAlert(Context context) {
        context.sendAlertMessage();
        return this;
    }
   
    /** The first ALERT_CONFIRMED arrived. */
    @Override
    public AbstractState alertConfirmed(Context context) {
        if (context.hikerConfirmedAndHavingMoreDays())
            return new OnTheWay();
        
        return new AlertConfirmed(); // no more polling for confirmations
    }
}