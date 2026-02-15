package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

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

    /** Called by StateMachine, shows a possible contact confirmation mail in a dialog. */
    @Override
    public void showConfirmMail(final Mail alertConfirmationMail) {
        SwingUtilities.invokeLater(() -> {
            final String mailInfoText = 
                    alertConfirmationMail.from()+"\n"+
                    DateUtil.toString(alertConfirmationMail.sent(), true);
            
            final JTextArea mailInfoComponent = new JTextArea(mailInfoText);
            mailInfoComponent.setEditable(false);
            mailInfoComponent.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            
            JOptionPane.showMessageDialog(
                    frame, 
                    mailInfoComponent, 
                    "Alert Confirmation Arrived", 
                    JOptionPane.INFORMATION_MESSAGE);
             });
   }
}