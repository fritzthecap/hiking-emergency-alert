package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
    private JTextField nameOfHikerField;
    private JTextField addressOfHikerField;
    private JTable alertContactsField;
    
    @Override
    protected void buildUi() {
        nameOfHikerField = SwingUtil.buildTextField(
                i18n("Your Name"),
                i18n("Required, will be in mail signature"),
                null);
        nameOfHikerField.setColumns(20);
        
        addressOfHikerField = SwingUtil.buildTextField(
                i18n("Your Address"),
                i18n("Would also be in mail signature"),
                null);
        addressOfHikerField.setColumns(20);
        
        alertContactsField = buildContactsTable();
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(nameOfHikerField);
        panel.add(addressOfHikerField);
        panel.add(new JScrollPane(alertContactsField));
        
        getContentPanel().add(panel);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = hike.getAlert();

        if (StringUtil.isNotEmpty(alert.getNameOfHiker()))
            nameOfHikerField.setText(alert.getNameOfHiker());

        if (StringUtil.isNotEmpty(alert.getAddressOfHiker()))
            nameOfHikerField.setText(alert.getAddressOfHiker());
        
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
    
    @Override
    protected boolean commit(boolean isWindowClose) {
        final Alert alert = getHike().getAlert();

        if (StringUtil.isNotEmpty(nameOfHikerField.getText()))
            alert.setNameOfHiker(nameOfHikerField.getText());
        
        if (StringUtil.isNotEmpty(addressOfHikerField.getText()))
            alert.setAddressOfHiker(addressOfHikerField.getText());
        
        // TODO: table
        
        return true;
    }
    
    
    private JTable buildContactsTable() {
        final Vector<Vector<Object>> data = new Vector<>();
        final JTable table = new JTable(buildTableModel(data));
        table.setBorder(BorderFactory.createTitledBorder(i18n("Contacts")));
        return table;
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