package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import fri.servers.hiking.emergencyalert.persistence.entities.Alert;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.time.AlertIntervalModel;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.components.ContactsPanel;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.components.ContactsTable;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Hiker name, address and phone, and a list of mail contacts.
 */
public class ContactsPage extends AbstractWizardPage
{
    private static final int MAXIMUM_INTERVAL_SHRINKING_PERCENT = 75;
    private static final int MINIMUM_INTERVAL_SHRINKING_PERCENT = 0;
    
    private JTextField nameOfHikerField;
    private JTextField addressOfHikerField;
    private JTextField phoneNumberOfHikerField;
    private ContactsTable contactsTable;
    
    private JFormattedTextField alertIntervalMinutesField;
    private JFormattedTextField alertIntervalShrinkingField;
    private JCheckBox useContactDetectionMinutesField;
    private JFormattedTextField confirmationPollingMinutesField;
    
    private ActionListener useContactDetectionMinutesActionListener;
    
    @Override
    protected String getTitle() {
        return i18n("Contacts");
    }
    
    @Override
    protected void buildUi() {
        getContentPanel().add(buildContactsUi(), BorderLayout.CENTER);
        getContentPanel().add(buildIntervalsUi(), BorderLayout.SOUTH);
        installFocusValidation();
    }

    @Override
    protected void populateUi(Hike hike) {
        populateContactsUi(hike);
        populateIntervalsUi(hike);
        // initialize state
        useContactDetectionMinutesActionListener.actionPerformed(null);
    }

    @Override
    protected String validateFields() {
        final String error = validateContactsFields();
        if (error != null)
            return error;
        
        return validateIntervalsFields();
    }

    @Override
    protected boolean commit(boolean goingForward) {
        return commitContacts() && commitIntervals();
    }
    

    // user and contacts
    
