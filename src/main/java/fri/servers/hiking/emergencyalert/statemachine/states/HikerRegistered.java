package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class HikerRegistered extends AbstractState
{
    @Override
    public AbstractState registration(Context context) {
        context.updateAlert();
        return this; // just answer valid event
    }
    
    @Override
    public AbstractState activation(Context context) {
        final AbstractState followerState = new HikeActivated();
        followerState.activation(context);
        return followerState;
    }
}