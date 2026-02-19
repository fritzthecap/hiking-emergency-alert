package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class OnTheWay extends AbstractState
{
    @Override
    public AbstractState comingHome(Context context) {
        context.comingHomeInTime();
        return new HomeAgain();
    }
    
   @Override
    public AbstractState overdueAlert(Context context) {
        if (context.alertsStoppedByHiker()) // hiker is alive, do not change to overdue state
            return this; // TODO: this is not yet in state/transition diagram!
        
        final AbstractState followerState = new OverdueAlert();
        followerState.overdueAlert(context); // immediately send first alert
        
        return followerState;
    } 
}