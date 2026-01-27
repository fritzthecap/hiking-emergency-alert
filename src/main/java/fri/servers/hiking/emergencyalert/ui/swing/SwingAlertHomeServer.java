package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.HikeWizard;

/**
 * The Swing UI for starting a hike observation.
 */
public class SwingAlertHomeServer extends SwingUserInterface
{
    /**
     * Called from super-constructor, so do not use
     * instance-fields here, they are not yet initialized!
     */
    @Override
    protected JFrame buildUi() {
        final StateMachine stateMachine = new StateMachine(
                new Hike(), 
                new MailerImpl(), 
                new HikeTimer(), 
                this);
        
        final JFrame frame = new JFrame();
        final HikeWizard hikeInputWizard = new HikeWizard(frame, stateMachine);
        frame.getContentPane().add(hikeInputWizard);
        
        return frame;
    }
    
    /** Required call to show the window on screen. */
    public void show(String title) {
        frame.setTitle(title);
        frame.pack();
        frame.setSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /** Overridden to notify the wizard's ObservationPage. */
    @Override
    public void showConfirmMail(Mail alertConfirmationMail) {
        SwingUtilities.invokeLater(() -> {
            final HikeWizard wizard = (HikeWizard) frame.getContentPane().getComponent(0);
            wizard.alertConfirmed();
        });
        
        super.showConfirmMail(alertConfirmationMail);
    }
}