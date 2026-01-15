package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;
import fri.servers.hiking.emergencyalert.util.DateUtil;

public class OnTheWay extends AbstractState
{
    @Override
    public AbstractState comingHome(Context context) {
        context.stop();
        System.out.println("You are back, congratulations to this successful hike! It is "+DateUtil.nowString());
        return new HomeAgain();
    }
    
   @Override
    public AbstractState overdueAlert(Context context) {
        final AbstractState followerState = new OverdueAlert();
        followerState.overdueAlert(context); // immediately send first alert
        return followerState;
    } 
}