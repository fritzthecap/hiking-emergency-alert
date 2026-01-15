package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class OverdueAlert extends OnTheWay // to inherit comingHome()
{
    /** The first or a next overdue timer event arrived. */
    @Override
    public AbstractState overdueAlert(Context context) {
        context.sendAlertMessage();
        return this;
    }
   
    /** The first alert confirmation arrived. */
    @Override
    public AbstractState alertConfirmed(Context context) {
        context.stop();
        context.alertConfirmed();
        return new AlertConfirmed();
    }
}