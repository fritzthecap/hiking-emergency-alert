package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JList;
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
    private JTextField helpRequestTitle;
    private JTextArea helpRequestText;
    private JList<String> procedureTodos;
    private JTextArea passingToNextText;
    
    @Override
    protected void buildUi() {
        helpRequestTitle = SwingUtil.buildTextField(
                i18n("Alert Mail Subject"),
                i18n("The text that will be in mail subject"),
                i18n("Hiking emergency - I need help!"));
        helpRequestTitle.setColumns(30);
        
        helpRequestText = SwingUtil.buildTextArea(
                i18n("Alert Mail Text"),
                i18n("The message's content text"),
                i18n("I had an accident while hiking and need help. Below you find a description of my planned route."));
        helpRequestText.setRows(5);
        helpRequestText.setLineWrap(true);
        
        final JComponent todoList = buildProcedureTodosList();
        
        passingToNextText = SwingUtil.buildTextArea(
                i18n("Passing-to-next Mail Text"),
                i18n("Text that will be sent to every contact that did not respond in time"),
                i18n("As you did not respond in time, another alert has been sent to the next contact person. You can ignore the preceding mail."));
        passingToNextText.setRows(3);
        passingToNextText.setLineWrap(true);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        final JPanel textFieldPanel = new JPanel(); // center one-line text field
        textFieldPanel.add(helpRequestTitle);
        panel.add(textFieldPanel);
        
        panel.add(new JScrollPane(helpRequestText)); // full width
        
        panel.add(todoList, BorderLayout.CENTER);
        
        panel.add(Box.createRigidArea(new Dimension(1, 30)));
        panel.add(passingToNextText);
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel, BorderLayout.CENTER);
        getContentPanel().add(contentPanel);
    }

    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestTitle()))
            helpRequestTitle.setText(alert.getHelpRequestTitle());
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestText()))
            helpRequestText.setText(hike.getAlert().getHelpRequestText());
        
        if (alert.getProcedureTodos() != null && alert.getProcedureTodos().size() > 0) {
            final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodos.getModel();
            listModel.removeAllElements();
            listModel.addAll(alert.getProcedureTodos());
        }
        
        if (StringUtil.isNotEmpty(alert.getPassingToNextText()))
            passingToNextText.setText(alert.getPassingToNextText());
    }
    
    @Override
    protected boolean commit(boolean isWindowClose) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(helpRequestTitle.getText()))
            alert.setHelpRequestTitle(helpRequestTitle.getText());
        
        if (StringUtil.isNotEmpty(helpRequestText.getText()))
            alert.setHelpRequestText(helpRequestText.getText());
        
        final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodos.getModel();
        if (listModel.getSize() > 0) {
            final List<String> newList = new ArrayList<>(listModel.getSize());
            for (Enumeration<String> e = listModel.elements(); e.hasMoreElements(); ) {
                final String todo = e.nextElement();
                if (StringUtil.isNotEmpty(todo))
                    newList.add(todo);
            }
            alert.setProcedureTodos(newList);
        }
        
        if (StringUtil.isNotEmpty(passingToNextText.getText()))
            alert.setPassingToNextText(passingToNextText.getText());
        
        return true;
    }
    
    
    private JComponent buildProcedureTodosList() {
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(i18n("First try to reach me by phone: 123456789."));
        listModel.addElement(i18n("If I do not respond, please call the local emergency service."));
        listModel.addElement(i18n("Forward this mail to them. If they have no mail, tell them my trail from description below."));
        listModel.addElement(i18n("IMPORTANT: when you could organize help, please send a response-mail to this. The MAIL-ID above must be contained in it. That prevents further contacts to be distressed."));
        
        procedureTodos = new JList<>(listModel); // list of 1-n multiline text-areas
        procedureTodos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        procedureTodos.setCellRenderer(new DefaultListCellRenderer() {
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
        cellEditor.setLineWrap(true);
        
        final JButton add = new JButton(i18n("Add"));
        add.setToolTipText(i18n("Adds a new line below selected line"));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = procedureTodos.getSelectedIndex();
                final int insertionIndex = (selectedIndex < 0) ? listModel.getSize() : selectedIndex + 1;
                listModel.insertElementAt("", insertionIndex);
                procedureTodos.setSelectedIndex(insertionIndex);
            }
        });
        
        final JButton remove = new JButton(i18n("Remove"));
        remove.setToolTipText(i18n("Removes the selected line"));
        remove.setEnabled(false);
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = procedureTodos.getSelectedIndex();
                if (selectedIndex >= 0) {
                    listModel.remove(selectedIndex);
                    cellEditor.setText("");
                    remove.setEnabled(false);
                }
            }
        });
        
        procedureTodos.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                final int selectedIndex = procedureTodos.getSelectedIndex();
                
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
                final int selectedIndex = procedureTodos.getSelectedIndex();
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
        
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(add);
        buttonsPanel.add(remove);
        final JPanel listAndButtonsPanel = new JPanel(new BorderLayout());
        listAndButtonsPanel.add(buttonsPanel, BorderLayout.NORTH);
        listAndButtonsPanel.add(new JScrollPane(procedureTodos), BorderLayout.CENTER);
        
        final JSplitPane listSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        listSplitPane.setResizeWeight(0.1);
        listSplitPane.setLeftComponent(listAndButtonsPanel);
        listSplitPane.setRightComponent(new JScrollPane(cellEditor));

        final JPanel fullSizePanel = new JPanel(new BorderLayout());
        fullSizePanel.setBorder(BorderFactory.createTitledBorder(i18n("Steps to be taken")));
        fullSizePanel.add(listSplitPane, BorderLayout.CENTER);
        
        return fullSizePanel;
    }
}