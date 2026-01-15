package fri.servers.hiking.emergencyalert.statemachine.states;

import fri.servers.hiking.emergencyalert.statemachine.AbstractState;
import fri.servers.hiking.emergencyalert.statemachine.Context;

public class HomeAgain extends AlertConfirmed // to inherit registration() and activation()
{
    /** Another press on "Home Again". */
    @Override
    public AbstractState comingHome(Context context) {
        return this;
    }
}