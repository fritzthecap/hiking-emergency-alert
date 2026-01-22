package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.mail.MailBuilder.*;
import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import fri.servers.hiking.emergencyalert.persistence.Alert;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Texts that will appear in mails, exclusive route.
 * Registration can be saved to JSON when finished here.
 */
public class MailTextsPage extends AbstractWizardPage
{
    private JTextField helpRequestTitleField;
    private JTextArea helpRequestTextField;
    private JList<String> procedureTodosField;
    private JTextArea passingToNextTextField;
    
    @Override
    protected void buildUi() {
        helpRequestTitleField = SwingUtil.buildTextField(
                i18n("Alert Mail Subject"),
                i18n("The text that will be in mail subject"),
                i18n("Hiking emergency - I need help!"));
        helpRequestTitleField.setColumns(30);
        
        helpRequestTextField = SwingUtil.buildTextArea(
                i18n("Alert Mail Text"),
                i18n("The message's content text"),
                i18n("I had an accident while hiking and need help. This is serious!"));
        helpRequestTextField.setRows(5);
        helpRequestTextField.setLineWrap(true);
        
        final JComponent todoList = buildProcedureTodosList();
        
        passingToNextTextField = SwingUtil.buildTextArea(
                i18n("Passing-to-next Mail Text"),
                i18n("Text that will be sent to every contact that did not respond in time"),
                i18n("As you did not respond in time, an alert mail has been sent to the next contact person. You can ignore the preceding mail."));
        passingToNextTextField.setRows(3);
        passingToNextTextField.setLineWrap(true);
        
        final JButton macroHelpButton = new JButton(i18n("Text Macros"));
        macroHelpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMacroListDialog();
            }
        });
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        final JPanel textFieldPanel = new JPanel(); // center one-line text field
        textFieldPanel.add(helpRequestTitleField);
        panel.add(textFieldPanel);
        panel.add(macroHelpButton);
        
        panel.add(new JScrollPane(helpRequestTextField)); // full width
        
        panel.add(todoList, BorderLayout.CENTER);
        
        panel.add(Box.createRigidArea(new Dimension(1, 10)));
        panel.add(new JScrollPane(passingToNextTextField));
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel, BorderLayout.CENTER);
        getContentPanel().add(contentPanel);
        
        installFocusListeners();
    }

    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestTitle()))
            helpRequestTitleField.setText(alert.getHelpRequestTitle());
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestText()))
            helpRequestTextField.setText(hike.getAlert().getHelpRequestText());
        
        if (alert.getProcedureTodos() != null && alert.getProcedureTodos().size() > 0) {
            final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodosField.getModel();
            listModel.removeAllElements();
            listModel.addAll(alert.getProcedureTodos());
            
            procedureTodosField.setSelectedIndex(0);
        }
        
        if (StringUtil.isNotEmpty(alert.getPassingToNextText()))
            passingToNextTextField.setText(alert.getPassingToNextText());
    }
    
    @Override
    protected String validateFields() {
        if (StringUtil.isEmpty(helpRequestTitleField.getText()))
            return i18n("Mail Subject must not be empty!");

        if (StringUtil.isEmpty(helpRequestTextField.getText()) && procedureTodosAreEmpty())
            return i18n("Either Mail Text or Steps to be Taken must have content!");
        
        if (StringUtil.isEmpty(passingToNextTextField.getText()))
            return i18n("Passing-to-next Text must not be empty!");
        
        return null;
    }

    @Override
    protected boolean commit(boolean goingForward) {
        if (goingForward && validate() == false)
            return false;
        
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(helpRequestTitleField.getText()))
            alert.setHelpRequestTitle(helpRequestTitleField.getText());
        
        alert.setHelpRequestText(helpRequestTextField.getText());
        
        final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodosField.getModel();
        if (listModel.getSize() > 0) {
            final List<String> newList = new ArrayList<>(listModel.getSize());
            for (Enumeration<String> e = listModel.elements(); e.hasMoreElements(); ) {
                final String todo = e.nextElement();
                if (StringUtil.isNotEmpty(todo))
                    newList.add(todo);
            }
            alert.setProcedureTodos(newList);
        }
        
        if (StringUtil.isNotEmpty(passingToNextTextField.getText()))
            alert.setPassingToNextText(passingToNextTextField.getText());
        
        return true;
    }
    
    
    private boolean procedureTodosAreEmpty() {
        final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodosField.getModel();
        for (Enumeration<String> e = listModel.elements(); e.hasMoreElements(); ) {
            final String todo = e.nextElement();
            if (StringUtil.isNotEmpty(todo))
                return false;
        }
        return true;
    }

    private void installFocusListeners() {
        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validate();
            }
        };
        helpRequestTitleField.addFocusListener(focusListener);
        passingToNextTextField.addFocusListener(focusListener);
        procedureTodosField.addFocusListener(focusListener);
    }
    
    private JComponent buildProcedureTodosList() {
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(i18n("First try to reach me by phone: 123456789."));
        listModel.addElement(i18n("If I do not respond, please call the local emergency service."));
        listModel.addElement(i18n("Forward this mail to them. If they have no mail, tell them my trail from description below."));
        listModel.addElement(i18n("IMPORTANT: when you could organize help, please send a response-mail to this. The MAIL-ID above must be contained in it. That prevents further contacts to be distressed."));
        
        procedureTodosField = new JList<>(listModel); // list of 1-n multiline text-areas
        procedureTodosField.setToolTipText(i18n("Tell the contact what to do when receiving this mail"));
        procedureTodosField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        procedureTodosField.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String theValue = (String) value;
                if (StringUtil.isEmpty(theValue))
                    theValue = " "; // else line has no height
                else
                    theValue = theValue.replace("\n", " | ");
                return super.getListCellRendererComponent(list, theValue, index, isSelected, cellHasFocus);
            }
        });
        
        final JTextArea cellEditor = new JTextArea();
        cellEditor.setToolTipText(i18n("Edit text of this step"));
        cellEditor.setLineWrap(true);
        
        final JButton add = new JButton(i18n("+"));
        add.setToolTipText(i18n("Adds a new item below selected step"));
        add.setFont(add.getFont().deriveFont(Font.BOLD, 14));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = procedureTodosField.getSelectedIndex();
                final int insertionIndex = (selectedIndex < 0) ? listModel.getSize() : selectedIndex + 1;
                listModel.insertElementAt("", insertionIndex);
                procedureTodosField.setSelectedIndex(insertionIndex);
            }
        });
        
        final JButton remove = new JButton(i18n("-"));
        remove.setFont(remove.getFont().deriveFont(Font.BOLD, 14));
        remove.setToolTipText(i18n("Removes the selected step"));
        remove.setEnabled(false);
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = procedureTodosField.getSelectedIndex();
                if (selectedIndex >= 0) {
                    listModel.remove(selectedIndex);
                    cellEditor.setText("");
                    remove.setEnabled(false);
                }
            }
        });
        
        procedureTodosField.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                final int selectedIndex = procedureTodosField.getSelectedIndex();
                
                final boolean selected = (selectedIndex >= 0 && selectedIndex < listModel.getSize());
                remove.setEnabled(selected);
                
                if (selected) {
                    cellEditor.setText(listModel.get(selectedIndex));
                    cellEditor.setCaretPosition(0);
                    cellEditor.requestFocusInWindow();
                }
            }
        });
        
        cellEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                final int selectedIndex = procedureTodosField.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < listModel.getSize())
                    listModel.set(selectedIndex, cellEditor.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });
        
        procedureTodosField.setSelectedIndex(0); // select first row to bring it into editor
        
        // layout
        
        final JComponent scrollTable = new JScrollPane(procedureTodosField);
        procedureTodosField.setVisibleRowCount(4);
        
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        final Dimension buttonSize = new Dimension(50, 26);
        forceSize(add, buttonSize);
        forceSize(remove, buttonSize);
        buttonsPanel.add(add);
        buttonsPanel.add(remove);
        
        final JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollTable, BorderLayout.CENTER);
        tablePanel.add(buttonsPanel, BorderLayout.EAST);
        
        final JSplitPane listSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listSplitPane.setResizeWeight(0.5);
        listSplitPane.setTopComponent(tablePanel);
        listSplitPane.setBottomComponent(new JScrollPane(cellEditor));

        final JPanel fullSizePanel = new JPanel(new BorderLayout());
        fullSizePanel.setBorder(BorderFactory.createTitledBorder(i18n("Steps to be taken by Contact")));
        fullSizePanel.add(listSplitPane, BorderLayout.CENTER);
        
        return fullSizePanel;
    }

    private void forceSize(JButton button, Dimension buttonSize) {
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setMinimumSize(buttonSize);
    }

    private void showMacroListDialog() {
        final String text = 
            "<html><body><h3>"+i18n("Macros you can use in all mail texts:")+"</h3><ul>"+
            "<li><b>"+MACRO_CONTACT+"</b> - "+i18n("the name of the contact the mail will be sent to")+"</li>"+
            "<li><b>"+MACRO_NEXT_CONTACT+"</b> - "+i18n("the name of the contact the next mail will be sent to")+"</li>"+
            "<li><b>"+MACRO_ALL_CONTACTS+"</b> - "+i18n("all contact names that may receive an alert mail")+"</li>"+
            "<li><b>"+MACRO_PHONE+"</b> - "+i18n("your phone number, if you entered it")+"</li>"+
            "<ul></body></html>";
        final JEditorPane message = new JEditorPane("text/html", text);
        JOptionPane.showMessageDialog(
                getFrame(), 
                message,
                i18n("Text Macros"), 
                JOptionPane.INFORMATION_MESSAGE);
    }
}