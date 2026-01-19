package fri.servers.hiking.emergencyalert.ui.swing.util;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
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
        final JScrollPane scrollPane = buildPanel();
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(scrollPane, BorderLayout.CENTER);
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
    protected final JComponent buildReadOnlyTable(Properties readOnlyProperties) {
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

    
    private JScrollPane buildPanel() {
        this.namesAndValues = buildNamesAndValues(properties);

        final Vector<Object> columnNames = buildColumns();
        addMoreColumns(columnNames);
        
        model = new DefaultTableModel(namesAndValues, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2)
                    return Boolean.class;
                return String.class;
            }
        };
        table = new JTable(model);

        setUneditableButCopyableCellEditor(); // allow copy, deny editing

        setTableHeight(table, model);

        return new JScrollPane(table);
    }

    private void setTableHeight(JTable table, TableModel model) {
        final int rowCount = Math.min(model.getRowCount() + 1, 30);
        final int height = rowCount * table.getRowHeight();
        table.setPreferredScrollableViewportSize(new Dimension(400, height));
    }

    private Vector<Object> buildColumns() {
        final Vector<Object> columnNames = newRow();
        columnNames.addElement(i18n("Name"));
        columnNames.addElement(i18n("Value"));
        return columnNames;
    }
    
    private Vector<Vector<Object>> buildNamesAndValues(Properties properties) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Vector<String> names = new Vector(properties.keySet());
        Collections.sort(names);
        
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