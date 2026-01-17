package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Properties Dialog, that lets view and copy names and values
 * of a properties map in a JTable.
 */
public class PropertiesViewDialog extends JDialog
{
    protected Properties properties;
    protected Vector<Vector<String>> namesAndValues;
    protected JTable table;
    protected TableModel model;

    public PropertiesViewDialog(JFrame frame, boolean modal, Properties properties, String title) {
        super(frame, title, modal);
        this.properties = properties;
        
        buildGUI();
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                close();
            }
        });
    }

    protected Container buildGUI() {
        JScrollPane sp = buildPanel();
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    protected void close() {
        dispose();
    }

    protected void setUneditableEditor() {
        DefaultCellEditor cellEditor = (DefaultCellEditor) table.getDefaultEditor(String.class);
        JTextField editor = (JTextField) cellEditor.getComponent();
        editor.setEditable(false);
    }

    
    private JScrollPane buildPanel() {
        this.namesAndValues = buildNamesAndValues(properties);

        Vector<String> columnNames = new Vector<>(2);
        columnNames.addElement("Name");
        columnNames.addElement("Value");
        model = new DefaultTableModel(namesAndValues, columnNames);
        table = new JTable(model);

        setUneditableEditor(); // allow copy, deny editing

        int rowCount = Math.min(model.getRowCount() + 1, 30);
        int height = rowCount * table.getRowHeight();
        table.setPreferredScrollableViewportSize(new Dimension(400, height));

        JScrollPane sp = new JScrollPane(table);
        return sp;
    }

    private Vector<Vector<String>> buildNamesAndValues(Properties properties) {
        Vector<String> names = new Vector<>(properties.size() > 0 ? properties.size() : 1);
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); )
            names.addElement((String) e.nextElement());
        Collections.sort(names);
        
        Vector<Vector<String>> nameAndValues = new Vector<>(properties.size() > 0 ? properties.size() : 1);
        for (Enumeration<?> e = names.elements(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            Vector<String> v = new Vector<>(2);
            v.addElement(name);
            v.addElement(properties.getProperty(name));
            nameAndValues.addElement(v);
        }
        return nameAndValues;
    }


    // test main
    public static void main(String[] args) {
        PropertiesViewDialog dialog = new PropertiesViewDialog(null, true, System.getProperties(), "Properties Viewer");
        dialog.setVisible(true);
    }
}