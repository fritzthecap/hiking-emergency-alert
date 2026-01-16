package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Dimension;
import javax.swing.JFrame;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.time.HikeTimer;

/**
 * The Swing UI for starting a hike observation.
 */
public class SwingAlertHomeServer extends SwingUserInterface
{
    @Override
    protected JFrame buildUi() {
        final StateMachine stateMachine = new StateMachine(
                new Hike(), 
                new MailerImpl(), 
                new HikeTimer(), 
                this);
        
        final JFrame frame = new JFrame();
        final HikeInputWizard hikeInputWizard = new HikeInputWizard(frame, stateMachine);
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
}