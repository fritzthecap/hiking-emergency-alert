package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Texts that will appear in mails, exclusive route.
 * Registration can be saved to JSON when finished here.
 */
public class MailTextsPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new RoutePage();
    }
}