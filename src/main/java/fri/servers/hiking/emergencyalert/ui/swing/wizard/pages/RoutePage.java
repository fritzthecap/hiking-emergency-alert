package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Route description text and image file chooser.
 */
public class RoutePage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new IntervalsPage();
    }
}