    private JComponent buildContactsUi() {
        nameOfHikerField = SwingUtil.buildTextField(
                "* "+i18n("Your Name"),
                i18n("Will appear in mail signature"),
                null);
        
        phoneNumberOfHikerField = SwingUtil.buildTextField(
                i18n("Your Phone Number"),
                i18n("Available as '$phone' variable in mail texts"),
                null);
        
        addressOfHikerField = SwingUtil.buildTextField(
                i18n("Your Address"),
                i18n("Would appear in mail signature"),
                null);
        
        final JComponent contactsTable = buildContactsTable();
        
        final String contactTip = i18n("Use your own e-mail as first contact for the case you are Ok but running late!");
        final TitledBorder insideBorder = BorderFactory.createTitledBorder(contactTip);
        insideBorder.setTitleColor(Color.GRAY);
        contactsTable.setToolTipText(contactTip);
        contactsTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("* "+i18n("Emergency Alert Contacts")), // outside
                insideBorder)
            );
        
        final JPanel hikerPanel = new JPanel();
        hikerPanel.setLayout(new BoxLayout(hikerPanel, BoxLayout.X_AXIS));
        hikerPanel.add(nameOfHikerField);
        hikerPanel.add(phoneNumberOfHikerField);
        hikerPanel.add(addressOfHikerField);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(hikerPanel);
        panel.add(Box.createRigidArea(new Dimension(1, 16)));
        panel.add(contactsTable);
        
        return panel;
    }
    
    private void populateContactsUi(Hike hike) {
        final Alert alert = hike.getAlert();

        if (StringUtil.isNotEmpty(alert.getNameOfHiker()))
            nameOfHikerField.setText(alert.getNameOfHiker());

        if (StringUtil.isNotEmpty(alert.getAddressOfHiker()))
            addressOfHikerField.setText(alert.getAddressOfHiker());
        
        if (StringUtil.isNotEmpty(alert.getPhoneNumberOfHiker()))
            phoneNumberOfHikerField.setText(alert.getPhoneNumberOfHiker());
        
        final boolean havingContacts = (alert.getAlertContacts() != null && alert.getAlertContacts().size() > 0);
        if (havingContacts) {
            final Vector<Vector<Object>> data = new Vector<>();
            
            for (Contact contact : alert.getAlertContacts()) {
                final Vector<Object> row = new Vector<>(6);
                row.addElement(contact.getMailAddress());
                row.addElement(contact.getFirstName());
                row.addElement(contact.getLastName());
                row.addElement(contact.isNeedsProcedure());
                row.addElement(contact.getDetectionMinutes());
                row.addElement(contact.isAbsent());
                
                data.add(row);
            }
            contactsTable.setData(data);
        }
        
        validate(); // clears error field
        
        contactsTable.addEmptyRowWhenNeeded();
    }
    
    private String validateContactsFields() {
        if (StringUtil.isEmpty(nameOfHikerField.getText()))
            return i18n("Your name must not be empty!");

        final String contactsError = contactsTable.validateContactsFields();
        if (contactsError != null)
            return contactsError;
        
        final int pollingInterval = SwingUtil.getNumberValue(confirmationPollingMinutesField);
        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = contactsTable.getData();
        int count = 0;
        for (int row = 0; row < dataVector.size(); row++) {
            if (contactsTable.isEmptyRow(dataVector, row) == false) {
                if (useContactDetectionMinutesField.isSelected()) {
                    final Object number = dataVector.get(row).get(ContactsTable.MAIL_DETECTION_MINUTES_COLUMN);
                    final int detectionMinutes = (number != null) ? (Integer) number : Alert.DEFAULT_ALERT_INTERVAL_MINUTES;
                    if (detectionMinutes <= pollingInterval)
                        return i18n("Contact Mail Detection minutes must be greater than Confirmation Polling Interval!");
                }

                final boolean absent = (Boolean) dataVector.get(row).get(ContactsTable.ABSENT_COLUMN);
                if (absent == false)
                    count++;
            }
        }
        
        if (count <= 0)
            return i18n("You need at least one non-absent mail contact in list!");
        
        return null;
    }

    private boolean commitContacts() {
        final Alert alert = getHike().getAlert();

        if (StringUtil.isNotEmpty(nameOfHikerField.getText()))
            alert.setNameOfHiker(nameOfHikerField.getText());
        
        if (StringUtil.isNotEmpty(addressOfHikerField.getText()))
            alert.setAddressOfHiker(addressOfHikerField.getText());
        
        if (StringUtil.isNotEmpty(phoneNumberOfHikerField.getText()))
            alert.setPhoneNumberOfHiker(phoneNumberOfHikerField.getText());
        
        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = contactsTable.getData();
        if (alert.getAlertContacts() == null)
            alert.setAlertContacts(new ArrayList<Contact>());
        else 
            alert.getAlertContacts().clear();
        
        for (int row = 0; row < dataVector.size(); row++) {
            if (contactsTable.isEmptyRow(dataVector, row) == false) {
                final String mailAddress = (String) dataVector.get(row).get(ContactsTable.E_MAIL_COLUMN);
                final String firstName = (String) dataVector.get(row).get(ContactsTable.FIRST_NAME_COLUMN);
                final String lastName = (String) dataVector.get(row).get(ContactsTable.LAST_NAME_COLUMN);
                final boolean needsProcedure = (Boolean) dataVector.get(row).get(ContactsTable.NEEDS_PROCEDURE_COLUMN);
                final Object number = dataVector.get(row).get(ContactsTable.MAIL_DETECTION_MINUTES_COLUMN);
                final int detectionMinutes = (number != null) ? (Integer) number : -1;
                final boolean absent = (Boolean) dataVector.get(row).get(ContactsTable.ABSENT_COLUMN);
                
                final Contact contact = new Contact();
                contact.setMailAddress(mailAddress);
                contact.setFirstName(firstName);
                contact.setLastName(lastName);
                contact.setNeedsProcedure(needsProcedure);
                if (detectionMinutes > 0)
                    contact.setDetectionMinutes(detectionMinutes);
                contact.setAbsent(absent);
                
                alert.getAlertContacts().add(contact);
            }
        }
        
        return true;
    }

    private JComponent buildContactsTable() {
        contactsTable = new ContactsTable() {
            /** When editing stops, remove empty rows, validate and add an empty row when needed. */
            @Override
            public void editingStopped(ChangeEvent e) {
                super.editingStopped(e);
                if (ContactsPage.this.validate()) // no error
                    contactsTable.addEmptyRowWhenNeeded(); // make sure to prepare an empty last row
            }
        };
        
        // set mail-detection-minutes column disabled when useContactDetectionMinutesField is off
        final TableCellRenderer originalHeaderRenderer = contactsTable.getTableHeader().getDefaultRenderer();
        contactsTable.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = originalHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setEnabled((table.convertColumnIndexToModel(column) == ContactsTable.MAIL_DETECTION_MINUTES_COLUMN) 
                        ? useContactDetectionMinutesField.isSelected()
                        : true);
                return c;
            }
        });
        
        return new ContactsPanel(contactsTable);
    }
    
    
    // intervals
    
    private JComponent buildIntervalsUi() {
        alertIntervalMinutesField = SwingUtil.buildNumberField(
                i18n("Alert Send Interval Minutes"), 
                i18n("Minutes to wait for response before sending an alert mail to the next contact"), 
                60);
        
        alertIntervalShrinkingField = SwingUtil.buildNumberField(
                i18n("Alert Interval Shrinking Percent"), 
                i18n("25% on a 60 minutes interval would mean the 2nd interval be just 45 minutes, the 3rd just 34, etc."), 
                0);
        
        useContactDetectionMinutesField = new JCheckBox(i18n("Use Mail Detection Minutes of Contacts"), false);
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
        panel.add(Box.createRigidArea(new Dimension(1, 16)));
        panel.add(confirmationPollingMinutesField);
        
        useContactDetectionMinutesActionListener = (new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean on = useContactDetectionMinutesField.isSelected();
                alertIntervalMinutesField.setEnabled(on == false);
                alertIntervalShrinkingField.setEnabled(on == false);
                
                ((JComponent) contactsTable.getDefaultRenderer(Integer.class)).setEnabled(on == true);
                contactsTable.repaint();
                contactsTable.getTableHeader().repaint();
            }
        });
        useContactDetectionMinutesField.addActionListener(useContactDetectionMinutesActionListener);
        
        final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(panel);
        
        return centerPanel;
    }
    
    private void populateIntervalsUi(Hike hike) {
        alertIntervalMinutesField.setValue(hike.getAlert().getAlertIntervalMinutes());
        alertIntervalShrinkingField.setValue(floatToPercent(hike.getAlert().getAlertIntervalShrinking()));
        if (hike.getAlert().isUseContactDetectionMinutes()) // depends on JCheckBox initially being false
            useContactDetectionMinutesField.doClick(); // setSelected() would NOT trigger ActionListener
        confirmationPollingMinutesField.setValue(hike.getAlert().getConfirmationPollingMinutes());
    }
    
    private String validateIntervalsFields() {
        final int pollingInterval = SwingUtil.getNumberValue(confirmationPollingMinutesField);
        
        if (useContactDetectionMinutesField.isSelected() == false) {
            final int alertInterval = SwingUtil.getNumberValue(alertIntervalMinutesField);
            if (alertInterval <= 0)
                return i18n("Alert Interval Minutes must not be empty!");
            
            if (alertInterval <= pollingInterval)
                return i18n("Alert Interval must be greater than Confirmation Polling Interval!");
            
            final int alertIntervalShrinking = SwingUtil.getNumberValue(alertIntervalShrinkingField);
            if (alertIntervalShrinking < MINIMUM_INTERVAL_SHRINKING_PERCENT)
                return i18n("Alert Interval Shrinking Percent must be greater equal")+" "+MINIMUM_INTERVAL_SHRINKING_PERCENT;
            else if (alertIntervalShrinking > MAXIMUM_INTERVAL_SHRINKING_PERCENT)
                return i18n("Alert Interval Shrinking Percent must be smaller equal")+" "+MAXIMUM_INTERVAL_SHRINKING_PERCENT;
            
            if (alertIntervalShrinking > 0) {
                final AlertIntervalModel intervalModel = new AlertIntervalModel(alertInterval, alertIntervalShrinking);
                @SuppressWarnings("rawtypes")
                final Vector<Vector> dataVector = contactsTable.getData();
                
                for (int row = 0; row < dataVector.size(); row++) // loop non-absent contacts
                    if (contactsTable.isEmptyRow(dataVector, row) == false && 
                            Boolean.FALSE.equals(dataVector.get(row).get(ContactsTable.ABSENT_COLUMN)))
                        if (intervalModel.nextIntervalMinutes() <= pollingInterval)
                            return i18n("Alert Interval with Shrinking Percent would become shorter than Confirmation Polling Interval!");
            }
        }
        // else: see contact fields validation
        
        if (pollingInterval <= 0)
            return i18n("Confirmation Polling Minutes must be greater zero!");
        
        return null;
    }

    private boolean commitIntervals() {
        final Hike hike = getHike();
        
        final int alertIntervalMinutes = SwingUtil.getNumberValue(alertIntervalMinutesField);
        if (alertIntervalMinutes > 0)
            hike.getAlert().setAlertIntervalMinutes(alertIntervalMinutes);
        
        final int alertIntervalShrinking = SwingUtil.getNumberValue(alertIntervalShrinkingField);
        if (alertIntervalShrinking >= MINIMUM_INTERVAL_SHRINKING_PERCENT && alertIntervalShrinking <= MAXIMUM_INTERVAL_SHRINKING_PERCENT)
            hike.getAlert().setAlertIntervalShrinking(percentToFloat(alertIntervalShrinking));
        
        hike.getAlert().setUseContactDetectionMinutes(useContactDetectionMinutesField.isSelected());
        
        final int confirmationPollingMinutes = SwingUtil.getNumberValue(confirmationPollingMinutesField);
        hike.getAlert().setConfirmationPollingMinutes(confirmationPollingMinutes);
        
        return true;
    }


    private int floatToPercent(float alertIntervalShrinking) {
        return Math.round(alertIntervalShrinking * 100f);
    }

    private float percentToFloat(int alertIntervalShrinking) {
        return (float) alertIntervalShrinking / 100f;
    }
    
    
    private void installFocusValidation() {
        final JComponent[] focusComponents = new JComponent[] {
                confirmationPollingMinutesField,
                useContactDetectionMinutesField,
                alertIntervalMinutesField,
                alertIntervalShrinkingField,
                nameOfHikerField,
                addressOfHikerField,
                phoneNumberOfHikerField,
        };
        installFocusListener(focusComponents, null);
    }
}