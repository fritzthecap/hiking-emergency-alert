package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class HikeActivated extends AbstractState
{
    @Override
    public AbstractState activation(Context context) {
        context.updateHike();
        context.startHikeTimer();
        return this;
    }
    
    @Override
    public AbstractState settingOff(Context context) {
        return new OnTheWay();
    }
    
    /** User canceled the hike before OnTheWay. */
    @Override
    public AbstractState comingHome(Context context) {
        return new HomeAgain();
    }
}