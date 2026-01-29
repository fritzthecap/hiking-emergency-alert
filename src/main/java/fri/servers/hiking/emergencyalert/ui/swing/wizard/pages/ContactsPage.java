package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.persistence.Alert;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
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
    private JTable alertContactsField;
    
    private JFormattedTextField alertIntervalMinutesField;
    private JFormattedTextField alertIntervalShrinkingField;
    private JCheckBox useContactDetectionMinutesField;
    private JFormattedTextField confirmationPollingMinutesField;
    
    @Override
    protected String getTitle() {
        return i18n("Contacts");
    }
    
    @Override
    protected void buildUi() {
        getContentPanel().add(buildContactsUi(), BorderLayout.CENTER);
        getContentPanel().add(buildIntervalsUi(), BorderLayout.SOUTH);
    }

    @Override
    protected void populateUi(Hike hike) {
        populateContactsUi(hike);
        populateIntervalsUi(hike);
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

    
    private JComponent buildContactsUi() {
        nameOfHikerField = SwingUtil.buildTextField(
                "* "+i18n("Your Name"),
                i18n("Will appear in mail signature"),
                null);
        
        addressOfHikerField = SwingUtil.buildTextField(
                i18n("Your Address"),
                i18n("Would appear in mail signature"),
                null);
        
        phoneNumberOfHikerField = SwingUtil.buildTextField(
                i18n("Your Phone Number"),
                i18n("Available as '$phone' variable in mail texts"),
                null);
        
        final JComponent contactsTable = buildContactsTable();
        contactsTable.setBorder(BorderFactory.createTitledBorder("* "+i18n("Emergency Alert Contacts")));
        
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
        
        installContactsFocusListeners();
        
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
                final Vector<Object> row = new Vector<>(5);
                row.addElement(contact.getMailAddress());
                row.addElement(contact.getFirstName());
                row.addElement(contact.getLastName());
                row.addElement(contact.getDetectionMinutes());
                row.addElement(contact.isAbsent());
                
                data.add(row);
            }
            alertContactsField.setModel(buildTableModel(data));
        }
        
        validate(); // clears error field
        
        if (havingContacts == false)
            alertContactsField.setModel(buildTableModel(createEmptyDataVector(1)));
        else
            addEmptyRowWhenNeeded();
    }
    
    private String validateContactsFields() {
        if (StringUtil.isEmpty(nameOfHikerField.getText()))
            return i18n("Your name must not be empty!");

        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = contactsDataVector();
        int count = 0;
        for (int row = 0; row < dataVector.size(); row++) {
            if (isEmptyRow(dataVector, row) == false) {
                final String mailAddress = (String) dataVector.get(row).get(0);
                if (MailUtil.isMailAddress(mailAddress) == false)
                    return i18n("There is an invalid mail address in contacts!");
                
                final String firstName = (String) dataVector.get(row).get(1);
                final String lastName = (String) dataVector.get(row).get(2);
                if (StringUtil.isEmpty(firstName) && StringUtil.isEmpty(lastName))
                    return i18n("Either first or last name of contact must be given!");
                
                final boolean absent = (Boolean) dataVector.get(row).get(4);
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
        final Vector<Vector> dataVector = contactsDataVector();
        if (alert.getAlertContacts() == null)
            alert.setAlertContacts(new ArrayList<Contact>());
        else 
            alert.getAlertContacts().clear();
        
        for (int row = 0; row < dataVector.size(); row++) {
            if (isEmptyRow(dataVector, row) == false) {
                final String mailAddress = (String) dataVector.get(row).get(0);
                final String firstName = (String) dataVector.get(row).get(1);
                final String lastName = (String) dataVector.get(row).get(2);
                final Object number = dataVector.get(row).get(3);
                final int detectionMinutes = (number != null) ? (Integer) number : -1;
                final boolean absent = (Boolean) dataVector.get(row).get(4);
                
                final Contact contact = new Contact();
                contact.setMailAddress(mailAddress);
                contact.setFirstName(firstName);
                contact.setLastName(lastName);
                if (detectionMinutes > 0)
                    contact.setDetectionMinutes(detectionMinutes);
                contact.setAbsent(absent);
                
                alert.getAlertContacts().add(contact);
            }
        }
        
        return true;
    }
    

    @SuppressWarnings("rawtypes")
    private Vector<Vector> contactsDataVector() {
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        return model.getDataVector();
    }
    
    private JComponent buildContactsTable() {
        alertContactsField = new JTable(buildTableModel(createEmptyDataVector(0))) {
            /** When editing stops, remove empty rows, validate and and an empty row when needed. */
            @Override
            public void editingStopped(ChangeEvent e) {
                super.editingStopped(e);
                
                removeEmptyRowsExceptLast();
                if (ContactsPage.this.validate()) // do not call Container.validate() here!
                    addEmptyRowWhenNeeded();
            }
            
            /** Implement table header tool tips. */
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    private String[] columnToolTips = new String[] {
                            i18n("The contact's mail address"),
                            i18n("The first name of the contact person"),
                            i18n("The last name of the contact person"),
                            i18n("How many minutes the person would need to detect an arrived mail"),
                            i18n("Absent contacts would be ignored when sending alert mails"),
                    };
                    
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };
        
        // commit cell editing on focus-lost
        alertContactsField.putClientProperty("terminateEditOnFocusLost", true); // is null by default
        
        ((DefaultCellEditor) alertContactsField.getDefaultEditor(String.class)).setClickCountToStart(1);
        ((DefaultCellEditor) alertContactsField.getDefaultEditor(Integer.class)).setClickCountToStart(1);
        
        SwingUtilities.invokeLater(() -> { // unreliable! set column widths
            final TableColumnModel columnModel = alertContactsField.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(100);
            columnModel.getColumn(1).setPreferredWidth(40);
            columnModel.getColumn(2).setPreferredWidth(40);
            columnModel.getColumn(3).setPreferredWidth(16);
            columnModel.getColumn(4).setPreferredWidth(10);
        });
        
        alertContactsField.getTableHeader().setReorderingAllowed(false);
        alertContactsField.setRowHeight(24);
        
        final JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(alertContactsField), BorderLayout.CENTER);
        
        SwingUtil.makeComponentFocusable(alertContactsField);
        
        return tablePanel;
    }

    private TableModel buildTableModel(Vector<Vector<Object>> data) {
        final Vector<Object> columnNames = new Vector<>();
        columnNames.add(i18n("Mail Address"));
        columnNames.add(i18n("First Name"));
        columnNames.add(i18n("Last Name"));
        columnNames.add(i18n("Minutes to Detect Mail"));
        columnNames.add(i18n("Absent"));
        
        return new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3)
                    return Integer.class;
                else if (columnIndex == 4)
                    return Boolean.class;
                return String.class;
            }
        };
    }

    private Vector<Vector<Object>> createEmptyDataVector(int numberOfEmptyRows) {
        final Vector<Vector<Object>> data = new Vector<>();
        for (int i = 0; i < numberOfEmptyRows; i++)
            data.add(createEmptyRow());
        return data;
    }

    private Vector<Object> createEmptyRow() {
        final Vector<Object> emptyRow = new Vector<>();
        emptyRow.add("");
        emptyRow.add("");
        emptyRow.add("");
        emptyRow.add(null);
        emptyRow.add(false);
        return emptyRow;
    }

    private void addEmptyRowWhenNeeded() {
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        final int lastRow = model.getRowCount() - 1;
        if (lastRow >= 0)
            if (isEmptyRow(model, lastRow) == false)
                model.addRow(createEmptyRow());
    }

    private void removeEmptyRowsExceptLast() {
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = model.getDataVector();
        
        final int startDecrement = 2;
        for (int row = dataVector.size() - startDecrement; row >= 0; row--)
            if (isEmptyRow(model, row))
                model.removeRow(row);
    }
        
    private boolean isEmptyRow(DefaultTableModel model, int rowIndex) {
        return isEmptyRow(model.getDataVector(), rowIndex);
    }
    
    @SuppressWarnings("rawtypes")
    private boolean isEmptyRow(Vector<Vector> dataVector, int rowIndex) {
        final Vector row = dataVector.get(rowIndex);
        final String mailAddress = (String) row.get(0);
        final String firstName = (String) row.get(1);
        final String lastName = (String) row.get(2);
        return (StringUtil.isEmpty(mailAddress) &&
                StringUtil.isEmpty(firstName) &&
                StringUtil.isEmpty(lastName));
    }
    
    private void installContactsFocusListeners() {
        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validate();
            }
        };
        nameOfHikerField.addFocusListener(focusListener);
        addressOfHikerField.addFocusListener(focusListener);
        phoneNumberOfHikerField.addFocusListener(focusListener);
    }
    
    
    private JComponent buildIntervalsUi() {
        alertIntervalMinutesField = SwingUtil.buildNumberField(
                i18n("Alert Send Interval Minutes"), 
                i18n("Minutes to wait for response before sending an alert mail to the next contact"), 
                60);
        
        alertIntervalShrinkingField = SwingUtil.buildNumberField(
                i18n("Alert Interval Shrinking Percent"), 
                i18n("25% on a 60 minutes interval would mean the 2nd interval be just 45 minutes, the 3rd just 34, etc."), 
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
        panel.add(Box.createRigidArea(new Dimension(1, 16)));
        panel.add(confirmationPollingMinutesField);
        
        useContactDetectionMinutesField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean on = useContactDetectionMinutesField.isSelected();
                alertIntervalMinutesField.setEnabled(on == false);
                alertIntervalShrinkingField.setEnabled(on == false);
            }
        });
        
        final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(panel);
        
        installIntervalsFocusListeners();
        
        return centerPanel;
    }
    
    private void populateIntervalsUi(Hike hike) {
        alertIntervalMinutesField.setValue(hike.getAlert().getAlertIntervalMinutes());
        alertIntervalShrinkingField.setValue(floatToPercent(hike.getAlert().getAlertIntervalShrinking()));
        useContactDetectionMinutesField.setSelected(hike.getAlert().isUseContactDetectionMinutes());
        confirmationPollingMinutesField.setValue(hike.getAlert().getConfirmationPollingMinutes());
    }
    
    private String validateIntervalsFields() {
        final int alertInterval = SwingUtil.getNumberValue(alertIntervalMinutesField);
        final int pollingInterval = SwingUtil.getNumberValue(confirmationPollingMinutesField);
        
        if (pollingInterval <= 0)
            return i18n("Confirmation Polling Minutes must be greater zero!");
        
        if (alertInterval <= pollingInterval)
            return i18n("Alert Interval must be greater than Confirmation Polling Interval!");
        
        if (useContactDetectionMinutesField.isSelected() == false) {
            if (alertInterval <= 0)
                return i18n("Alert Interval Minutes must not be empty!");
            
            final int alertIntervalShrinking = SwingUtil.getNumberValue(alertIntervalShrinkingField);
            if (alertIntervalShrinking < MINIMUM_INTERVAL_SHRINKING_PERCENT)
                return i18n("Alert Interval Shrinking Percent must be greater equal")+" "+MINIMUM_INTERVAL_SHRINKING_PERCENT;
            else if (alertIntervalShrinking > MAXIMUM_INTERVAL_SHRINKING_PERCENT)
                return i18n("Alert Interval Shrinking Percent must be smaller equal")+" "+MAXIMUM_INTERVAL_SHRINKING_PERCENT;
        }
        
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
    
    private void installIntervalsFocusListeners() {
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