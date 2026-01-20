package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Texts that will appear in mails, exclusive route.
 * Registration can be saved to JSON when finished here.
 */
public class MailTextsPage extends AbstractWizardPage
{
    @Override
    protected void buildUi() {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    protected void populateUi(Hike hike) {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    public boolean commit() {
        return true;
    }
}