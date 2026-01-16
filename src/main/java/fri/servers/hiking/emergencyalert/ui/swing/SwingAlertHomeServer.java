package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Dimension;
import javax.swing.JFrame;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.time.HikeTimer;

public class SwingAlertHomeServer extends SwingUserInterface
{
    private HikeInputWizard hikeInputWizard;
    
    @Override
    protected JFrame buildUi() {
        final StateMachine stateMachine = new StateMachine(
                new Hike(), 
                new MailerImpl(), 
                new HikeTimer(), 
                this);
        
        final JFrame frame = new JFrame();
        this.hikeInputWizard = new HikeInputWizard(frame, stateMachine);
        frame.getContentPane().add(hikeInputWizard);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // until ACTVATION
        
        return frame;
    }
    
    public void show(String title) {
        frame.setTitle(title);
        frame.pack();
        frame.setSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    @Override
    public void comingHome() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // enable window close again
        super.comingHome();
    }
}