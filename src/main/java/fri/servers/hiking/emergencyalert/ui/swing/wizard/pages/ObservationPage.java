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
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.statemachine.states.HikeActivated;
import fri.servers.hiking.emergencyalert.statemachine.states.OnTheWay;
import fri.servers.hiking.emergencyalert.statemachine.states.OverdueAlert;
import fri.servers.hiking.emergencyalert.ui.swing.Log;
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
    protected String getTitle() {
        return i18n("Observation");
    }
    
    /** Overridden to avoid "Next" being enabled. There is nothing to validate here. */
    @Override
    protected boolean validate() {
        return true;
    }
    
    /** Prevent going back to previous page while stateMachine is running. */
    @Override
    protected boolean commit(boolean goingForward) {
        return canClose;
    }
    
    /** @return false when hike is still running, else true. */
    @Override
    public boolean windowClosing() {
        return canClose;
    }

    /** Called when an alert confirmation mail was received. */
    public void alertConfirmed() {
        endState(Color.BLUE); // hiker had an accident
    }
    
    @Override
    protected void buildUi() {
        // top
        instructionsArea = new JTextArea();
        final int BORDER = 12; // empty border space
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        instructionsArea.setEditable(false);
        instructionsArea.setOpaque(false);
        timePanel = new JLabel("", JLabel.CENTER);
        timePanel.setFont(timePanel.getFont().deriveFont(20f));
        
        // center
        consoleOut = new JTextArea();
        consoleOut.setEditable(false);
        consoleOut.setBackground(new Color(0, 255, 0, 42)); // LIGHT_GREEN
        Log.redirectOut(consoleOut);
        
        consoleErr = new JTextArea();
        consoleErr.setEditable(false);
        consoleErr.setBackground(new Color(255, 0, 0, 42)); // LIGHT_RED
        Log.redirectErr(consoleErr);
        
        // bottom
        homeAgainListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homeAgain(getStateMachine(), homeAgain);
            }
        };
        homeAgain = new JButton(i18n("Home Again"));
        
        final JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.add(timePanel, BorderLayout.NORTH);
        instructionsPanel.add(instructionsArea, BorderLayout.CENTER);
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(scrollPaneForColoredConsole(splitPane, consoleOut, "Progress"));
        splitPane.setBottomComponent(scrollPaneForColoredConsole(splitPane, consoleErr, "Errors"));
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
        final String plannedHome = DateUtil.toString(hike.getPlannedHome());
        
        final String instructions = 
                i18n("This window can be closed only by the 'Home Again' button.")+"\n"+
                i18n("Click it as soon as you return.")+"\n"+
                i18n("Emergency alert mails will be sent starting from")+" "+plannedHome+".\n"+
                i18n("If you kill this window, the running observation will end!");
        instructionsArea.setText(instructions);
        timePanel.setText(DateUtil.toString(hike.getPlannedBegin())+"   \u2192   "+plannedHome); // arrow right
        
        consoleOut.setText("");
        consoleErr.setText("");
        
        canClose = false;
        getTrolley().setPreviousEnabled(false);
        
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
                    getStateMachine().getMailer().setCheckedAuthentication(authenticator);
                
                // change to state HikeActivated
                getStateMachine().getUserInterface().activateHike(hike);
            }
            catch (Exception e) { // validation assertions could strike
                endState(null);
                throw e; // will be visible in consoleErr
            }
        });
    }


    private JScrollPane scrollPaneForColoredConsole(final JSplitPane splitPane, JTextArea console, String title) {
        final JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setBorder(BorderFactory.createTitledBorder(i18n(title)));
        
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

    private void homeAgain(StateMachine stateMachine, JButton homeAgain) {
        String message = null;
        if (stateMachine.getState().getClass().equals(HikeActivated.class)) // hike has not even started
            message = i18n("You want to cancel the hike?");
        else if (stateMachine.getState().getClass().equals(OnTheWay.class)) // home before planned end
            message = i18n("Welcome back, you are in time!");
        else if (stateMachine.getState().getClass().equals(OverdueAlert.class)) // home after first alert mail
            message = i18n("Welcome back, you are late!");
        
        if (message != null) {
            message += "\n\n"+
                    i18n("Press 'Yes' if that is you, ")+getHike().getAlert().getNameOfHiker()+",\n"+
                    i18n("or 'No' to continue the running observation.\n");
            final int response = JOptionPane.showConfirmDialog(
                    homeAgain,
                    message,
                    i18n("Confirm Termination"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (response == JOptionPane.YES_OPTION) {
                endState(Color.GREEN.darker().darker());
                getStateMachine().getUserInterface().comingHome();
            }
        }
    }
    
    private void endState(Color homeAgainColor) {
        if (homeAgainColor != null)
            homeAgain.setForeground(homeAgainColor);
        else
            homeAgain.setEnabled(false);
        
        homeAgain.removeActionListener(homeAgainListener);
        
        canClose = true; // allows window close
        
        getTrolley().setPreviousEnabled(true);
    }
}