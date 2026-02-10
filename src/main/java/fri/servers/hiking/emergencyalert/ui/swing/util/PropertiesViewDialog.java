package fri.servers.hiking.emergencyalert.ui.swing.util;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Properties Dialog, that lets view and copy names and values
 * of a properties map in a JTable.
 */
public class PropertiesViewDialog extends JDialog
{
    protected final Frame parent;
    protected Properties properties;
    protected Vector<Vector<Object>> namesAndValues;
    protected JTable table;
    protected TableModel model;

    public PropertiesViewDialog(Frame parent, Properties properties, String title) {
        super(parent, title, true);
        this.parent = parent;
        this.properties = properties;
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible && table == null) {
            buildUi();
            pack();
            setLocationRelativeTo(parent);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent ev) {
                    close();
                }
            });
        }
        super.setVisible(visible);
    }

    protected Container buildUi() {
        final JScrollPane tableScrollPane = buildTableScrollPane();
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tableScrollPane, BorderLayout.CENTER);
        return contentPane;
    }

    protected void close() {
        dispose();
    }

    protected void setUneditableButCopyableCellEditor() {
        setTableUneditableButCopyable(table);
    }

    private void setTableUneditableButCopyable(JTable table) {
        final DefaultCellEditor cellEditor = (DefaultCellEditor) table.getDefaultEditor(String.class);
        final JTextField editor = (JTextField) cellEditor.getComponent();
        editor.setEditable(false);
    }

    /** Creates a new table row. */
    protected final Vector<Object> newRow() {
        return new Vector<>(3);
    }
    
    /** Override to add further column headers. */
    protected void addMoreColumns(Vector<Object> columnNames) {
    }

    /** Convenience method that builds another table from given properties. */
    protected final JScrollPane buildReadOnlyTable(Properties readOnlyProperties) {
        final Vector<Vector<Object>> namesAndValues = buildNamesAndValues(readOnlyProperties);

        final Vector<Object> columnNames = buildColumns();
        
        final TableModel model = new DefaultTableModel(namesAndValues, columnNames);
        final JTable table = new JTable(model);
        table.setEnabled(false);

        setTableHeight(table, model);

        return new JScrollPane(table);
    }

    /** Override to add further cell values. */
    protected void addMoreTableCells(Vector<Object> newRow) {
    }

    /** Override to return class for further cell values. */
    protected Class<?> getColumnClassForIndex(int columnIndex) {
        return String.class;
    }

    /** Displayed property names go through here. Override for custom sort oder. */
    protected List<String> sort(List<String> propertyNames) {
        Collections.sort(propertyNames);
        return propertyNames;
    }

    
    private JScrollPane buildTableScrollPane() {
        this.namesAndValues = buildNamesAndValues(properties);

        final Vector<Object> columnNames = buildColumns();
        addMoreColumns(columnNames);
        
        model = new DefaultTableModel(namesAndValues, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return getColumnClassForIndex(columnIndex);
            }
        };
        table = new JTable(model);

        setUneditableButCopyableCellEditor(); // allow copy, deny editing

        setTableHeight(table, model);

        return new JScrollPane(table);
    }

    private void setTableHeight(JTable table, TableModel model) {
        final int MAX_ROW_COUNT = 12;
        final int rowCount = Math.max(model.getRowCount(), MAX_ROW_COUNT);
        final int height = rowCount * table.getRowHeight();
        table.setPreferredScrollableViewportSize(new Dimension(500, height));
    }

    private Vector<Object> buildColumns() {
        final Vector<Object> columnNames = newRow();
        columnNames.addElement(i18n("Name"));
        columnNames.addElement(i18n("Value"));
        return columnNames;
    }
    
    private Vector<Vector<Object>> buildNamesAndValues(Properties properties) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final List<String> names = sort(new Vector(properties.keySet()));
        
        final Vector<Vector<Object>> nameAndValues = new Vector<>(properties.size() > 0 ? properties.size() : 1);
        for (Object name : names) {
            final Vector<Object> newRow = newRow();
            newRow.add(name);
            newRow.addElement(properties.getProperty((String) name));
            addMoreTableCells(newRow);
            
            nameAndValues.addElement(newRow);
        }
        return nameAndValues;
    }
    
    
    /** Test main. */
    public static void main(String[] args) {
        PropertiesViewDialog dialog = new PropertiesViewDialog(null, System.getProperties(), "Properties Viewer");
        dialog.setVisible(true);
    }
}