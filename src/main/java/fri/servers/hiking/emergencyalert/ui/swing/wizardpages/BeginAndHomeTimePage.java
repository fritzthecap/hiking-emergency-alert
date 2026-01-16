package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * 
 */
public class BeginAndHomeTimePage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new ActivationPage();
    }
}