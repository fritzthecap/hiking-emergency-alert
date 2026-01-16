package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Choose your UI language.
 */
public class LanguagePage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new MailConfigurationPage();
    }
}