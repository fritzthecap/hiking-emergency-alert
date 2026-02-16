package fri.servers.hiking.emergencyalert.ui.swing;

import javax.swing.JFrame;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

/**
 * Basic user-interface providing an interactive mail <code>Authenticator</code>
 * but nothing else.
 */
public class SwingUserInterface extends UserInterface
{
    protected final JFrame frame;
    
    public SwingUserInterface() {
        frame = buildUi();
        
        interactiveAuthenticatorFactory = new InteractiveAuthenticatorFactory() {
            @Override
            public Authenticator newAuthenticator() {
                return new InteractiveAuthenticator(frame);
            }
        };
    }
    
    /**
     * Called from <code>SwingUserInterface()</code> constructor.
     * Builds the wizard that lets edit Hike data.
     * This implementation returns null, to be overridden.
     * @return the Swing parent window to be used as dialog parent.
     */
    protected JFrame buildUi() {
        return null;
    }

    /** Called by StateMachine, renders the contact confirmation mail. */
    @Override
    public void showConfirmMail(final Mail alertConfirmationMail) {
        System.out.println(
                "Alert Confirmation Mail:\n"+
                "    "+alertConfirmationMail.from()+"\n"+
                "    "+DateUtil.toString(alertConfirmationMail.sent(), true));
   }
}