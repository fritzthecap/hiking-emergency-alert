package fri.servers.hiking.emergencyalert.ui.swing.util;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;

/**
 * Properties Dialog, that lets edit names and values of a properties map.
 * A popup menu lets add and delete rows.
 */
public class PropertiesEditDialog extends PropertiesViewDialog
{
    private JButton ok, cancel;
    private JPopupMenu popup;
    private JMenuItem delete, insert;

    public PropertiesEditDialog(Frame parent, Properties properties, String title) {
        super(parent, properties, title);
    }
    
    @Override
    protected Container buildUi() {
        final Container contentPane = super.buildUi();

        ((DefaultCellEditor) table.getDefaultEditor(String.class)).setClickCountToStart(1);
        
        final JPanel buttonPanel = new JPanel(); // centers buttons
        buttonPanel.add(ok = new JButton(i18n("Ok")));
        buttonPanel.add(cancel = new JButton(i18n("Cancel")));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == ok)
                    ok();
                else if (e.getSource() == cancel)
                    close();
            }
        };
        ok.addActionListener(actionListener);
        cancel.addActionListener(actionListener);
        
        if (model.getRowCount() <= 0) // add an empty row
            insertRowAt("", "", 0);

        buildActions();
        
        return contentPane;
    }

    private void buildActions() {
        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == insert)
                    insertRowAtSelections();
                else if (e.getSource() == delete)
                    removeSelectedRows();
            }
        };
        popup = new JPopupMenu();
        popup.add(insert = new JMenuItem(i18n("Insert")));
        insert.addActionListener(actionListener);
        popup.add(delete = new JMenuItem(i18n("Delete")));
        delete.addActionListener(actionListener);
        
        final MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    showPopup(e);
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

    /** Do nothing to enable editing. */
    @Override
    protected void setUneditableButCopyableCellEditor() {
    }
    
    @Override
    protected void addMoreColumns(Vector<Object> columnNames) {
        columnNames.addElement(i18n("Include"));
    }
    
    @Override
    protected void addMoreTableCells(Vector<Object> newRow) {
        newRow.add(Boolean.valueOf(false)); // default do NOT include property
    }
    
    @Override
    protected Class<?> getColumnClassForIndex(int columnIndex) {
        if (columnIndex == 2)
            return Boolean.class;
        return super.getColumnClassForIndex(columnIndex);
    }

    protected boolean validateProperties() {
        commitTable();
        return storeToProperties(); // false when duplicates were found
    }
    
    protected void ok() {
        if (validateProperties())
            close();
    }

    
    
    private void showPopup(MouseEvent e) {
        if (table.getSelectedRowCount() <= 0) { // set selection if not set
            final int row = table.rowAtPoint(e.getPoint());
            final DefaultListSelectionModel lm = (DefaultListSelectionModel) table.getSelectionModel();
            lm.setSelectionInterval(row, row);
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void commitTable() {
        final DefaultCellEditor cellEditor = (DefaultCellEditor) table.getCellEditor();
        if (cellEditor != null)
            cellEditor.stopCellEditing();
        else
            table.editingStopped(null);
    }

    private boolean storeToProperties() {
        final Properties includedProperties = new Properties();
        for (int i = 0; i < namesAndValues.size(); i++) {
            final Vector<Object> tableRow = namesAndValues.elementAt(i);
            final Boolean include = ((Boolean) tableRow.elementAt(2));
            if (Boolean.TRUE.equals(include)) {
                final String name = ((String) tableRow.elementAt(0)).trim();
                if (name.length() > 0) {
                    if (includedProperties.containsKey(name)) {
                        table.setRowSelectionInterval(i, i);
                        errorMessage(i18n("Found duplicate property name:")+" "+name);
                        return false;
                    }
                    includedProperties.put(name, ((String) tableRow.elementAt(1)).trim());
                }
            }
        }
        
        properties.clear();
        properties.putAll(includedProperties);
        
        return true;
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, i18n("Error"), JOptionPane.ERROR_MESSAGE);
    }

    private void insertRowAtSelections() {
        final int[] indexes = getSelectedIndexes();
        if (indexes != null) {
            table.getSelectionModel().clearSelection();
            for (int i = indexes.length - 1; i >= 0; i--) // insert clones of all selected rows
                insertRowAt(indexes[i]);
            
            if (indexes.length == 1) // if exactly one row was inserted, edit it
                table.editCellAt(indexes[0] + 1, 0);
        }
        else { // insert row at end
            insertRowAt("", "", model.getRowCount() - 1);
        }
    }

    private void insertRowAt(int rowIndex) {
        final int newRowIndex = rowIndex + 1;
        insertRowAt("", "", newRowIndex);
        final DefaultListSelectionModel lm = (DefaultListSelectionModel) table.getSelectionModel();
        lm.addSelectionInterval(newRowIndex, newRowIndex);
    }

    private void insertRowAt(String name, String value, int rowIndex) {
        final Vector<Object> row = newRow();
        row.addElement(name);
        row.addElement(value);
        row.addElement(Boolean.TRUE);
        ((DefaultTableModel) model).insertRow(Math.max(rowIndex, 0), row);
    }

    private void removeSelectedRows() {
        commitTable(); // close cell editor, else wrong values in cells!
        
        final int[] indexes = getSelectedIndexes();
        if (indexes != null)
            for (int i = indexes.length - 1; i >= 0; i--)
                ((DefaultTableModel) model).removeRow(indexes[i]);

        if (model.getRowCount() <= 0)
            insertRowAt("", "", 0);
    }

    private int[] getSelectedIndexes() {
        final int[] selectedIndexes = table.getSelectedRows();
        if (selectedIndexes == null || selectedIndexes.length <= 0)
            return null;
        Arrays.sort(selectedIndexes);
        return selectedIndexes;
    }

    
    /** Test main. */
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("aaa", "AAA");
        properties.put("ddd", "DDD");
        properties.put("eee", "EEE");
        properties.put("bbb", "BBB");
        properties.put("ccc", "CCC");
        PropertiesEditDialog dialog = new PropertiesEditDialog(null, properties, "Properties Editor");
        dialog.setVisible(true);
    }
}