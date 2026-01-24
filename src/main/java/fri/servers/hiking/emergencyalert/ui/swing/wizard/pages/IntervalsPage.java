package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
    protected String getTitle() {
        return i18n("Send and Receive Intervals");
    }
    
    @Override
    protected void buildUi() {
        alertIntervalMinutesField = SwingUtil.buildNumberField(
                i18n("Alert Send Interval Minutes"), 
                i18n("Minutes to wait for response before sending an alert mail to the next contact"), 
                60);
        
        alertIntervalShrinkingField = SwingUtil.buildNumberField(
                i18n("Alert Interval Shrinking Percent"), 
                i18n("75% on a 60 minutes interval would mean the 2nd interval be just 45 minutes, the 3rd just 34, etc."), 
                100);
        
        useContactDetectionMinutesField = new JCheckBox(i18n("Use Mail Detection Minutes of Contacts"));
        useContactDetectionMinutesField.setToolTipText(
                i18n("For alert intervals, use the estimated mail detection minutes of listed contacts"));
        
        confirmationPollingMinutesField = SwingUtil.buildNumberField(
                i18n("Confirmation Receive Interval Minutes"), 
                i18n("Minutes to wait between attempts to receive a response mail from an alerted contact"), 
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
        
        installFocusListeners();
    }
    
    @Override
    protected void populateUi(Hike hike) {
        alertIntervalMinutesField.setValue(hike.getAlertIntervalMinutes());
        alertIntervalShrinkingField.setValue(floatToPercent(hike.getAlertIntervalShrinking()));
        useContactDetectionMinutesField.setSelected(hike.isUseContactDetectionMinutes());
        confirmationPollingMinutesField.setValue(hike.getConfirmationPollingMinutes());
    }
    
    @Override
    protected String validateFields() {
        if (SwingUtil.getNumberValue(confirmationPollingMinutesField) <= 0)
            return i18n("Confirmation Polling Minute must not be empty!");
        
        if (useContactDetectionMinutesField.isSelected() == false) {
            if (SwingUtil.getNumberValue(alertIntervalMinutesField) <= 0)
                return i18n("Alert Interval Minutes must not be empty!");
            
            if (SwingUtil.getNumberValue(alertIntervalShrinkingField) <= 0)
                return i18n("Alert Interval Shrinking Percent must not be empty!");
        }
        
        return null;
    }

    @Override
    protected boolean commit(boolean goingForward) {
        final Hike hike = getHike();
        
        final int alertIntervalMinutes = SwingUtil.getNumberValue(alertIntervalMinutesField);
        if (alertIntervalMinutes > 0)
            hike.setAlertIntervalMinutes(alertIntervalMinutes);
        
        final int alertIntervalShrinking = SwingUtil.getNumberValue(alertIntervalShrinkingField);
        if (alertIntervalShrinking > 0)
            hike.setAlertIntervalShrinking(percentToFloat(alertIntervalShrinking));
        
        hike.setUseContactDetectionMinutes(useContactDetectionMinutesField.isSelected());
        
        final int confirmationPollingMinutes = SwingUtil.getNumberValue(confirmationPollingMinutesField);
        hike.setConfirmationPollingMinutes(confirmationPollingMinutes);
        
        return true;
    }


    private int floatToPercent(float alertIntervalShrinking) {
        return Math.round(alertIntervalShrinking * 100f);
    }

    private float percentToFloat(int alertIntervalShrinking) {
        return (float) alertIntervalShrinking / 100f;
    }
    
    private void installFocusListeners() {
        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validate();
            }
        };
        confirmationPollingMinutesField.addFocusListener(focusListener);
        useContactDetectionMinutesField.addFocusListener(focusListener);
        alertIntervalMinutesField.addFocusListener(focusListener);
        alertIntervalShrinkingField.addFocusListener(focusListener);
    }
}