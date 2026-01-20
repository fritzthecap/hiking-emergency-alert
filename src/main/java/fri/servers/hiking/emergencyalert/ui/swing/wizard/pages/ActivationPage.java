package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import javax.swing.JOptionPane;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Shows all data to user and asks for activation.
 * On window close, ask for saving data to file!
 */
public class ActivationPage extends AbstractWizardPage
{
    /** Nothing to save any more here, always returns true. */
    @Override
    public boolean windowClosing() {
        return true;
    }
    
    @Override
    protected void buildUi() {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    protected void populateUi(Hike hike) {
//        final Contact contact = hike.getAlert().getNonAbsentContacts().get(0);
//        
//        final MailBuilder mailBuilder = new MailBuilder(contact, hike);
//        final Mail alertMail = mailBuilder.buildAlertMail();
//        final Mail passingToNextMail = mailBuilder.buildPassingToNextMail();
//        
//        final String text = "";
    }
    
    @Override
    protected boolean commit() {
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

        return true;
    }

}