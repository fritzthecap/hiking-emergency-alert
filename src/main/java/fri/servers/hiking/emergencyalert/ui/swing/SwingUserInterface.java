package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.util.DateUtil;

public class SwingUserInterface extends UserInterface
{
    private Component dialogParentWindow;
    
    public SwingUserInterface() {
        dialogParentWindow = buildUi();
        interactiveAuthenticator = new InteractiveAuthenticator(dialogParentWindow);
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

    /** Shows confirmation mail data in a dialog. */
    @Override
    public void showConfirmMail(Mail alertConfirmationMail) {
        final String mailInfoText = 
                alertConfirmationMail.from()+"\n"+
                alertConfirmationMail.subject()+"\n"+
                DateUtil.toString(alertConfirmationMail.sent(), true);
        
        final JTextArea mailInfoComponent = new JTextArea(mailInfoText);
        mailInfoComponent.setEditable(false);
        mailInfoComponent.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        
        JOptionPane.showMessageDialog(
                dialogParentWindow, 
                mailInfoComponent, 
                "Alert Confirmation Arrived", 
                JOptionPane.INFORMATION_MESSAGE);
    }
}