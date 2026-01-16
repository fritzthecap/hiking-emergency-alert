package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.Log;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * After activation this page shows StateMachine
 * console output and the "Home Again" button.
 */
public class ObservationPage extends AbstractWizardPage
{
    @Override
    protected AbstractWizardPage nextPage() {
        return null; // makes "Next" button disabled
    }
    
    @Override
    protected void populateUi(Hike hike) {
        buildUi();
    }
    
    /** @return false, don't let go to "Previous" page. */
    @Override
    public boolean hasPreviousPage() {
        return false;
    }
    
    /** @return false, exit only through "Home Again" button. */
    @Override
    public boolean frameCanBeClosed() {
        return false;
    }
    
    private void buildUi() {
        final int SPACE = 16;
        
        final String instructions = i18n("""
This window can be closed only by stopping the hike observation!
Click the 'Home Again' button as soon as you retun from your hike.
Emergency alert mails will be sent starting from""");
        final JTextArea instructionsArea = new JTextArea(instructions+formatHikeEndDate());
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));
        instructionsArea.setEditable(false);
        instructionsArea.setOpaque(false);
        add(instructionsArea, BorderLayout.NORTH);
        
        final JTextArea consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        add(new JScrollPane(consoleArea), BorderLayout.CENTER);
        
        final JButton homeAgain = new JButton(i18n("Home Again"));
        homeAgain.setForeground(Color.RED);
        homeAgain.setToolTipText(
                i18n("Click to stop alert mails from being sent after")+" "+formatHikeEndDate());

        final JPanel buttonPanel = new JPanel(new FlowLayout()); // centers button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));
        buttonPanel.add(homeAgain);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        Log.redirectOutAndErrStreams(consoleArea);
        
        homeAgain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homeAgain.setForeground(Color.GREEN.darker().darker());
                getData().getUserInterface().comingHome();
            }
        });
    }

    private String formatHikeEndDate() {
        final Date plannedHome = getData().getHike().getPlannedHome();
        if (plannedHome != null)
            return DateUtil.toString(plannedHome);
        return "";
    }
}