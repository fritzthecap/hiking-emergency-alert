package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.persistence.Alert;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Hiker name and address, and a list of mail contacts.
 */
public class ContactsPage extends AbstractWizardPage
{
    private JLabel errorField;
    private JTextField nameOfHikerField;
    private JTextField addressOfHikerField;
    private JTextField phoneNumberOfHikerField;
    private JTable alertContactsField;
    
    @Override
    protected void buildUi() {
        errorField = new JLabel(i18n("Your Name is missing!"));
        errorField.setForeground(Color.RED);
        
        nameOfHikerField = SwingUtil.buildTextField(
                i18n("Your Name"),
                i18n("Required, will appear in mail signature"),
                null);
        nameOfHikerField.setColumns(20);
        
        addressOfHikerField = SwingUtil.buildTextField(
                i18n("Your Address"),
                i18n("Would also appear in mail signature"),
                null);
        addressOfHikerField.setColumns(20);
        
        phoneNumberOfHikerField = SwingUtil.buildTextField(
                i18n("Your Phone Number"),
                i18n("Will be available as '$phone' macro in mail texts"),
                null);
        
        final JComponent contactsTable = buildContactsTable();
        contactsTable.setBorder(BorderFactory.createTitledBorder(i18n("Contacts")));
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(errorField);
        panel.add(nameOfHikerField);
        panel.add(addressOfHikerField);
        panel.add(phoneNumberOfHikerField);
        panel.add(contactsTable);
        
        getContentPanel().add(panel);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = hike.getAlert();

        if (StringUtil.isNotEmpty(alert.getNameOfHiker()))
            nameOfHikerField.setText(alert.getNameOfHiker());

        if (StringUtil.isNotEmpty(alert.getAddressOfHiker()))
            nameOfHikerField.setText(alert.getAddressOfHiker());
        
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
        
        validate(); // would remove any empty row
        
        if (havingContacts == false)
            alertContactsField.setModel(buildTableModel(createEmptyDataVector(1)));
        else
            addEmptyRowWhenNeeded();
    }
    
    @Override
    @SuppressWarnings({ "rawtypes" })
    protected boolean commit(boolean goingForward) {
        if (goingForward && validate() == false)
            return false;
        
        final Alert alert = getHike().getAlert();

        if (StringUtil.isNotEmpty(nameOfHikerField.getText()))
            alert.setNameOfHiker(nameOfHikerField.getText());
        
        if (StringUtil.isNotEmpty(addressOfHikerField.getText()))
            alert.setAddressOfHiker(addressOfHikerField.getText());
        
        if (StringUtil.isNotEmpty(phoneNumberOfHikerField.getText()))
            alert.setPhoneNumberOfHiker(phoneNumberOfHikerField.getText());
        
        final Vector<Vector> dataVector = commitAndGetContacts();
        if (alert.getAlertContacts() == null)
            alert.setAlertContacts(new ArrayList<Contact>());
        else 
            alert.getAlertContacts().clear();
        
        for (Vector dataRow : dataVector) {
            final String mailAddress = (String) dataRow.get(0);
            if (StringUtil.isNotEmpty(mailAddress)) {
                final String firstName = (String) dataRow.get(1);
                final String lastName = (String) dataRow.get(2);
                Object column3 = dataRow.get(3);
                final int detectionMinutes = (column3 != null ? (Integer) column3 : -1);
                final boolean absent = (Boolean) dataRow.get(4);
                
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

    
    private boolean validate() {
        String error;
        if ((error = validate(commitAndGetContacts())) != null) {
            errorField.setText(error);
            return false;
        }
        errorField.setText("");
        return true;
    }
    
    @SuppressWarnings("rawtypes")
    private Vector<Vector> commitAndGetContacts() {
        final DefaultCellEditor cellEditor = (DefaultCellEditor) alertContactsField.getCellEditor();
        if (cellEditor != null)
            cellEditor.stopCellEditing();
        else
            alertContactsField.editingStopped(null);
        
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        return model.getDataVector();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String validate(Vector<Vector> contacts) {
        if (StringUtil.isEmpty(nameOfHikerField.getText()))
            return i18n("Your name must not be empty!");

        int count = 0;
        for (Vector<Object> contact : contacts) {
            final String mailAddress = (String) contact.get(0);
            if (StringUtil.isNotEmpty(mailAddress))
                count++;
        }
        
        if (count <= 0)
            return i18n("You need at least one mail contact in list!");
        
        for (Vector<Object> contact : contacts) {
            final String mailAddress = (String) contact.get(0);
            if (StringUtil.isNotEmpty(mailAddress)) {
                if (MailUtil.isMailAddress(mailAddress) == false)
                    return i18n("There is an invalid mail address in contacts!");
                
                final String firstName = (String) contact.get(1);
                final String lastName = (String) contact.get(2);
                if (StringUtil.isEmpty(firstName) && StringUtil.isEmpty(lastName))
                    return i18n("Either first or last name of contact must be given!");
            }
        }
        
        return null;
    }

    private JComponent buildContactsTable() {
        alertContactsField = new JTable(buildTableModel(createEmptyDataVector(0))) {
            @Override
            public void editingStopped(ChangeEvent e) {
                super.editingStopped(e);
                removeEmptyRows(true);
                addEmptyRowWhenNeeded();
            }
        };
        ((DefaultCellEditor) alertContactsField.getDefaultEditor(String.class)).setClickCountToStart(1);
        ((DefaultCellEditor) alertContactsField.getDefaultEditor(Integer.class)).setClickCountToStart(1);
        alertContactsField.getTableHeader().setReorderingAllowed(false);
        alertContactsField.setToolTipText(i18n("To delete a row, erase the Mail Address field"));
        
        final JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(alertContactsField), BorderLayout.CENTER);
        
        // TODO: Add and Remove buttons for table
        
        return tablePanel;
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
        emptyRow.add(60);
        emptyRow.add(false);
        return emptyRow;
    }

    private void addEmptyRowWhenNeeded() {
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        final int lastRow = model.getRowCount() - 1;
        if (lastRow >= 0) {
            final String mailAddress = (String) model.getValueAt(lastRow, 0);
            if (StringUtil.isNotEmpty(mailAddress))
                model.addRow(createEmptyRow());
        }
    }

    private void removeEmptyRows(boolean whenNotLast) {
        final DefaultTableModel model = (DefaultTableModel) alertContactsField.getModel();
        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = model.getDataVector();
        
        final int startDecrement = (whenNotLast ? 2 : 1);
        for (int i = dataVector.size() - startDecrement; i >= 0; i--) {
            final String mailAddress = (String) model.getValueAt(i, 0);
            if (StringUtil.isEmpty(mailAddress))
                model.removeRow(i);
        }
    }

    private TableModel buildTableModel(Vector<Vector<Object>> data) {
        final Vector<Object> columnNames = new Vector<>();
        columnNames.add(i18n("Mail Address"));
        columnNames.add(i18n("First Name"));
        columnNames.add(i18n("Last Name"));
        columnNames.add(i18n("Mail Detection Minutes"));
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
}