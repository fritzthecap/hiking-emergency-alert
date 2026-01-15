package fri.servers.hiking.emergencyalert.statemachine;

/**
 * Enumerates all possible events and binds them to method calls on states.
 */
public enum Event
{
    /** The alert texts were written, with all necessary data of the hiker and a list of contacts. */
    REGISTRATION {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.registration(context);
        }
    },
    /** The hike was described with all necessary informations for rescuers, and the timer gets started. */
    ACTIVATION {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.activation(context);
        }
    },
    /** The timer detected that the set-off time of the hike has passed. */
    SETTING_OFF {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.settingOff(context);
        }
    },
    /** The user came home and deactivated the hike. */
    COMING_HOME {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.comingHome(context);
        }
    },
    /** The timer detected that the coming-home time has passed, or the wait-interval for alert-confirmations has passed. */
    OVERDUE_ALERT {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.overdueAlert(context);
        }
    },
    /** One of the contacts answered the alert message that was sent to him. */
    ALERT_CONFIRMED {
        @Override
        public AbstractState transition(AbstractState state, Context context) {
            return state.alertConfirmed(context);
        }
    },
    ;
    
    /**
     * Invokes the method on given state that belongs to this event.
     * @param state the state on which to invoke the event-method.
     * @param context carries out transition actions.
     */
    public abstract AbstractState transition(AbstractState state, Context context);
}