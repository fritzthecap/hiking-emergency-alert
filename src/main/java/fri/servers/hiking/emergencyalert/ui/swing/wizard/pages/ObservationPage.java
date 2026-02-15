package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.Log;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import jakarta.mail.Authenticator;

/**
 * After activation this page shows StateMachine
 * console output and the "Home Again" button.
 */
public class ObservationPage extends AbstractWizardPage
{
    private boolean canClose; // when false, StateMachine is running
    private JButton homeAgain;
    private ActionListener homeAgainListener;
    private JTextArea instructionsArea;
    private JLabel timePanel;
    private JTextArea consoleOut;
    private JTextArea consoleErr;
    
    @Override
    protected boolean shouldShowSaveButton() {
        return false;
    }
    
    @Override
    protected String getTitle() {
        return i18n("Observation");
    }
    
    /** Overridden to avoid "Next" being enabled. There is nothing to validate here. */
    @Override
    protected boolean validate() {
        return true;
    }
    
    /** @return false when hike is still running, else true. */
    @Override
    public boolean windowClosing() {
        return canClose;
    }

    /** Called when an alert confirmation mail was received. */
    public void alertConfirmed() {
        endState(false); // hiker had an accident
    }
    
    @Override
    protected void buildUi() {
        // top
        instructionsArea = new JTextArea();
        final int BORDER = 12; // empty border space
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        instructionsArea.setEditable(false);
        instructionsArea.setOpaque(false);
        timePanel = (JLabel) SwingUtil.increaseFontSize(new JLabel("", JLabel.CENTER), 160, true, false);
        
        // center
        consoleOut = new JTextArea();
        consoleOut.setEditable(false);
        consoleOut.setBackground(new Color(0, 255, 0, 42)); // LIGHT_GREEN
        
        consoleErr = new JTextArea();
        consoleErr.setEditable(false);
        consoleErr.setBackground(new Color(255, 0, 0, 42)); // LIGHT_RED
        
        // bottom
        homeAgainListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homeAgain();
            }
        };
        homeAgain = new JButton(i18n("Home Again"));
        
        final JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.add(timePanel, BorderLayout.NORTH);
        instructionsPanel.add(instructionsArea, BorderLayout.CENTER);
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(scrollPaneForColoredConsole(splitPane, consoleOut, i18n("Progress")));
        splitPane.setBottomComponent(scrollPaneForColoredConsole(splitPane, consoleErr, i18n("Errors")));
        splitPane.setResizeWeight(0.5);
        
        final JPanel buttonPanel = new JPanel(new FlowLayout()); // centers button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        buttonPanel.add(homeAgain);
        
        getContentPanel().add(instructionsPanel, BorderLayout.NORTH);
        getContentPanel().add(splitPane, BorderLayout.CENTER);
        getContentPanel().add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final String plannedBegin = (hike.getPlannedBegin() != null) ? DateUtil.toString(hike.getPlannedBegin()) : "";
        final String plannedHome = DateUtil.toString(hike.getPlannedHome());
        
        final String instructions = 
                i18n("This window can be closed only by the 'Home Again' button.")+"\n"+
                i18n("Click it as soon as you return.")+"\n"+
                i18n("Emergency alert mails will be sent starting from")+" "+plannedHome+".\n"+
                i18n("If you kill this window, the running observation will end!");
        instructionsArea.setText(instructions);
        timePanel.setText(plannedBegin+"   \u2192   "+plannedHome); // arrow right
        
        consoleOut.setText("");
        consoleErr.setText("");
        
        canClose = false;
        getTrolley().setBackwardEnabled(false);
        
        homeAgain.setForeground(Color.RED);
        homeAgain.setEnabled(true);
        homeAgain.addActionListener(homeAgainListener);
        homeAgain.setToolTipText(
                i18n("Click to stop alert mails from being sent after")+" "+plannedHome);
        
        SwingUtilities.invokeLater(() -> { // call the StateMachine
            try {
                // avoid another password dialog
                final Authenticator authenticator = getTrolley().getAuthenticator();
                if (authenticator != null)
                    getStateMachine().getMailer().setCheckedAuthenticator(authenticator);
                
                // no more UI-exceptions possible here, redirect System outputs to UI now
                Log.redirectOut(consoleOut);
                Log.redirectErr(consoleErr);
                
                // change to state HikeActivated, this triggers another mail connection check
                getStateMachine().getUserInterface().activateHike(hike);
            }
            catch (Exception e) { // validation assertions could strike
                endState(false); // stay on this hike
                throw e; // will be visible in consoleErr
            }
        });
    }

    /** Prevent going back to previous page while stateMachine is running. */
    @Override
    protected boolean commit(boolean goingForward) {
        return canClose;
    }
    

    private JScrollPane scrollPaneForColoredConsole(final JSplitPane splitPane, JTextArea console, String title) {
        final JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        
        // workaround for the Swing repaint bug when textArea is opaque and has a background color
        final AdjustmentListener scrollBarListener = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                splitPane.repaint();
            }
        };
        scrollPane.getVerticalScrollBar().addAdjustmentListener(scrollBarListener);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(scrollBarListener);
        
        return scrollPane;
    }

    private void homeAgain() {
        final StateMachine stateMachine = getStateMachine();
        String message = null;
        
        if (stateMachine.notYetOnTheWay()) // hike has not even started
            message = i18n("You want to cancel the hike?");
        else if (stateMachine.inTime()) // home before planned end
            message = i18n("Welcome back, you are in time!");
        else if (stateMachine.tooLate()) // home after first alert mail
            message = i18n("Welcome back, you are late!");
        
        if (message != null) {
            message += "\n\n"+
                i18n("Press 'Yes' if that is you, ")+getHike().getAlert().getNameOfHiker()+",\n"+
                i18n("or 'No' to continue the running observation.")+"\n";
            
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    getFrame(),
                    message,
                    i18n("Confirm Termination"),
                    JOptionPane.YES_NO_OPTION,
                    stateMachine.notYetOnTheWay() ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE))
            {
                // START keep order of statements
                final boolean inTime = stateMachine.inTime();
                getStateMachine().getUserInterface().comingHome();
                endState(inTime);
                // END keep order of statements
            }
        }
    }
    
    private void endState(boolean startNewHike) {
        homeAgain.setEnabled(false); // can not press button another time
        homeAgain.removeActionListener(homeAgainListener); // will be added again on populateUi()
        
        canClose = true; // allow to close the window
        
        getTrolley().setBackwardEnabled(true);
        
        // ask for new hike and initialize it when Yes
        if (startNewHike && 
                JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                        getFrame(),
                        i18n("Do you want to create a new hike?"),
                        i18n("Continue with New Hike?"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE))
        {
            final Hike oldHike = getHike();
            final Hike newHike = new Hike(); // new Hike contains no route yet!
            newHike.setAlert(oldHike.getAlert());
            
            getStateMachine().getUserInterface().registerHike(newHike);
            
            getTrolley().gotoPage(RouteAndTimesPage.class); // go back to "Route" page
            getTrolley().setHikeFile(null); // forget old file name
        }
    }
}