package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Shows all data to user and asks for activation.
 * On window close, ask for saving data to file!
 */
public class ActivationPage extends AbstractWizardPage
{
    private JTextArea hikerData;
    private JTextArea contactsAndSendTimes;
    private JTextField alertMailSubject;
    private JTextArea alertMailText;
    private JTextArea attachmentFileNames;
    private JTextArea passingToNextMailText;
    
    @Override
    protected String getTitle() {
        return i18n("Activation");
    }
    
    @Override
    protected void buildUi() {
        hikerData = SwingUtil.buildTextArea(i18n("You"), null, null);
        contactsAndSendTimes= SwingUtil.buildTextArea(i18n("Contacts and planned Alert Times"), null, null);
        alertMailSubject = SwingUtil.buildTextField(i18n("Mail Subject"), null, null);
        alertMailText = SwingUtil.buildTextArea(i18n("Mail Text"), null, null);
        attachmentFileNames = SwingUtil.buildTextArea(i18n("Mail Attachments"), null, null);
        passingToNextMailText = SwingUtil.buildTextArea(i18n("Passing-to-next Mail"), null, null);
    }
    
    @Override
    protected void populateUi(Hike hike) {

        // You: (Name, Address, Phone, MailFromAddress
        // Contacts (non-absent) and planned alert receive time: 
        //     Fritz Ritzberger 2026-01-24 20:30
        //     Heiliger Geist   2026-01-24 21:30
        //     Bergrettung      2026-01-24 21:30
        // Alert Mail: 
        //     Subject
        //     Text (holds Route Description)
        //     Optional attachments
        // Optional Passing-to-next Mail: 
        
//        final Contact contact = hike.getAlert().getNonAbsentContacts().get(0);
//        
//        final MailBuilder mailBuilder = new MailBuilder(contact, hike);
//        final Mail alertMail = mailBuilder.buildAlertMail();
//        final Mail passingToNextMail = mailBuilder.buildPassingToNextMail();
//        
//        final String text = "";
    }
    
    @Override
    protected boolean commit(boolean goingForward) {
        if (goingForward) {
            final String message = 
                    i18n("Are you sure that you want to start the hike now?");
            final int response = JOptionPane.showConfirmDialog(
                    getContentPanel(),
                    message,
                    "Confirm Hike Begin",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (response != JOptionPane.YES_OPTION)
                return false;
        }
        return true; // nothing else to commit here
    }
}