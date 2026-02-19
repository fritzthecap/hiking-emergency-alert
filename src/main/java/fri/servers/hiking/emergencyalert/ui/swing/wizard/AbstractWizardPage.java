package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.entities.Day;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.util.FileChooser;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * Shared logic for all wizard pages.
 */
public abstract class AbstractWizardPage
{
    private final JPanel addablePanel;
    private final JPanel contentPanel;
    private final JLabel titleField;
    private final JTextArea errorField;
    
    private Trolley trolley;
    
    private JButton saveButton;
    private FileChooser saveFileChooser;
    private UninstallableFocusListener focusListener;
    
    /** Constructor visible to sub-classes only. */
    protected AbstractWizardPage() {
        titleField = (JLabel) SwingUtil.increaseFontSize(new JLabel(), 160, true, false);
        titleField.setHorizontalAlignment(JLabel.CENTER);
        
        errorField = new JTextArea();
        errorField.setEditable(false);
        errorField.setLineWrap(true);
        errorField.setWrapStyleWord(true);
        errorField.setForeground(Color.RED);
        errorField.setOpaque(false);
        errorField.setRows(1);
        
        final JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.add(errorField, BorderLayout.CENTER);
        
        final JPanel titleAndError = new JPanel(new BorderLayout());
        titleAndError.add(titleField, BorderLayout.CENTER);
        titleAndError.add(errorPanel, BorderLayout.SOUTH);
        
        this.addablePanel = new JPanel(new BorderLayout());
        addablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createLineBorder(Color.GRAY))
            );
        
        addablePanel.add(titleAndError, BorderLayout.NORTH);
        
        this.contentPanel = new JPanel(new BorderLayout());
        addablePanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    /** Package-visible for HikeWizard only, not for sub-classes in different package! */
    JComponent getAddablePanel() {
        return addablePanel;
    }
    
    
    /** @return true when this page should show a save-button. By default this returns true. */
    protected boolean shouldShowSaveButton() {
        return true;
    }

    /** @return the title of this wizard page, appearing on top. */
    protected abstract String getTitle();

    /** Called just once for each wizard-page. Sub-classes must fill their UI with fields. */
    protected abstract void buildUi();
    
    /** Called on every page change, sub-classes must fill their fields with data from the given Hike. */
    protected abstract void populateUi(Hike hike);

    /**
     * Called when entering this page. Builds and populates UI with data.
     * @param goingForward true when going to next page, false when to previous.
     * @param trolley contains StateMachine, Hike and other data passed between pages.
     */
    public final void enter(Trolley trolley, boolean goingForward) {
        this.trolley = trolley;
        
        contentPanel.removeAll();
        
        titleField.setText(getTitle());
        ensureSaveButton(); // i18n is available now
        
        buildUi();
        populateUi(getHike());
        
        if (goingForward)
            validate();
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Calls <code>validateFields()</code> and sets the resulting error to error-field.
     * Enables the "Next" button when valid.
     * @return true when no error occurred, else false.
     */
    protected boolean validate() {
        final String error = validateFields();
        final boolean valid = (error == null);
        errorField.setText(valid ? "" : error);
        trolley.setForwardEnabled(valid);
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
     * Installs a validating focus-listeners onto all given components.
     * Any already existing focus-listener will be released.
     * @param afterValidate something to be executed after validation, argument will be <code>valid</code>.
     */
    protected void installFocusListener(JComponent[] components, Consumer<Boolean> afterValidate) {
        if (focusListener != null)
            focusListener.uninstall(); // enable garbage-collection
        
        focusListener = new UninstallableFocusListener(components, afterValidate);
    }

    
    /**
     * Called when going forward or backward to another page. 
     * @param goingForward true when going to next page, false when to previous.
     * @return null when <code>commit()</code> returned false, else the trolley.
     */
    public final Trolley leave(boolean goingForward) {
        if (goingForward) {
            final boolean valid = validate();
            if (valid == false)
                return null;
        }
        
        final boolean committed = commit(goingForward);
        
        if (committed && focusListener != null) {
            focusListener.uninstall(); // enable garbage-collection
            focusListener = null;
        }
        
        return committed ? trolley : null;
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
        commit(false); // false: do not validate mail connection now
        return askForSaveWhenChanged(i18n("Confirm Termination"), (trolley.getHikeFile() != null));
    }

    /**
     * Opens a Save-dialog when hike was changed.
     * @return true when no error happened or user answered "No", else false ("Cancel").
     */
    protected boolean askForSaveWhenChanged(String title, boolean letChooseTargetFile) {
        if (trolley.isHikeChanged()) {
            final int saveChanges = JOptionPane.showConfirmDialog(
                    getFrame(), 
                    i18n("Save hike inputs?"), 
                    title, 
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
            
            if (saveChanges == JOptionPane.YES_OPTION)
                return saveHikeToFile(letChooseTargetFile);
            else if (saveChanges == JOptionPane.CANCEL_OPTION)
                return false;
        }
        return true; // no change detected, or answer was "No"
    }

    /** Reused logic to verify hike times. */
    protected String validateHikeTimes(Date beginDateTime, List<Day> days) {
        if (beginDateTime != null) { // begin is optional!
            // when given, hike duration must be at least one minute
            if (isOneMinuteAfter(beginDateTime, days.get(0).getPlannedHome()) == false)
                return i18n("The planned end time must be after begin!");
        }
        
        Date previousHomeTime = DateUtil.now();
        int dayNumber = 1;
        for (Day day : days) { // check whether days are sorted by home-time
            // each day's home-time must be at least one minute after previous day's home-time
            if (isOneMinuteAfter(previousHomeTime, day.getPlannedHome()) == false)
                return i18n("Day")+" "+dayNumber+": "+i18n("The planned end time must be in future!");
            
            previousHomeTime = day.getPlannedHome();
            dayNumber++;
        }
        
        return null;
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
        final Container contentPane = (getFrame() != null) ? getFrame().getContentPane() : null;
        if (contentPane != null)
            getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    protected final void setDefaultCursor() {
        final Container contentPane = (getFrame() != null) ? getFrame().getContentPane() : null;
        if (contentPane != null)
            getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    

    private boolean isOneMinuteAfter(Date beforeDate, Date afterDate) {
        final long millisInFuture = afterDate.getTime() - beforeDate.getTime();
        return (millisInFuture / 60000 >= 1);
    }
    
    /**
     * Called by windowClose() on any page, and from ActivationPage.commit().
     * @param letChooseTargetFile true when coming from ActivationPage.commit(),
     *      or from <code>windowClose()</code> when <code>trolley.getHikeFile() != null</code>;
     *      false when coming from <code>windowClose()</code> and <code>trolley.getHikeFile() == null</code>.
     */
    private boolean saveHikeToFile(boolean letChooseTargetFile) {
        final File hikeFile = trolley.getHikeFile();
        final boolean alreadyChosenFile = (hikeFile != null);
        final HikeFileManager hikeFileManager = new HikeFileManager();
        final File targetFile;
        
        if (letChooseTargetFile) { // target is to save e.g. to hike_2026-01-28.json 
            targetFile = chooseSaveFile(hikeFile, hikeFileManager);
            if (targetFile == null) // save dialog was canceled
                return false;
        }
        else {
            targetFile = alreadyChosenFile ? hikeFile : null;
        }
        
        try {
            setWaitCursor();
            trolley.save(hikeFileManager, targetFile, getHike());
            return true;
        }
        catch (Exception e) {
            showSaveErrorDialog(e);
            return false;
        }
        finally {
            setDefaultCursor();
        }
    }

    private File chooseSaveFile(File hikeFile, HikeFileManager hikeFileManager) {
        // target is to save e.g. to hike_2026-01-28.json where 2026-01-28 is the hike begin date
        final String directory = (hikeFile != null) ? hikeFile.getParent() : hikeFileManager.getSavePath();
        
        if (saveFileChooser == null)
            saveFileChooser = new FileChooser(getContentPanel(), directory);
        
        final File suggestedFile = (hikeFile != null) 
                ? hikeFile
                : createFilenameFromHike(directory, hikeFileManager.getSaveFilename());
        
        return saveFileChooser.save(suggestedFile); // opens dialog to browse file-system
    }

    private File createFilenameFromHike(String directory, String saveFilename) {
        final String baseName = saveFilename.substring(0, saveFilename.lastIndexOf("."));
        final Date day = (getHike().getPlannedBegin() != null) 
                ? getHike().getPlannedBegin()
                : getHike().currentDay().getPlannedHome();
        final String beginDay = DateUtil.toDateString(day);
        final String customName = baseName+"_"+beginDay+".json";
        return new File(directory, customName);
    }

    private void showSaveErrorDialog(Exception e) {
        JOptionPane.showMessageDialog(
                getFrame(),
                e.toString(),
                i18n("Error"),
                JOptionPane.ERROR_MESSAGE);
    }
    
    private void ensureSaveButton() { /// called from enter()
        if (shouldShowSaveButton() == false)
            return;
        
        if (this.saveButton == null) {
            this.saveButton = new JButton(i18n("Save"));
            final JPanel titleAndError = (JPanel) titleField.getParent();
            titleAndError.add(saveButton, BorderLayout.EAST);
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveHikeToFile(trolley.getHikeFile() == null);
                }
            });
        }
    }



    private class UninstallableFocusListener extends FocusAdapter
    {
        private JComponent[] components;
        private Consumer<Boolean> afterValidate;
        
        UninstallableFocusListener(JComponent[] components, Consumer<Boolean> afterValidate) {
            this.components = components;
            this.afterValidate = afterValidate;
            
            for (JComponent component : components)
                component.addFocusListener(UninstallableFocusListener.this);
        }
        
        @Override
        public void focusLost(FocusEvent e) {
            final boolean valid = validate();
            
            if (afterValidate != null)
                afterValidate.accept(valid);
        }
        
        void uninstall() {
            for (JComponent component : components)
                component.removeFocusListener(UninstallableFocusListener.this);
            components = new JComponent[0];
        }
    }
}