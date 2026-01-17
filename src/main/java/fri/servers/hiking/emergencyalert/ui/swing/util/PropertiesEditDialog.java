package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Properties Dialog, that lets edit names and values of a properties map.
 * A popup menu lets add and delete rows.
 */
public class PropertiesEditDialog extends PropertiesViewDialog
{
    private JButton ok, cancel;
    private boolean canceled = true;
    private JPopupMenu popup;
    private JMenuItem delete, insert;

    public PropertiesEditDialog(JFrame frame, boolean modal, Properties properties, String title) {
        super(frame, modal, properties, title);
        
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == insert)
                    insertRowAtSelections();
                else if (e.getSource() == delete)
                    removeSelectedRows();
            }
        };
        popup = new JPopupMenu();
        popup.add(insert = new JMenuItem("Insert"));
        insert.addActionListener(actionListener);
        popup.add(delete = new JMenuItem("Delete"));
        delete.addActionListener(actionListener);
        
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
        };
        table.addMouseListener(mouseListener);
        table.getParent().addMouseListener(mouseListener);
        
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                    removeSelectedRows();
                else if (e.getKeyCode() == KeyEvent.VK_INSERT)
                    insertRowAtSelections();
            }
        });
    }

    @Override
    protected Container buildGUI() {
        Container container = super.buildGUI();

        table.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
        
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == ok)
                    ok();
                else if (e.getSource() == cancel)
                    close();
            }
        };
        JPanel panel = new JPanel();
        panel.add(ok = new JButton("Ok"));
        ok.addActionListener(actionListener);
        panel.add(cancel = new JButton("Cancel"));
        cancel.addActionListener(actionListener);
        container.add(panel, BorderLayout.SOUTH);
        
        if (model.getRowCount() <= 0) // add an empty row
            insertRowAt("", "", 0);

        return container;
    }

    /** Do nothing to avoid uneditable textfield from PropViewDialog */
    @Override
    protected void setUneditableEditor() {
    }

    public Properties getProperties() {
        storeToProperties();
        return this.properties;
    }

    public boolean isCanceled() {
        return canceled;
    }


    private void ok() {
        canceled = false;
        commitTable();
        storeToProperties();
        close();
    }
    
    private void showPopup(MouseEvent e) {
        if (table.getSelectedRowCount() <= 0) { // set selection if not set
            int row = table.rowAtPoint(e.getPoint());
            DefaultListSelectionModel lm = (DefaultListSelectionModel) table.getSelectionModel();
            lm.setSelectionInterval(row, row);
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void commitTable() {
        DefaultCellEditor cellEditor = (DefaultCellEditor) table.getCellEditor();
        if (cellEditor != null)
            cellEditor.stopCellEditing();
        else
            table.editingStopped(null);
    }

    private void storeToProperties() {
        properties.clear();

        for (int i = 0; i < namesAndValues.size(); i++) {
            Vector<String> v = namesAndValues.elementAt(i);
            String name = ((String) v.elementAt(0)).trim();
            String value = ((String) v.elementAt(1)).trim();

            if (name.length() > 0)
                properties.put(name, value);
        }
    }

    private void insertRowAtSelections() {
        int[] indexes = getSelectedIndexes();
        if (indexes != null) {
            table.getSelectionModel().clearSelection();
            // clone and insert all selected rows
            for (int i = indexes.length - 1; i >= 0; i--)
                insertRowAt(indexes[i]);
            
            // if exactly one row inserted, edit it
            if (indexes.length == 1)
                table.editCellAt(indexes[0] + 1, 0);
        }
        else { // insert row at end
            insertRowAt("", "", model.getRowCount() - 1);
        }
    }

    private void insertRowAt(int row) {
        int newrow = row + 1;
        insertRowAt(new String((String) model.getValueAt(row, 0)), new String((String) model.getValueAt(row, 1)),
                newrow);
        DefaultListSelectionModel lm = (DefaultListSelectionModel) table.getSelectionModel();
        lm.addSelectionInterval(newrow, newrow);
    }

    private void insertRowAt(String name, String value, int index) {
        Vector<String> row = new Vector<>(2);
        row.addElement(name);
        row.addElement(value);
        ((DefaultTableModel) model).insertRow(Math.max(index, 0), row);
    }

    private void removeSelectedRows() {
        int[] indexes = getSelectedIndexes();
        if (indexes != null)
            for (int i = indexes.length - 1; i >= 0; i--)
                ((DefaultTableModel) model).removeRow(indexes[i]);

        if (model.getRowCount() <= 0)
            insertRowAt("", "", 0);
    }

    private int[] getSelectedIndexes() {
        int[] selectedIndexes = table.getSelectedRows();
        if (selectedIndexes == null || selectedIndexes.length <= 0)
            return null;
        Arrays.sort(selectedIndexes);
        return selectedIndexes;
    }

    
    // test main
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("aaa", "AAA");
        properties.put("ddd", "DDD");
        properties.put("eee", "EEE");
        properties.put("bbb", "BBB");
        properties.put("ccc", "CCC");
        PropertiesEditDialog dialog = new PropertiesEditDialog(null, true, properties, "Properties Editor");
        dialog.setVisible(true);
    }
}