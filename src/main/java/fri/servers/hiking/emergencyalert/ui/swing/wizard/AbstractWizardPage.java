package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
    
    private JLabel errorField = new JLabel();


    /** Constructor visible to sub-classes only. */
    protected AbstractWizardPage() {
        this.addablePanel = new JPanel(new BorderLayout());
        this.contentPanel = new JPanel(new BorderLayout());
        
        addablePanel.add(contentPanel, BorderLayout.CENTER);
        
        addablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createLineBorder(Color.GRAY))
            );
        
        errorField = new JLabel(" ");
        errorField.setForeground(Color.RED);
        //errorField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        final JPanel errorPanel = new JPanel();
        errorPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        errorPanel.add(errorField);
        addablePanel.add(errorPanel, BorderLayout.NORTH);
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
     * @param goingForward true when going to next page, false when to previous.
     * @param trolley contains StateMachine, Hike and other data passed between pages.
     */
    public final void enter(Trolley trolley, boolean goingForward) {
        setWaitCursor();
        try {
            this.trolley = trolley;
            if (uiWasBuilt == false) {
                buildUi();
                uiWasBuilt = true;
            }
            
            if (goingForward) {
                populateUi(trolley.stateMachine.getHike());
                validate();
            }
        }
        finally {
            setDefaultCursor();
        }
    }

    /**
     * Called when going forward or backward to another page. 
     * @param goingForward true when going to next page, false when to previous.
     * @return null when <code>commit()</code> returned null, else the trolley.
     */
    public final Trolley leave(boolean goingForward) {
        setWaitCursor();
        try {
            if (goingForward) {
                final boolean valid = validate();
                if (valid == false)
                    return null;
            }
            return commit(goingForward) ? trolley : null;
        }
        finally {
            setDefaultCursor();
        }
    }

    /**
     * Calls <code>validateFields()</code> and sets the resulting error to error-field.
     * Enables the "Next" button when valid.
     * @return true when no error occurred, else false.
     */
    protected final boolean validate() {
        final String error = validateFields();
        final boolean valid = (error == null);
        errorField.setText(valid ? "" : error);
        trolley.setNextEnabled(valid);
        return valid;
    }
    
    /**
     * Called when going forward to next page. Validate UI fields. 
     * This default implementation always returns null. To be overridden.
     * @return null when all fields are valid, else an error message about the first invalid field.
     */
    protected String validateFields() {
        return null;
    }

    /**
     * Called when going forward or backward to another page, or user closes window. 
     * Commit UI fields into Hike data. 
     * @param goingForward true when "Next" was clicked, false when "Previous" or user closes window.
     * @return true when can leave page or not going forward, false when validation did not succeed.
     */
    protected abstract boolean commit(boolean goingForward);

    /**
     * Called when user closes the window.
     * Opens a save-data dialog and optionally saves Hike to file.
     * To be overridden with <code>return true</code> when not having data to save.
     * @return true when user clicked NO or save succeeded, else false.
     */
    public boolean windowClosing() {
        setWaitCursor();
        try {
            commit(false);
            
            if (trolley.isHikeChanged()) {
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
        }
        finally {
            setDefaultCursor();
        }
        
        return true;
    }

    private void saveHikeToFile() throws IOException {
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
    
    protected final JLabel getErrorField() {
        return errorField;
    }
    
    protected final void setWaitCursor() {
        final Container contentPane = (getFrame() != null) ? getFrame().getContentPane() : null;
        if (contentPane != null)
            getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    protected final void setDefaultCursor() {
        final Container contentPane = (getFrame() != null) ? getFrame().getContentPane() : null;
        if (contentPane != null)
            getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}