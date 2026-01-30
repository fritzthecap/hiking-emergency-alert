package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class AlertConfirmed extends HikerRegistered // to inherit activation()
{
    /** A new hike is going to be configured. */
    @Override
    public AbstractState registration(Context context) {
        final AbstractState followerState = new HikerRegistered();
        followerState.registration(context);
        return followerState;
    }
    
    /** Too late press on "Home Again", let it be legal. */
    @Override
    public AbstractState comingHome(Context context) {
        return new HomeAgain();
    }
    
    /** Another alert confirmation arrived from another contact. */
    @Override
    public AbstractState alertConfirmed(Context context) {
        return this;
    }
}