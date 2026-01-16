package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

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
}