package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
        
        // procedureTodos START
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
                value = ((String) value).replace("\n", " | ");
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        
        final JTextArea cellEditor = new JTextArea();
        cellEditor.setRows(3);
        procedureTodos.addListSelectionListener(new ListSelectionListener() {
            private int selectedIndex = -1;
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                if (selectedIndex >= 0)
                    listModel.set(selectedIndex, cellEditor.getText());
                
                selectedIndex = procedureTodos.getSelectedIndex();
                cellEditor.setText(listModel.get(selectedIndex));
                cellEditor.setCaretPosition(0);
                cellEditor.requestFocusInWindow();
            }
        });
        // procedureTodos END
        
        passingToNextText = SwingUtil.buildTextArea(
                i18n("Passing-to-next Mail Text"),
                i18n("Text that will be sent to every contact that did not respond in time"),
                i18n("As you did not respond in time, another alert has been sent to the next contact person. You can ignore the preceding mail."));
        passingToNextText.setRows(3);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        final JPanel textFieldPanel = new JPanel(); // center one-line text field
        textFieldPanel.add(helpRequestTitle);
        panel.add(textFieldPanel);
        panel.add(new JScrollPane(helpRequestText)); // full width
        
        final JPanel fullSizePanel = new JPanel(new BorderLayout());
        fullSizePanel.setBorder(BorderFactory.createTitledBorder(i18n("Steps to be taken")));
        final JSplitPane listSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        listSplitPane.setResizeWeight(0.3);
        listSplitPane.setLeftComponent(new JScrollPane(procedureTodos));
        listSplitPane.setRightComponent(new JScrollPane(cellEditor));
        fullSizePanel.add(listSplitPane);
        panel.add(fullSizePanel, BorderLayout.CENTER);
        
        panel.add(Box.createRigidArea(new Dimension(1, 30)));
        panel.add(passingToNextText);
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel);
        getContentPanel().add(contentPanel);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestTitle()))
            helpRequestTitle.setText(alert.getHelpRequestTitle());
        
        if (StringUtil.isNotEmpty(alert.getHelpRequestText()))
            helpRequestText.setText(hike.getAlert().getHelpRequestText());
        
        if (alert.getProcedureTodos().size() > 0) {
            final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodos.getModel();
            listModel.removeAllElements();
            listModel.addAll(alert.getProcedureTodos());
        }
        
        if (StringUtil.isNotEmpty(alert.getPassingToNextText()))
            passingToNextText.setText(alert.getPassingToNextText());
    }
    
    @Override
    public boolean commit(boolean isWindowClose) {
        final Alert alert = getHike().getAlert();
        
        if (StringUtil.isNotEmpty(helpRequestTitle.getText()))
            alert.setHelpRequestTitle(helpRequestTitle.getText());
        
        if (StringUtil.isNotEmpty(helpRequestText.getText()))
            alert.setHelpRequestText(helpRequestText.getText());
        
        final DefaultListModel<String> listModel = (DefaultListModel<String>) procedureTodos.getModel();
        final List<String> newList = new ArrayList<>(listModel.getSize());
        for (Enumeration<String> e = listModel.elements(); e.hasMoreElements(); )
            newList.add(e.nextElement());
        alert.setProcedureTodos(newList);
        
        if (StringUtil.isNotEmpty(passingToNextText.getText()))
            alert.setPassingToNextText(passingToNextText.getText());
        
        return true;
    }
}