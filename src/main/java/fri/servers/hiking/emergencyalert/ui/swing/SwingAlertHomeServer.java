package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SwingAlertHomeServer extends SwingUserInterface
{
    private JFrame frame;
    
    @Override
    protected JFrame buildUi() {
        final JFrame frame = new JFrame();
        frame.getContentPane().add(buildHikeDataWizard());
        return frame;
    }
    
    public void show() {
        // TODO: manage window close button with confirmation dialog
        // Closing the window would NOT stop the StateMachine!
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private Component buildHikeDataWizard() {
        final JPanel panel = new JPanel();
        panel.add(new JLabel("Implement me!"));
        // TODO: build and assign frame
        return panel;
    }
}