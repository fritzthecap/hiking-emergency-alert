package fri.servers.hiking.emergencyalert.statemachine;

/**
 * Implements an error-by-default response for all events,
 * to be specifically overridden by sub-classes.
 */
public abstract class AbstractState
{
    public AbstractState registration(Context context) {
        return wrongState(context);
    }
    
    public AbstractState activation(Context context) {
        return wrongState(context);
    }
    
    public AbstractState settingOff(Context context) {
        return wrongState(context);
    }
    
    public AbstractState comingHome(Context context) {
        return wrongState(context);
    }
    
    public AbstractState overdueAlert(Context context) {
        return wrongState(context);
    }
    
    public AbstractState alertConfirmed(Context context) {
        return wrongState(context);
    }
    
    /** Call this when transition is illegal. */
    protected final AbstractState wrongState(Context context) {
        throw new IllegalStateException(
                "No event '"+context.getEvent()+"' expected in state "+
                getClass().getSimpleName()+", developer fault!");
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}