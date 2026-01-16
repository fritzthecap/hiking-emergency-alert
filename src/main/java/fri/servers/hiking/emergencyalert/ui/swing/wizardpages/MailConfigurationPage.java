package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Configure your mail server.
 */
public class MailConfigurationPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new ContactsPage();
    }
}