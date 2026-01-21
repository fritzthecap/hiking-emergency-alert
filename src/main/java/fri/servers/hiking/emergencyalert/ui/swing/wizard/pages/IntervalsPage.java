package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
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
    private JFormattedTextField confirmationPollingMinutesField;
    
    @Override
    protected void buildUi() {
        alertIntervalMinutesField = SwingUtil.buildNumberField(
                i18n("Alert Interval Minutes"), 
                i18n("Minutes to wait for response before sending an alert mail to the next contact"), 
                60);
        alertIntervalShrinkingField = SwingUtil.buildNumberField(
                i18n("Alert Interval Shrinking Percent"), 
                i18n("With 75% and a 60 minutes alert interval, the 2nd interval would would be just 45 minutes, the 3rd 34, etc."), 
                100);
        useContactDetectionMinutesField = new JCheckBox(i18n("Use Contact Detection Minutes"));
        useContactDetectionMinutesField.setToolTipText(
                i18n("For alert intervals, use the estimated minutes the contact needs to detect a mail"));
        
        confirmationPollingMinutesField = SwingUtil.buildNumberField(
                i18n("Confirmation Polling Minutes"), 
                i18n("Minutes to wait between attempts to receive a response mail from some contact"), 
                2);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(alertIntervalMinutesField);
        panel.add(alertIntervalShrinkingField);
        panel.add(useContactDetectionMinutesField);
        panel.add(Box.createRigidArea(new Dimension(1, 30)));
        panel.add(confirmationPollingMinutesField);
        
        useContactDetectionMinutesField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean on = useContactDetectionMinutesField.isSelected();
                alertIntervalMinutesField.setEnabled(on == false);
                alertIntervalShrinkingField.setEnabled(on == false);
            }
        });
        
        final JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.add(panel);
        getContentPanel().add(contentPanel);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        alertIntervalMinutesField.setValue(hike.getAlertIntervalMinutes());
        alertIntervalShrinkingField.setValue(floatToPercent(hike.getAlertIntervalShrinking()));
        useContactDetectionMinutesField.setSelected(hike.isUseContactDetectionMinutes());
        confirmationPollingMinutesField.setValue(hike.getConfirmationPollingMinutes());
    }
    
    @Override
    protected boolean commit(boolean isWindowClose) {
        final Hike hike = getHike();
        hike.setAlertIntervalMinutes((int) alertIntervalMinutesField.getValue());
        hike.setAlertIntervalShrinking(percentToFloat((int) alertIntervalShrinkingField.getValue()));
        hike.setUseContactDetectionMinutes(useContactDetectionMinutesField.isSelected());
        hike.setConfirmationPollingMinutes((int) confirmationPollingMinutesField.getValue());
        return true;
    }


    private int floatToPercent(float alertIntervalShrinking) {
        return Math.round(alertIntervalShrinking * 100f);
    }

    private float percentToFloat(int alertIntervalShrinking) {
        return (float) alertIntervalShrinking / 100f;
    }
}