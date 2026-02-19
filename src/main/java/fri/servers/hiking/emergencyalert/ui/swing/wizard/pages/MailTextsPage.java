package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.persistence.MailBuilder.*;
import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import fri.servers.hiking.emergencyalert.persistence.entities.Alert;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Texts that will appear in mails, exclusive route.
 * Registration can be saved to JSON when finished here.
 */
public class MailTextsPage extends AbstractWizardPage
{
    private JTextField mailSubjectField;
    private JTextArea mailIntroductionTextField;
    private JList<String> procedureTodosField;
    private JTextArea passingToNextTextField;
    private JCheckBox usePassingToNextMail;
    
    @Override
    protected String getTitle() {
        return i18n("Mail Texts");
    }
    
    @Override
    protected void buildUi() {
        mailSubjectField = SwingUtil.buildTextField(
                "* "+i18n("Alert Mail Subject"),
                i18n("The text that will be in mail subject"),
                i18n("Hiking emergency - I need help!"));
        mailSubjectField.setColumns(40);
        
        mailIntroductionTextField = SwingUtil.buildTextArea(
                i18n("The message's content text"),
                i18n("I had an accident while hiking and need help. This is serious!"));
        mailIntroductionTextField.setRows(4);
        
        final JComponent todoList = buildProcedureTodosList();
        
        passingToNextTextField = SwingUtil.buildTextArea(
                i18n("Text that will be sent to every contact that did not respond in time"),
                i18n("As you did not respond in time, an alert mail has been sent to next contact $nextContact. You can ignore the preceding mail."));
        passingToNextTextField.setRows(3);
        
        usePassingToNextMail = new JCheckBox(i18n("Use Passing-to-next Mail"), true);
        usePassingToNextMail.setToolTipText(i18n("When off, no mail is sent to previous contact when next contact gets alerted"));
        usePassingToNextMail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passingToNextTextField.setEnabled(usePassingToNextMail.isSelected());
            }
        });
        
        final JButton variablesHelpButton = new JButton(i18n("Variables"));
        variablesHelpButton.setToolTipText(i18n("Text substitution variables you can use here"));
        variablesHelpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMacroListDialog();
            }
        });
        
        final JPanel subjectTextPanel = new JPanel();
        subjectTextPanel.setLayout(new BoxLayout(subjectTextPanel, BoxLayout.X_AXIS));
        final JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // avoid field height stretched
        textFieldPanel.add(mailSubjectField);
        subjectTextPanel.add(textFieldPanel);
        subjectTextPanel.add(Box.createRigidArea(new Dimension(6, 1)));
        subjectTextPanel.add(variablesHelpButton);
        subjectTextPanel.add(Box.createRigidArea(new Dimension(4, 1)));
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(subjectTextPanel);
        
        panel.add(SwingUtil.buildScrollPane(
                "* "+i18n("Alert Mail Text"), 
                mailIntroductionTextField)); // full width
        
        panel.add(todoList);
        
        usePassingToNextMail.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(usePassingToNextMail);
        
        panel.add(SwingUtil.buildScrollPane(
                i18n("Continue-to-next Mail Text"),
                passingToNextTextField));
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel, BorderLayout.CENTER);
        getContentPanel().add(contentPanel);
        
        installFocusValidation();
    }

    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestSubject()))
            mailSubjectField.setText(alert.getHelpRequestSubject());
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestIntroduction()))
            mailIntroductionTextField.setText(hike.getAlert().getHelpRequestIntroduction());
        
        if (alert.getProcedureTodos() != null && alert.getProcedureTodos().size() > 0) {
            final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodosField.getModel();
            listModel.removeAllElements();
            listModel.addAll(alert.getProcedureTodos());
            
            procedureTodosField.setSelectedIndex(0);
        }
        
        usePassingToNextMail.setSelected(alert.isUsePassingToNextMail());
        passingToNextTextField.setEnabled(alert.isUsePassingToNextMail());
        
        if (StringUtil.isNotEmpty(alert.getPassingToNextText()))
            passingToNextTextField.setText(alert.getPassingToNextText());
    }
    
    @Override
    protected String validateFields() {
        if (StringUtil.isEmpty(mailSubjectField.getText()))
            return i18n("Mail Subject must not be empty!");

        if (StringUtil.isEmpty(mailIntroductionTextField.getText()) && procedureTodosAreEmpty())
            return i18n("Either Mail Text or Steps to be Taken must have content!");
        
        if (usePassingToNextMail.isSelected())
            if (StringUtil.isEmpty(passingToNextTextField.getText()))
                return i18n("Passing-to-next Text must not be empty!");
        
        return null;
    }

    @Override
    protected boolean commit(boolean goingForward) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(mailSubjectField.getText()))
            alert.setHelpRequestSubject(mailSubjectField.getText());
        
        alert.setHelpRequestIntroduction(mailIntroductionTextField.getText());
        
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
        
        alert.setUsePassingToNextMail(usePassingToNextMail.isSelected());
        
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

    private JComponent buildProcedureTodosList() {
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(i18n("First try to reach me by phone $phone."));
        listModel.addElement(i18n("If I do not respond, please call the local emergency service."));
        listModel.addElement(i18n("Forward this mail to them, or tell them my trail from description below."));
        listModel.addElement(i18n("IMPORTANT: when you could organize help, please send a response-mail. The MAIL-ID above must be contained in it. That prevents further contacts to be distressed."));
        
        procedureTodosField = new JList<>(listModel); // list of 1-n multiline text-areas
        procedureTodosField.setFont(procedureTodosField.getFont().deriveFont(Font.PLAIN)); // default font is BOLD
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
        
        final JTextArea cellEditor = SwingUtil.buildTextArea(i18n("Edit text of this todo step"), null);
        cellEditor.setRows(2);
        
        final JButton add = SwingUtil.getAddOrRemoveButton(
                true, 
                i18n("Adds a new item below selected step, or at end"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final int selectedIndex = procedureTodosField.getSelectedIndex();
                        final int insertionIndex = (selectedIndex < 0) ? listModel.getSize() : selectedIndex + 1;
                        listModel.insertElementAt("", insertionIndex);
                        procedureTodosField.setSelectedIndex(insertionIndex);
                    }
                });
        
        final JButton remove = SwingUtil.getAddOrRemoveButton(
                false, 
                i18n("Removes the selected step"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final int selectedIndex = procedureTodosField.getSelectedIndex();
                        if (selectedIndex >= 0) {
                            listModel.remove(selectedIndex);
                            final int newSize = procedureTodosField.getModel().getSize();
                            if (newSize <= 0) {
                                cellEditor.setText("");
                                ((JButton) e.getSource()).setEnabled(false);
                            }
                            else {
                                procedureTodosField.setSelectedIndex(
                                        selectedIndex >= newSize ? selectedIndex - 1 : selectedIndex);
                            }
                        }
                    }
                });
        remove.setEnabled(false);
        
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
        procedureTodosField.setVisibleRowCount(3);
        
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(add);
        buttonsPanel.add(remove);
        
        final JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(scrollTable, BorderLayout.CENTER);
        tablePanel.add(buttonsPanel, BorderLayout.EAST);
        
        final JSplitPane listSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listSplitPane.setResizeWeight(0.8);
        listSplitPane.setTopComponent(tablePanel);
        listSplitPane.setBottomComponent(new JScrollPane(cellEditor));

        final JPanel all = new JPanel(new BorderLayout());
        all.setBorder(BorderFactory.createTitledBorder(i18n("Steps to be taken by Contact in Case of Emergency")));
        all.add(listSplitPane, BorderLayout.CENTER);
        
        return all;
    }

    private void showMacroListDialog() {
        final String text = 
            "<html><body><h3>"+i18n("Variables you can use in all mail text parts here:")+"</h3><ul>"+
            "<li><b>"+MACRO_CONTACT+"</b> - "+i18n("the name of the contact the mail will be sent to")+"</li>"+
            "<li><b>"+MACRO_NEXT_CONTACT+"</b> - "+i18n("the name of the next contact a mail will be sent to")+"</li>"+
            "<li><b>"+MACRO_ALL_CONTACTS+"</b> - "+i18n("all contact names that may receive an alert mail in a row")+"</li>"+
            "<li><b>"+MACRO_ME+"</b> - "+i18n("your name, if you entered it")+"</li>"+
            "<li><b>"+MACRO_MY_PHONE+"</b> - "+i18n("your phone number, if you entered it")+"</li>"+
            "<li><b>"+MACRO_BEGIN_TIME+"</b> - "+i18n("the begin date/time of your absence")+"</li>"+
            "<li><b>"+MACRO_END_TIME+"</b> - "+i18n("the end date/time of your absence")+"</li>"+
            "<ul></body></html>";
        final JEditorPane message = new JEditorPane("text/html", text);
        JOptionPane.showMessageDialog(
                getFrame(), 
                message,
                i18n("Text Substitutions"), 
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void installFocusValidation() {
        final JComponent[] focusComponents = new JComponent[] {
                mailSubjectField,
                mailIntroductionTextField,
                passingToNextTextField,
                procedureTodosField,
                usePassingToNextMail,
        };
        installFocusListener(focusComponents, null);
    }
}