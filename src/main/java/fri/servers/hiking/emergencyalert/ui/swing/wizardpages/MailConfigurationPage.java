package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import fri.servers.hiking.emergencyalert.mail.MailException;
import fri.servers.hiking.emergencyalert.mail.MailUtil;
import fri.servers.hiking.emergencyalert.mail.impl.ConnectionCheck;
import fri.servers.hiking.emergencyalert.mail.impl.MailProperties;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.util.PropertiesEditDialog;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;
import jakarta.mail.Authenticator;

/**
 * Configure your mail server.
 */
public class MailConfigurationPage extends AbstractWizardPage
{
    private JTextField mailUser; // mail.user, required
    
    private JComboBox<String> receiveMailProtocol; //"pop3" or "imap", mail.store.protocol
    private JTextField receiveMailHost; // mail.pop3.host, required
    private JFormattedTextField receiveMailPort; // optional, 110 is POP3 default port, 143 is IMAP

    private JComboBox<String> sendMailProtocol; // "smtp", optional, mail.store.protocol
    private JTextField sendMailHost; // mail.smtp.host, required
    private JFormattedTextField sendMailPort; // optional, 25 is SMTP default port, or 587
    private JTextField sendMailFromAccount; // optional, mail.smtp.from, usually the same as mailUser
    
    private JButton mailPropertiesButton;
    private JButton mailTestButton;

    private JLabel errorField;
    private boolean focusListenerInstalled;
    
    private Authenticator validAuthenticator;
    // TODO: can be passed back to activation, when not null, holds the valid mail password
    
    @Override
    protected AbstractWizardPage nextPage() {
        return new ContactsPage();
    }
    
    @Override
    protected void commitData() {
        commitToMailConfiguration();
    }
    
    @Override
    protected void buildUi() {
        mailUser = SwingUtil.buildTextField(
                i18n("Mail User"), 
                i18n("Normally this is your mail address"), 
                null);
        
        final JComponent mailPassword = 
                new JLabel(i18n("The mail password will be requested when activating the hike."));
        
        receiveMailProtocol = SwingUtil.buildComboBox(
                i18n("Protocol"), 
                i18n("Protocol used for receiving mail"), 
                new String [] { "pop3", "imap" });
        
        receiveMailHost = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Something like 'pop.provider.domain' or 'imap.provider.domain'"), 
                null);
        receiveMailPort = SwingUtil.buildNumberField(
                i18n("Port"), 
                i18n("POP3 uses 110 or 995 (secure), IMAP uses 143 or 993 (secure)"),
                110);
        
        sendMailProtocol = SwingUtil.buildComboBox(
                i18n("Protocol"), 
                i18n("Protocol used for sending mail"), 
                new String [] { "smtp" });
        
