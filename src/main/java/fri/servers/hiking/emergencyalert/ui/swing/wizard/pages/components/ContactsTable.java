package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.components;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.persistence.entities.Alert;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/** The email-table on contacts page. */
public class ContactsTable extends JTable
{
    private final String[] columnToolTips = new String[] {
            i18n("The contact's mail address"),
            i18n("The first name of the contact person"),
            i18n("The last name of the contact person"),
            i18n("How many minutes the person would need to detect an arrived mail"),
            i18n("Absent contacts would be ignored when sending alert mails"),
        };
    
    private final Vector<Object> columnNames = new Vector<>();
    
    {   // instance initializer
        columnNames.add(i18n("Mail Address"));
        columnNames.add(i18n("First Name"));
        columnNames.add(i18n("Last Name"));
        columnNames.add(i18n("Minutes to Detect Mail"));
        columnNames.add(i18n("Absent"));
    }

    private final List<Class<?>> columnClasses = List.of(
            String.class,
            String.class,
            String.class,
            Integer.class,
            Boolean.class);

    public ContactsTable() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // commit cell editing on focus-lost
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // is null by default
        
        getTableHeader().setReorderingAllowed(false); // no column dragging
        setRowHeight(24); // bigger rows
        
        ((DefaultCellEditor) getDefaultEditor(String.class)).setClickCountToStart(1);
        ((DefaultCellEditor) getDefaultEditor(Integer.class)).setClickCountToStart(1);
        
        SwingUtilities.invokeLater(() -> {
            final TableColumnModel columnModel = getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(100);
            columnModel.getColumn(1).setPreferredWidth(40);
            columnModel.getColumn(2).setPreferredWidth(40);
            columnModel.getColumn(3).setPreferredWidth(16);
            columnModel.getColumn(4).setPreferredWidth(10);
        });
        
        buildTableModel(createEmptyDataVector(0));
    }
    
    
    @SuppressWarnings("rawtypes")
    public Vector<Vector> getData() {
        final DefaultTableModel model = (DefaultTableModel) getModel();
        return model.getDataVector();
    }
    
    public void setData(Vector<Vector<Object>> data) {
        setModel(buildTableModel(data));
    }
    
    
    /** To be called on WizardPage.validate(). */
    public String validateContactsFields() {
        @SuppressWarnings("rawtypes")
        final Vector<Vector> dataVector = ((DefaultTableModel) getModel()).getDataVector();
        for (int row = 0; row < dataVector.size(); row++) {
            if (isEmptyRow(dataVector, row) == false) {
                final String mailAddress = (String) dataVector.get(row).get(0);
                if (MailUtil.isMailAddress(mailAddress) == false)
                    return i18n("There is an invalid mail address in contacts!");
                
                final String firstName = (String) dataVector.get(row).get(1);
                final String lastName = (String) dataVector.get(row).get(2);
                if (StringUtil.isEmpty(firstName) && StringUtil.isEmpty(lastName))
                    return i18n("Either first or last name of contact must be given!");
            }
        }
        return null;
    }

    public void addEmptyRowWhenNeeded() {
        final DefaultTableModel model = (DefaultTableModel) getModel();
        final int lastRow = model.getRowCount() - 1;
        if (lastRow < 0 || isEmptyRow(model, lastRow) == false)
            model.addRow(createEmptyRow());
    }

    @SuppressWarnings("rawtypes")
    public boolean isEmptyRow(Vector<Vector> dataVector, int rowIndex) {
        final Vector row = dataVector.get(rowIndex);
        final String mailAddress = (String) row.get(0);
        final String firstName = (String) row.get(1);
        final String lastName = (String) row.get(2);
        return (StringUtil.isEmpty(mailAddress) &&
                StringUtil.isEmpty(firstName) &&
                StringUtil.isEmpty(lastName));
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        return toolTipText(event);
    }
    
    
    /** Overridden to add tooltips to table header. */
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            @Override
            public String getToolTipText(MouseEvent event) {
                return toolTipText(event);
            }
        };
    }

    
    private String toolTipText(MouseEvent event) {
        if (event != null)  {
            final int column = columnAtPoint(event.getPoint());
            
            if (column >= 0 & columnToolTips.length > column)
                return columnToolTips[convertColumnIndexToModel(column)];
        }
        return null;
    }
    
    private TableModel buildTableModel(Vector<Vector<Object>> data) {
        return new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClasses.get(columnIndex);
            }
        };
    }
    
    private boolean isEmptyRow(DefaultTableModel model, int rowIndex) {
        return isEmptyRow(model.getDataVector(), rowIndex);
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
        emptyRow.add(Integer.valueOf(Alert.DEFAULT_ALERT_INTERVAL_MINUTES));
        emptyRow.add(Boolean.FALSE);
        return emptyRow;
    }
}