package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

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
import fri.servers.hiking.emergencyalert.statemachine.states.OnTheWay;
import fri.servers.hiking.emergencyalert.statemachine.states.OverdueAlert;
import fri.servers.hiking.emergencyalert.ui.swing.Log;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * After activation this page shows StateMachine
 * console output and the "Home Again" button.
 */
public class ObservationPage extends AbstractWizardPage
{
    private boolean canClose; // StateMachine is running
    private JButton homeAgain;
    private ActionListener homeAgainListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            homeAgain(getData(), homeAgain);
        }
    };
    private JTextArea instructionsArea;
    private JLabel timePanel;
    private JTextArea consoleOut;
    private JTextArea consoleErr;
    
    @Override
    protected AbstractWizardPage nextPage() {
        return null; // makes "Next" button disabled
    }
    
    @Override
    public AbstractWizardPage getPreviousPage() {
        if (canClose == false)
            return null; // makes "Previous" button disabled
        
        return super.getPreviousPage();
    }
    
    /**
     * Called when user tries to close window.
     * @return false when hike is still running, else true.
     */
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
        final int BORDER = 16; // empty border space
        
        // top
        instructionsArea = new JTextArea();
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        instructionsArea.setEditable(false);
        instructionsArea.setOpaque(false);
        final JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.add(instructionsArea, BorderLayout.CENTER);
        timePanel = new JLabel("", JLabel.CENTER);
        timePanel.setFont(timePanel.getFont().deriveFont(20f));
        instructionsPanel.add(timePanel, BorderLayout.SOUTH);
        add(instructionsPanel, BorderLayout.NORTH);
        
        // center
        consoleOut = new JTextArea();
        consoleOut.setEditable(false);
        consoleOut.setBackground(new Color(0, 255, 0, 42)); // LIGHT_GREEN
        Log.redirectOut(consoleOut);
        
        consoleErr = new JTextArea();
        consoleErr.setEditable(false);
        consoleErr.setBackground(new Color(255, 0, 0, 42)); // LIGHT_RED
        Log.redirectErr(consoleErr);
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(newJScrollPane(splitPane, consoleOut, "Progress"));
        splitPane.setBottomComponent(newJScrollPane(splitPane, consoleErr, "Errors"));
        splitPane.setResizeWeight(0.5);
        
        add(splitPane, BorderLayout.CENTER);
        
        // bottom
        homeAgain = new JButton(i18n("Home Again"));
        final JPanel buttonPanel = new JPanel(new FlowLayout()); // centers button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        buttonPanel.add(homeAgain);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final String plannedHome = DateUtil.toString(hike.getPlannedHome());
        
        final String instructions = 
                i18n("This window can be closed only by stopping the hike observation.")+"\n"+
                i18n("Click the 'Home Again' button as soon as you return from your hike.")+"\n"+
                i18n("Emergency alert mails will be sent starting from")+" "+plannedHome;
        instructionsArea.setText(instructions);
        timePanel.setText(DateUtil.toString(hike.getPlannedBegin())+"   \u2192   "+plannedHome); // arrow right
        
        consoleOut.setText("");
        consoleErr.setText("");
        
        canClose = false;
        
        homeAgain.setForeground(Color.RED);
        homeAgain.setEnabled(true);
        homeAgain.addActionListener(homeAgainListener);
        homeAgain.setToolTipText(
                i18n("Click to stop alert mails from being sent after")+" "+plannedHome);
        
        SwingUtilities.invokeLater(() -> {
            try {
                final StateMachine stateMachine = getData();
                stateMachine.getUserInterface().activateHike(hike);
            }
            catch (Exception e) { // validation assertions could strike
                endState(null);
                throw e; // will be visible in consoleErr
            }
        });
    }

    private JScrollPane newJScrollPane(final JSplitPane splitPane, JTextArea console, String title) {
        final JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setBorder(BorderFactory.createTitledBorder(i18n(title)));
        
        // workaround for the Swing repaint bug 
        // when textArea is opaque and has a background color
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

    private void homeAgain(final StateMachine stateMachine, final JButton homeAgain) {
        String message = null;
        if (stateMachine.getState().getClass().equals(OnTheWay.class)) // home too early
            message = i18n("Welcome back, you are in time!");
        else if (stateMachine.getState().getClass().equals(OverdueAlert.class)) // home after first alert mail
            message = i18n("Welcome back, you are late!");
        
        if (message != null) {
            message += "\n"+
                    i18n("Press 'Yes' if that is you,")+"\n"+
                    i18n("or 'No' to continue observing the hike.");
            final int response = JOptionPane.showConfirmDialog(
                    homeAgain,
                    message,
                    "Confirm Termination",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (response == JOptionPane.YES_OPTION) {
                endState(Color.GREEN.darker().darker());
                
                getData().getUserInterface().comingHome();
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
    }
}