        sendMailHost = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Something like 'smtp.provider.domain'"), 
                null);
        sendMailPort = SwingUtil.buildNumberField(
                i18n("Port"), 
                i18n("SMTP uses 25 or 587 (secure)"),
                25);
        
        sendMailFromAccount = SwingUtil.buildTextField(
                i18n("'From' Mail Address"), 
                i18n("Needed only when the mail-user is not a mail-address"), 
                null);
        
        mailPropertiesButton = new JButton(i18n("Edit Custom Properties"));
        mailPropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCustomMailPropertiesDialog();
            }
        });
        mailPropertiesButton.setToolTipText(i18n("Add more mail properties, overriding the basic ones"));
        
        mailTestButton = new JButton(i18n("Test Mail Connection"));
        mailTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionTest();
            }
        });
        mailTestButton.setToolTipText(i18n("Sends a mail to your mailbox, then receives and deletes it"));
        
        errorField = new JLabel();
        errorField.setForeground(Color.RED);
        //errorField.setFont(errorField.getFont().deriveFont(14f));
        
        bindProtocolToPort();
        
        layoutFields(mailPassword);
    }

    @Override
    protected void populateUi(Hike hike) {
        final MailConfiguration mailConfiguration = hike.getAlert().getMailConfiguration();
        
        mailUser.setText(mailConfiguration.getMailUser());
        
        receiveMailProtocol.setSelectedItem(mailConfiguration.getReceiveMailProtocol());
        receiveMailHost.setText(mailConfiguration.getReceiveMailHost());
        receiveMailPort.setValue(mailConfiguration.getReceiveMailPort());

        sendMailProtocol.setSelectedItem(mailConfiguration.getSendMailProtocol());
        sendMailHost.setText(mailConfiguration.getSendMailHost());
        sendMailPort.setValue(mailConfiguration.getSendMailPort());
        sendMailFromAccount.setText(mailConfiguration.getSendMailFromAccount());
        
        installFocusListeners();
    }
    
    private MailConfiguration commitToMailConfiguration() {
        final MailConfiguration mailConfiguration = getData().getHike().getAlert().getMailConfiguration();
        
        mailConfiguration.setMailUser(mailUser.getText());
        
        mailConfiguration.setReceiveMailProtocol((String) receiveMailProtocol.getSelectedItem());
        mailConfiguration.setReceiveMailHost(receiveMailHost.getText());
        mailConfiguration.setReceiveMailPort((int) receiveMailPort.getValue());

        mailConfiguration.setSendMailProtocol((String) sendMailProtocol.getSelectedItem());
        mailConfiguration.setSendMailHost(sendMailHost.getText());
        mailConfiguration.setSendMailPort((int) sendMailPort.getValue());
        mailConfiguration.setSendMailFromAccount(sendMailFromAccount.getText());
        
        return mailConfiguration;
    }

    private void openCustomMailPropertiesDialog() {
        final MailConfiguration mailConfiguration = commitToMailConfiguration();
        final MailProperties coreProperties = new MailProperties(mailConfiguration);
        final Properties customProperties = MailProperties.customProperties(); // is a clone
        
        final CustomPropertiesEditDialog propertiesEditor = new CustomPropertiesEditDialog(
                getFrame(), 
                coreProperties, // rendered read-only
                customProperties, // will be edited
                mailConfiguration.getCustomProperties(), // set include flags for these
                i18n("Mail Properties"));
        propertiesEditor.setVisible(true); // is modal
        
        if (propertiesEditor.wasCommitted())
            mergeCustomProperties(coreProperties, customProperties);
    }
    
    private void mergeCustomProperties(MailProperties mailProperties, Properties customProperties) {
        final MailConfiguration mailConfiguration = getData().getHike().getAlert().getMailConfiguration();
        mailConfiguration.getCustomProperties().clear();
        
        for (Map.Entry<Object,Object> entrySet : customProperties.entrySet()) {
            final List<String> tuple = new ArrayList<>(2);
            tuple.add((String) entrySet.getKey());
            tuple.add((String) entrySet.getValue());
            mailConfiguration.getCustomProperties().add(tuple);
        }
    }
    
    private String validateMailProperties() { // TODO: move validation to mail package!
        final MailConfiguration mailConfiguration = commitToMailConfiguration();
        
        final String mailUser = mailConfiguration.getMailUser();
        if (StringUtil.isEmpty(mailUser))
            return i18n("Mail User is missing!");
        else if (mailUser.contains("@") && MailUtil.isMailAddress(mailUser) == false)
            return i18n("Mail User is not a valid mail address!");
            
        if (StringUtil.isEmpty(mailConfiguration.getReceiveMailHost()))
            return i18n("Receive Host name is missing!");
        if (StringUtil.isEmpty(mailConfiguration.getReceiveMailProtocol()))
            return i18n("Receive Protocol name is missing!");
        if (StringUtil.isEmpty(mailConfiguration.getSendMailHost()))
            return i18n("Send Host name is missing!");
        if (StringUtil.isEmpty(mailConfiguration.getSendMailProtocol()))
            return i18n("Send Protocol name is missing!");
        
        final String mailFromAdress = mailConfiguration.getMailFromAdress();
        if (StringUtil.isNotEmpty(mailFromAdress) && MailUtil.isMailAddress(mailFromAdress) == false)
            return i18n("'From' Mail Adress is not a valid mail address!");

        return null; // all fields are valid!
    }
    
    private void bindProtocolToPort() {
        final ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final String protocol = (String) receiveMailProtocol.getSelectedItem();
                if ("pop3".equals(protocol))
                    receiveMailPort.setValue(110);
                else if ("pop3s".equals(protocol))
                    receiveMailPort.setValue(995);
                else if ("imap".equals(protocol))
                    receiveMailPort.setValue(143);
                else if ("imaps".equals(protocol))
                    receiveMailPort.setValue(993);
            }
        };

        receiveMailProtocol.addItemListener(itemListener);
    }

    private void connectionTest() {
        String error;
        try {
            final ConnectionCheck connectionCheck = 
                    new ConnectionCheck(commitToMailConfiguration(), validAuthenticator);
            final boolean success = connectionCheck.trySendAndReceive();
            error = (success ? null : "For problem please see console!");
            
            validAuthenticator = success ? connectionCheck.getValidAuthenticator() : null;
        }
        catch (MailException e) {
            e.printStackTrace();
            error = e.getMessage();
        }
        
        if (error == null)
            JOptionPane.showMessageDialog(getFrame(), i18n("Connection works!"), 
                    i18n("Success"), JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(getFrame(), error, 
                    i18n("Error"), JOptionPane.ERROR_MESSAGE);
    }

    private void layoutFields(JComponent mailPassword) {
        final JPanel mailUserPanel = new JPanel();
        mailUserPanel.setLayout(new BoxLayout(mailUserPanel, BoxLayout.Y_AXIS));
        mailUserPanel.add(errorField);
        mailUserPanel.add(mailUser);
        mailUserPanel.add(mailPassword);
        mailUserPanel.add(Box.createRigidArea(new Dimension(1, 16)));
        
        final JPanel receivePanel = new JPanel();
        receivePanel.setLayout(new BoxLayout(receivePanel, BoxLayout.Y_AXIS));
        receivePanel.setBorder(BorderFactory.createTitledBorder(i18n("Receive")));
        receivePanel.add(receiveMailHost);
        receivePanel.add(receiveMailProtocol);
        receivePanel.add(receiveMailPort);
        receivePanel.add(Box.createRigidArea(new Dimension(1, 41)));
        
        final JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
        sendPanel.setBorder(BorderFactory.createTitledBorder(i18n("Send")));
        sendPanel.add(sendMailHost);
        sendPanel.add(sendMailProtocol);
        sendPanel.add(sendMailPort);
        sendPanel.add(sendMailFromAccount);
        
        final JPanel sendAndReceive = new JPanel(new GridLayout(1, 2));
        sendAndReceive.add(receivePanel);
        sendAndReceive.add(sendPanel);
        
        final JPanel mailPropertiesButtonPanel = new JPanel();
        mailPropertiesButtonPanel.add(mailPropertiesButton);
        mailPropertiesButtonPanel.add(mailTestButton);
        
        final JPanel all = new JPanel(new BorderLayout());
        all.add(mailUserPanel, BorderLayout.NORTH);
        all.add(sendAndReceive, BorderLayout.CENTER);
        all.add(mailPropertiesButtonPanel, BorderLayout.SOUTH);
        
        setLayout(new GridBagLayout());
        add(all);
    }
    
    private void installFocusListeners() {
        if (focusListenerInstalled == true)
            return;
        
        focusListenerInstalled = true;

        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                final String error = validateMailProperties();
                final boolean valid = (error == null);
                mailPropertiesButton.setEnabled(valid);
                errorField.setText(valid ? "" : error);
                mailPropertiesButton.setEnabled(valid);
                mailTestButton.setEnabled(valid);
            }
        };
        focusListener.focusLost(null); // set initial state
        
        mailUser.addFocusListener(focusListener);
        receiveMailHost.addFocusListener(focusListener);
        receiveMailPort.addFocusListener(focusListener);
        receiveMailProtocol.getEditor().getEditorComponent().addFocusListener(focusListener);
        sendMailHost.addFocusListener(focusListener);
        sendMailPort.addFocusListener(focusListener);
        sendMailProtocol.getEditor().getEditorComponent().addFocusListener(focusListener);
        sendMailFromAccount.addFocusListener(focusListener);
    }

    
    
    private static class CustomPropertiesEditDialog extends PropertiesEditDialog
    {
        private Properties readOnlyProperties;
        private List<List<String>> propertiesToBeMarkedIncluded;
        private boolean committed;
        
        CustomPropertiesEditDialog(
                Frame parent, 
                Properties readOnlyProperties, 
                Properties editableProperties, 
                List<List<String>> propertiesToBeMarkedIncluded,
                String title)
        {
            super(parent, editableProperties, title);
            this.readOnlyProperties = readOnlyProperties;
            this.propertiesToBeMarkedIncluded = propertiesToBeMarkedIncluded;
        }
        
        boolean wasCommitted() {
            return committed;
        }
        
        @Override
        protected Container buildUi() {
            final Container contentPane = super.buildUi();
            setIncludeFlags();
            return contentPane;
        }

        @Override
        protected boolean validateProperties() {
            return committed = super.validateProperties();
        }
        
        private void setIncludeFlags() {
            for (int i = 0; i < namesAndValues.size(); i++) {
                final Vector<Object> triple = namesAndValues.get(i);
                final Object tripleName = triple.get(0);
                for (List<String> tuple : propertiesToBeMarkedIncluded) {
                    final String tupleName = tuple.get(0);
                    if (tripleName.equals(tupleName))
                        triple.set(2, Boolean.TRUE); // set include from false to true
                }
            }
        }
    }
}