package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import javax.swing.JTextArea;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailBuilder;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.Log;

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
        final ObservationPage page = (ObservationPage) super.getNextPage();
        
        final JTextArea consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        Log.redirectOutAndErr(consoleArea);
        
        page.setConsole(consoleArea);
        
        final StateMachine stateMachine = getData();
        stateMachine.getUserInterface().activateHike(stateMachine.getHike());
        
        return page;
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final Contact contact = hike.getAlert().getNonAbsentContacts().get(0);
        
        final MailBuilder mailBuilder = new MailBuilder(contact, hike);
        final Mail alertMail = mailBuilder.buildAlertMail();
        final Mail passingToNext = mailBuilder.buildPassingToNextMail();
        
        final String text = "";
    }
}