package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Hiker contact and address, and a list of mail contacts.
 */
public class ContactsPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new MailTextsPage();
    }
}