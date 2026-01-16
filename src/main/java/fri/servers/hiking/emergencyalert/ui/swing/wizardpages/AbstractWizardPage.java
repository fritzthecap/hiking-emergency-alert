package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;

/**
 * Shared logic for all wizard pages.
 */
public abstract class AbstractWizardPage extends JPanel
{
    private StateMachine stateMachine;
    private AbstractWizardPage previousPage;

    /** Constructor visible to sub-classes only. */
    protected AbstractWizardPage() {
        super(new BorderLayout());
        
        final int SPACE = 8;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SPACE, SPACE, 0, SPACE),
                BorderFactory.createLineBorder(Color.GRAY))
            );
    }
    
    /** Required post-constructor call! */
    public AbstractWizardPage setData(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        populateUi(stateMachine.getHike());
        return this;
    }

    /** @return the Hike that was set into this page. */
    protected final StateMachine getData() {
        return stateMachine;
    }
    
    /** Sub-classes must fill their UI with data, the Hike has been passed. */
    protected void populateUi(Hike hike) {
        add(new JLabel(getClass().getSimpleName(), JLabel.CENTER));
    }
    
    /** @return true when there is a next page to this. */
    public boolean hasNextPage() {
        return nextPage() != null;
    }
    
    /** @return the next wizard page on "Next" button click. */
    public AbstractWizardPage getNextPage() {
        final AbstractWizardPage nextPage = nextPage();
        if (nextPage != null)
            nextPage.setData(stateMachine).setPreviousPage(this); // pass this' data to next page
        return nextPage;
    }

    /** @return true when there is a previous page to this. */
    public boolean hasPreviousPage() {
        return previousPage != null;
    }
    
    /** @return the previous wizard page on "Previous" button click. */
    public AbstractWizardPage getPreviousPage() {
        if (hasPreviousPage())
            previousPage.setData(stateMachine); // every page could possibly change the hike pointer!
        return previousPage;
    }

    /**
     * Called when user closed the window. Does nothing. 
     * To be overridden for saving data.
     */
    public void windowClosing() {
    }

    /** @return the next wizard page, to be implemented by sub-classes. */
    protected abstract AbstractWizardPage nextPage();
    
    /**
     * Some wizard pages may want to avoid application exit
     * through the top right window close button.
     * This default implementation returns true.
     * @return true when application may still exit on this page, else false.
     */
    public boolean frameCanBeClosed() {
        return true;
    }
    
    private AbstractWizardPage setPreviousPage(AbstractWizardPage wizardPage) {
        this.previousPage = wizardPage;
        return this;
    }
}