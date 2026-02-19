package fri.servers.hiking.emergencyalert.statemachine;

import java.util.Objects;
import fri.servers.hiking.emergencyalert.mail.Mailer;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.states.AlertConfirmed;
import fri.servers.hiking.emergencyalert.statemachine.states.HikeActivated;
import fri.servers.hiking.emergencyalert.statemachine.states.HikerRegistered;
import fri.servers.hiking.emergencyalert.statemachine.states.HomeAgain;
import fri.servers.hiking.emergencyalert.statemachine.states.OnTheWay;
import fri.servers.hiking.emergencyalert.statemachine.states.OverdueAlert;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * Finite automaton that must be driven by calling <code>dispatchEvent()</code>.
 * It covers the whole hike life-cycle from editing data and activating the hike-timer 
 * to finally detecting an alert-confirmation or the home-again user action.
 * <p/>
 * Implemented interfaces:
 * <ul>
 *  <li>User.EventDispatcher: edits and saves Alert and Hike data:
 *      REGISTRATION, ACTIVATION, COMING_HOME</li>
 *  <li>HikeTimer.EventDispatcher: fires events when planned times pass: 
 *      SETTING_OFF, OVERDUE_ALERT</li>
 *  <li>Mailer.EventDispatcher: fires when alert-confirmation found in inbox:
 *      ALERT_CONFIRMED</li>
 * </ul>
 * 
 * @see fri/servers/hiking/emergencyalert/statemachine/state-transition-diagram.html
 */
public class StateMachine implements
    UserInterface.EventDispatcher,
    HikeTimer.EventDispatcher,
    Mailer.EventDispatcher 
{
    private Context context;
    private AbstractState state = new HikerRegistered(); // initial state
    
    /**
     * Constructs a new StateMachine for observing a Hike.
     * @param hike the data of the hike, including mail configuration.
     * @param mailer the mail functionality.
     * @param timer maintains planned time-events of the hike.
     */
    public StateMachine(Hike hike, Mailer mailer, HikeTimer timer, UserInterface user) {
        Objects.requireNonNull(user).setEventDispatcher(this);
        context = newContext(hike, this, mailer, timer, user);
    }
    
    /** Sets given event into context and then calls the event's transition. */
    @Override
    public void dispatchEvent(Event event) {
        System.out.println("-> Dispatching event "+event+" at "+DateUtil.nowString());
        
        context.setEvent(event);
        state = event.transition(state, context);
        
        System.out.println("-> New state is "+state.getClass().getSimpleName());
        
        // end states need a new initialization
        if (state instanceof AlertConfirmed || state instanceof HomeAgain) { // TODO: this is ugly!
            context = new Context(context);
            
            System.out.println("StateMachine is back to initial state, you can start a new hike.");
        }
    }
    
    /** Sets given parameter into context and then calls <code>dispatchEvent(event)</code>. */
    @Override
    public void dispatchEvent(Event event, Object parameter) {
        context.setEventParameter(parameter);
        dispatchEvent(event);
    }
    
    /** Factory method that enables derived Context classes. Called from constructor. */
    protected Context newContext(Hike hike, StateMachine stateMachine, Mailer mailer, HikeTimer timer, UserInterface user) {
        return new Context(hike, stateMachine, mailer, timer, user);
    }
    
    /** @return the current state. */
    public final AbstractState getState() {
        return state;
    }
    
    /** @return the observed Hike. */
    public final Hike getHike() {
        return context.getHike();
    }
    
    /** @return the user interface. */
    public final UserInterface getUserInterface() {
        return context.getUserInterface();
    }
    
    /** @return the mail tool. */
    public final Mailer getMailer() {
        return context.getMailer();
    }
    
    /** @return true when timer is already running, i.e. ACTIVATION took place. */
    public final boolean isRunning() {
        return context.isRunning();
    }
    
    public boolean notYetOnTheWay() {
        return getState().getClass().equals(HikeActivated.class);
    }
    public boolean onTheWay() {
        return getState().getClass().equals(OnTheWay.class);
    }
    public boolean overdueAlert() {
        return getState().getClass().equals(OverdueAlert.class);
    }
}