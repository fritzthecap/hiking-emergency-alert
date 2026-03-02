package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.components;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;

/** Adds a right-side buttons-bar to contacts table. */
public class ContactsPanel extends JPanel
{
    private ContactsTable contactsTable;
    
    public ContactsPanel(ContactsTable contactsTable) {
        super(new BorderLayout());
        
        this.contactsTable = contactsTable;
        
        // START keep order of statements
        final JScrollPane scrollPane = new JScrollPane(contactsTable);
        SwingUtil.makeComponentFocusable(contactsTable);
        // END keep order of statements
        
        add(scrollPane, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.EAST);
    }

    private JComponent buildButtonPanel() {
        final ActionListener deleteListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DefaultTableModel model = (DefaultTableModel) contactsTable.getModel();
                final int[] selectedRows = contactsTable.getSelectedRows(); // is in increasing order
                for (int i = selectedRows.length - 1; i >= 0; i--)
                    model.removeRow(selectedRows[i]);
                contactsTable.addEmptyRowWhenNeeded();
            }
        };
        final JButton deleteButton = SwingUtil.getSmallButton(
                "\u2715",
                i18n("Delete selected rows"),
                deleteListener);
        
        final JButton moveUpButton = SwingUtil.getSmallButton(
                "\u2191",
                i18n("Move selected rows upwards"),
                new MoveListener(contactsTable, true));
        
        final JButton moveDownButton = SwingUtil.getSmallButton(
                "\u2193",
                i18n("Move selected rows downwards"),
                new MoveListener(contactsTable, false));
        
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(21)); // go down from table-header to rows
        buttonPanel.add(deleteButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);
        
        final ListSelectionListener buttonEnablingListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e != null && e.getValueIsAdjusting())
                    return;
                
                final int[] selectedRows = contactsTable.getSelectedRows();
                final boolean selectionExists = (selectedRows.length > 0);
                deleteButton.setEnabled(selectionExists);
                
                boolean moveUpPossible = selectionExists;
                boolean moveDownPossible = selectionExists;
                
                for (int i = 0; i < selectedRows.length; i++) {
                    if (selectedRows[i] == 0)
                        moveUpPossible = false;
                    
                    if (selectedRows[i] == contactsTable.getRowCount() - 1)
                        moveDownPossible = false;
                }
                
                moveUpButton.setEnabled(moveUpPossible);
                moveDownButton.setEnabled(moveDownPossible);
            }
        };
        
        buttonEnablingListener.valueChanged(null); // set initial state
        contactsTable.getSelectionModel().addListSelectionListener(buttonEnablingListener);
        
        return buttonPanel;
    }

    
    private static class MoveListener implements ActionListener
    {
        private final JTable table;
        private final boolean upwards;
        
        MoveListener(JTable table, boolean upwards) {
            this.table = table;
            this.upwards = upwards;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (table.isEditing())
                table.getCellEditor().stopCellEditing();
            
            final DefaultTableModel model = (DefaultTableModel) table.getModel();
            final int rowCount = model.getRowCount();
            
            final int[] selectedRows = table.getSelectedRows(); // is in increasing order
            final int[] newSelectedRows = new int[selectedRows.length];
            System.arraycopy(selectedRows, 0, newSelectedRows, 0, selectedRows.length);
            
            final int start = upwards ? 0 : (selectedRows.length - 1); 
            for (int i = start; upwards ? (i < selectedRows.length) : (i >= 0); ) {
                final int rowIndex = selectedRows[i];
                final boolean movePossible = upwards ? (rowIndex > 0) : (rowIndex < rowCount - 1);
                if (movePossible) {
                    final int newIndex = upwards ? (rowIndex - 1) : (rowIndex + 1);
                    model.moveRow(rowIndex, rowIndex, newIndex);
                    newSelectedRows[i] = newIndex;
                }
                i = upwards ? (i + 1) : (i - 1);
            }
            
            table.clearSelection();
            for (int i = 0; i < newSelectedRows.length; i++)
                table.getSelectionModel().addSelectionInterval(newSelectedRows[i], newSelectedRows[i]);
        }
    }
}