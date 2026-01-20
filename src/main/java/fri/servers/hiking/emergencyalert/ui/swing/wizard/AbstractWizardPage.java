package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;

/**
 * Shared logic for all wizard pages.
 */
public abstract class AbstractWizardPage
{
    private final JPanel addablePanel;
    private final JPanel contentPanel;
    private Trolley trolley;
    private boolean uiWasBuilt;

    /** Constructor visible to sub-classes only. */
    protected AbstractWizardPage() {
        this.addablePanel = new JPanel(new BorderLayout());
        this.contentPanel = new JPanel(new BorderLayout());
        
        addablePanel.add(contentPanel, BorderLayout.CENTER);
        
        addablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createLineBorder(Color.GRAY))
            );
    }
    
    /** Package-visible for HikeWizard only, not for subclasses! */
    JComponent getAddablePanel() {
        return addablePanel;
    }
    
    
    /** Called just once for each wizard-page. Sub-classes must fill their UI with fields. */
    protected abstract void buildUi();
    
    /** Called on every page change, sub-classes must fill their fields with data from the given Hike. */
    protected abstract void populateUi(Hike hike);

    /**
     * Called when entering this page. 
     * Build UI when not done, populate UI with data.
     * @param trolley contains StateMachine, Hike and other data passed between pages.
     */
    public final void enter(Trolley trolley) {
        this.trolley = trolley;
        if (uiWasBuilt == false) {
            buildUi();
            uiWasBuilt = true;
        }
        populateUi(trolley.stateMachine.getHike());
    }

    /**
     * Called when going forward or backward to another page. 
     * @return null when <code>commit()</code> returned null, else the trolley.
     */
    public final Trolley leave() {
        return commit() ? trolley : null;
    }
    
    /**
     * Called when going forward or backward to another page. 
     * Commit UI fields into Hike data. 
     * @return true when can leave page.
     */
    protected abstract boolean commit();

    /**
     * Called when user closes the window.
     * Opens a save-data dialog and optionally saves Hike to file.
     * To be overridden with <code>return true</code> when not having data to save.
     * @return true when user clicked NO or save succeeded, else false.
     */
    public boolean windowClosing() {
        commit();
        
        if (trolley.hikeChanged()) {
            final int saveChanges = JOptionPane.showConfirmDialog(
                    getFrame(), 
                    i18n("Save hike inputs?"), 
                    i18n("Confirm Termination"), 
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
            if (saveChanges == JOptionPane.YES_OPTION) {
                try {
                    saveHikeToFile();
                    return true;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            getFrame(),
                            e.toString(),
                            i18n("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else if (saveChanges == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    private void saveHikeToFile() throws IOException {
        commit();
        throw new RuntimeException("Implement me");
    }

    /** @return parent for any dialog. */
    protected final JFrame getFrame() {
        return (JFrame) SwingUtilities.windowForComponent(addablePanel);
    }
    
    /** @return any field must be added to this panel, by default it has BorderLayout. */
    protected final JPanel getContentPanel() {
        return contentPanel;
    }
    
    /** @return the Trolley that was set into this page. */
    protected final Trolley getTrolley() {
        return trolley;
    }
    /** @return the StateMachine from trolley. */
    protected final StateMachine getStateMachine() {
        return getTrolley().stateMachine;
    }
    /** @return the Hike from StateMachine. */
    protected final Hike getHike() {
        return getStateMachine().getHike();
    }
    
    protected final void setWaitCursor() {
        getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    protected final void setDefaultCursor() {
        getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}