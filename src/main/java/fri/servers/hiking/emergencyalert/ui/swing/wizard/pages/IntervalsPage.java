package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

/**
 * Alert overdue interval, its shrinking fraction,
 * confirmation polling interval.
 */
public class IntervalsPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return new BeginAndHomeTimePage();
    }
}