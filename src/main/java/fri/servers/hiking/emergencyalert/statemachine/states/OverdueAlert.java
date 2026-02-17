package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class OverdueAlert extends AbstractState
{
    /** 'Home Again' button was pushed on user-interface. */
    @Override
    public AbstractState comingHome(Context context) {
        context.comingHomeTooLate();
        return new HomeAgain();
    }
    
    /** The first or a next overdue timer event arrived. */
    @Override
    public AbstractState overdueAlert(Context context) {
        context.sendAlertMessage();
        return this;
    }
   
    /** The first alert confirmation arrived. */
    @Override
    public AbstractState alertConfirmed(Context context) {
        if (context.alertConfirmedByContact())
            return new AlertConfirmed();
        
        return new OnTheWay(); // TODO: this is not in state/transition diagram!
    }
}