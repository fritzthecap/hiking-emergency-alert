package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class HikerRegistered extends AbstractState
{
    @Override
    public AbstractState registration(Context context) {
        context.updateHike();
        return this; // hike-update event, no state change
    }
    
    @Override
    public AbstractState activation(Context context) {
        context.updateHike();
        context.activateHike();
        return new HikeActivated();
    }
}