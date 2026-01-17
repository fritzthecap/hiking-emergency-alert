package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;

/**
 * Shared logic for all wizard pages.
 */
public abstract class AbstractWizardPage extends JPanel
{
    private StateMachine stateMachine;
    private AbstractWizardPage nextPage;
    private AbstractWizardPage previousPage;
    private boolean builtUi;

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
    public final AbstractWizardPage setData(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        if (builtUi == false) {
            buildUi();
            builtUi = true;
        }
        populateUi(stateMachine.getHike());
        return this;
    }

    /** @return the Hike that was set into this page. */
    protected final StateMachine getData() {
        return stateMachine;
    }
    
    /** This is called just once for each wizard-page. Sub-classes must fill their UI with fields. */
    protected void buildUi() {
        //add(new JLabel(getClass().getSimpleName(), JLabel.CENTER)); // TODO remove this, make method abstract!
    }
    
    /** On every page change sub-classes must fill their fields with data from the given Hike. */
    protected void populateUi(Hike hike) {
        //add(new JLabel("MAIL-ID: "+hike.uniqueMailId), BorderLayout.NORTH); // TODO remove this, make method abstract!
    }
    
    /** @return true when there is a next page to this. */
    public final boolean hasNextPage() {
        if (nextPage != null)
            return true;
        return (nextPage = nextPage()) != null;
    }
    
    /** @return the next wizard page on "Next" button click. */
    public AbstractWizardPage getNextPage() {
        final AbstractWizardPage next = (nextPage != null) ? nextPage : nextPage();
        if (next != null)
            next.setPreviousPage(this).setData(stateMachine); // pass this' data to next page
        return next;
    }

    /** @return true when there is a previous page to this. */
    public final boolean hasPreviousPage() {
        return previousPage != null;
    }
    
    /** @return the previous wizard page on "Previous" button click. */
    public AbstractWizardPage getPreviousPage() {
        if (hasPreviousPage())
            previousPage.setData(stateMachine); // every page could possibly change the hike pointer!
        return previousPage;
    }

    /**
     * Called when user closed the window.
     * @return true for let window exit. To be overridden for saving data.
     */
    public boolean windowClosing() {
        return true;
    }

    /** @return the next wizard page, to be implemented by sub-classes. */
    protected abstract AbstractWizardPage nextPage();
    
    private AbstractWizardPage setPreviousPage(AbstractWizardPage wizardPage) {
        this.previousPage = wizardPage;
        return this;
    }
}