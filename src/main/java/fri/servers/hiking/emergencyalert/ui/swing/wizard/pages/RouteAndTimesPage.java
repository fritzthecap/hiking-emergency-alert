package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import javax.swing.JList;
import javax.swing.JTextField;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Route description text and image file chooser.
 */
public class RouteAndTimesPage extends AbstractWizardPage
{
    private JTextField routeField;
    private JList<String> routeImagesField;
    private JTextField plannedBeginField;
    private JTextField plannedHomeField;

    @Override
    protected void buildUi() {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    protected void populateUi(Hike hike) {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    protected boolean commit(boolean goingForward) {
        throw new RuntimeException("Implement me!");
    }
}