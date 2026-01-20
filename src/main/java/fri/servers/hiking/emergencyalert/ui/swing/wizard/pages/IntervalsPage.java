package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Overdue alert send interval, its shrinking fraction,
 * confirmation polling interval, etc.
 */
public class IntervalsPage extends AbstractWizardPage
{
    private JFormattedTextField alertIntervalMinutesField;
    private JFormattedTextField alertIntervalShrinkingField;
    private JCheckBox useContactDetectionMinutesField;
    private JFormattedTextField confirmationPollingMinutes;
    
    @Override
    protected void buildUi() {
        alertIntervalMinutesField = SwingUtil.buildNumberField(
                i18n("Alert Interval Minutes"), 
                i18n("Minutes to wait before sending mail to the next contact"), 
                60);
        alertIntervalShrinkingField = SwingUtil.buildNumberField(
                i18n("Alert Interval Shrinking Percent"), 
                i18n("With 75% and a 60 minutes alert interval, the 2nd interval would would be just 45 minutes, the 3rd 34, etc."), 
                100);
        useContactDetectionMinutesField = new JCheckBox(i18n("Use Contact Detection Minutes"));
        useContactDetectionMinutesField.setToolTipText(
                i18n("For alert intervals, use the estimated minutes the contact needs to detect a mail"));
        confirmationPollingMinutes = SwingUtil.buildNumberField(
                i18n("Confirmation Polling Minutes"), 
                i18n("Minutes to wait between attempts to receive a response mail from some contact"), 
                5);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(alertIntervalMinutesField);
        panel.add(alertIntervalShrinkingField);
        panel.add(useContactDetectionMinutesField);
        panel.add(confirmationPollingMinutes);
        
        useContactDetectionMinutesField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean on = useContactDetectionMinutesField.isSelected();
                alertIntervalMinutesField.setEnabled(on == false);
                alertIntervalShrinkingField.setEnabled(on == false);
            }
        });
        
        getContentPanel().add(panel);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        throw new RuntimeException("Implement me!");
    }
    
    @Override
    protected boolean commit(boolean isWindowClose) {
        getHike().setAlertIntervalMinutes(getAlertIntervalMinutes());
        getHike().setAlertIntervalShrinking(getAlertIntervalShrinking());
        getHike().setUseContactDetectionMinutes(isUseContactDetectionMinutes());
        getHike().setConfirmationPollingMinutes(getConfirmationPollingMinutes());
        return true;
    }


    private Integer getAlertIntervalMinutes() {
        throw new RuntimeException("Implement me!");
    }

    private Float getAlertIntervalShrinking() {
        throw new RuntimeException("Implement me!");
    }

    private Boolean isUseContactDetectionMinutes() {
        throw new RuntimeException("Implement me!");
    }

    private int getConfirmationPollingMinutes() {
        throw new RuntimeException("Implement me!");
    }
}