package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import javax.swing.JOptionPane;

/**
 * Shows all data to user and asks for activation.
 * On window close, ask for saving data to file!
 */
public class ActivationPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new ObservationPage();
    }
    
    @Override
    public AbstractWizardPage getNextPage() {
        final String message = 
                i18n("Are you sure that you want to start the hike now?");
        final int response = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Hike Begin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (response != JOptionPane.YES_OPTION)
            return null;

        return super.getNextPage();
    }

//    protected void buildUi() {
//    }

//    @Override
//    protected void populateUi(Hike hike) {
//        final Contact contact = hike.getAlert().getNonAbsentContacts().get(0);
//        
//        final MailBuilder mailBuilder = new MailBuilder(contact, hike);
//        final Mail alertMail = mailBuilder.buildAlertMail();
//        final Mail passingToNextMail = mailBuilder.buildPassingToNextMail();
//        
//        final String text = "";
//    }
}