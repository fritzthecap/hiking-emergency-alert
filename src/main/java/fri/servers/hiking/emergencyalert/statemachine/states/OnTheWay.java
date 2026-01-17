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
        final AbstractState followerState = new OverdueAlert();
        followerState.overdueAlert(context); // immediately send first alert
        return followerState;
    } 
}