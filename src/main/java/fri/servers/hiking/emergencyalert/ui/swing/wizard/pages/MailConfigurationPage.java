package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
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
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.util.PropertiesEditDialog;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Configure mail send- and receive-connection.
 * <p/>
 * Securing mail:
 * <ol>
 * <li>Securing Email Transmission (Send/Receive) 
 * <ul><li>
 * Use TLS/SSL: Ensure your mail client/server uses 
 * TLS 1.2 or higher for both SMTP (sending) and 
 * IMAP/POP (receiving) to encrypt data between servers.
 * </li><li>
 * Configure Mail Clients: In Outlook/Gmail, 
 * ensure settings for incoming and outgoing servers 
 * are set to "SSL/TLS" rather than "None" or "STARTTLS" 
 * (if a higher encryption option is available). 
 * </li>
 * </ul>
 * </ol>
 * Die Mailserver Konfiguration ist auf Android phones zu finden unter:
 * <blockquote>
 *     Einstellungen -> Apps -> E-Mail ->
 *     E-Mail-Einstellungen -> 
 *     Tippe auf das Konto (Mail-Adresse) ->
 *     Ganz hinunter scrollen zu Servereinstellungen -> 
 *     Eingangssserver, Ausgangsserver.
 * </blockquote>
 * Die Ports nur übernehmen, wenn SSL-Zertifikate des Mail-Providers
 * auf dem Computer installiert wurden!
 */
public class MailConfigurationPage extends AbstractWizardPage
{
    private JTextField mailUserField; // mail.user, required
    
    private JComboBox<String> receiveMailProtocolField; //"pop3" or "imap", mail.store.protocol
    private JTextField receiveMailHostField; // mail.pop3.host, required
    private JFormattedTextField receiveMailPortField; // optional, 110 is POP3 default port, 143 is IMAP

    private JComboBox<String> sendMailProtocolField; // "smtp", optional, mail.store.protocol
    private JTextField sendMailHostField; // mail.smtp.host, required
    private JFormattedTextField sendMailPortField; // optional, 25 is SMTP default port, or 587
    private JTextField sendMailFromAccountField; // optional, mail.smtp.from, usually the same as mailUser
    
    private JButton mailPropertiesButton;
    private JButton mailTestButton;
    private JFormattedTextField maximumConnectionTestSecondsField;
    
    private Properties customPropertiesToCommit;

    @Override
    protected String getTitle() {
        return i18n("Mail Connection Configuration");
    }
    
    @Override
    protected void buildUi() {
        mailUserField = SwingUtil.buildTextField(
                "* "+i18n("Mail User"), 
                i18n("Normally this is your mail address"), 
                null);
        
        receiveMailProtocolField = SwingUtil.buildComboBox(
                "* "+i18n("Protocol"), 
                i18n("Protocol to use for reading INBOX mails"), 
                new String [] { "pop3", "imap" });
        receiveMailHostField = SwingUtil.buildTextField(
                "* "+i18n("Host"), 
                i18n("Something like 'pop.provider.domain' or 'imap.provider.domain'"), 
                null);
        receiveMailPortField = SwingUtil.buildNumberField(
                "* "+i18n("Port"), 
                i18n("POP3 uses 110 or 995 (secure), IMAP uses 143 or 993 (secure)"),
                110);
        
        sendMailProtocolField = SwingUtil.buildComboBox(
                "* "+i18n("Protocol"), 
                i18n("Protocol to use for sending mail"), 
                new String [] { "smtp" });
        sendMailHostField = SwingUtil.buildTextField(
                "* "+i18n("Host"), 
                i18n("Maybe the same as receive host, or something like 'smtp.provider.domain'"), 
                null);
        sendMailPortField = SwingUtil.buildNumberField(
                "* "+i18n("Port"), 
                i18n("SMTP uses 25 or 587 (secure)"),
                25);
        sendMailFromAccountField = SwingUtil.buildTextField(
                i18n("'From' Mail Address"), 
                i18n("Needed only when the mail-user is not a mail-address"), 
                null);
        
        mailPropertiesButton = new JButton(i18n("More Properties"));
        mailPropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCustomMailPropertiesDialog();
            }
        });
        mailPropertiesButton.setToolTipText(i18n("Add more mail properties, overriding the basic ones"));
        
        mailTestButton = new JButton(i18n("Test Connection"));
        mailTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setWaitCursor();
                try {
                    connectionTest(true);
                }
                finally {
                    setDefaultCursor();
                }
            }
        });
        mailTestButton.setToolTipText(i18n("Sends a mail to your mailbox, then receives and deletes it"));
        
        final JLabel maximumConnectionTestSecondsLabel = new JLabel(i18n("Maximum Connection Wait"));
        final JLabel seconds = new JLabel(i18n("Seconds"));
        maximumConnectionTestSecondsField = SwingUtil.buildNumberField(
                null, 
                i18n("The maximum number of seconds to wait on mail connection test"),
                6);
        maximumConnectionTestSecondsField.setColumns(3);
        
        layoutFields(maximumConnectionTestSecondsLabel, seconds);
        
        bindProtocolToPort();
        
        installFocusValidation();
    }

    @Override
    protected void populateUi(Hike hike) {
        final MailConfiguration mailConfiguration = hike.getAlert().getMailConfiguration();
        
        mailUserField.setText(mailConfiguration.getMailUser());
        
        receiveMailProtocolField.setSelectedItem(mailConfiguration.getReceiveMailProtocol());
        receiveMailHostField.setText(mailConfiguration.getReceiveMailHost());
        receiveMailPortField.setValue(mailConfiguration.getReceiveMailPort());

        sendMailProtocolField.setSelectedItem(mailConfiguration.getSendMailProtocol());
        sendMailHostField.setText(mailConfiguration.getSendMailHost());
        sendMailPortField.setValue(mailConfiguration.getSendMailPort());
        sendMailFromAccountField.setText(mailConfiguration.getSendMailFromAccount());
        
        maximumConnectionTestSecondsField.setValue(mailConfiguration.getMaximumConnectionTestSeconds());
    }
    
    @Override
    protected String validateFields() {
        final String mailUser = mailUserField.getText();
        if (StringUtil.isEmpty(mailUser))
            return i18n("Mail User is missing!");
        
        if (mailUser.contains("@") && MailUtil.isMailAddress(mailUser) == false)
            return i18n("Mail User seems to be a mail address but is not valid!");
            
        if (StringUtil.isEmpty(receiveMailHostField.getText()))
            return i18n("Receive Host name is missing!");
        
        if (StringUtil.isEmpty((String) receiveMailProtocolField.getSelectedItem()))
            return i18n("Receive Protocol name is missing!");
        
        if (SwingUtil.getNumberValue(receiveMailPortField) <= 0)
            return i18n("Receive Port number is missing!");
        
        if (StringUtil.isEmpty(sendMailHostField.getText()))
            return i18n("Send Host name is missing!");
        
        if (StringUtil.isEmpty((String) sendMailProtocolField.getSelectedItem()))
            return i18n("Send Protocol name is missing!");
        
        if (SwingUtil.getNumberValue(sendMailPortField) <= 0)
            return i18n("Send Port number is missing!");
        
        final String sendMailFromAccount = sendMailFromAccountField.getText();
        if (StringUtil.isNotEmpty(sendMailFromAccount) && MailUtil.isMailAddress(sendMailFromAccount) == false)
            return i18n("'From' Mail Address is not a valid mail address!");

        if (MailUtil.isMailAddress(sendMailFromAccount) == false && MailUtil.isMailAddress(mailUser) == false)
            return i18n("Either Mail User or 'From' Address must be a mail address!");
        
        return null; // all fields are valid!
    }
    
    /** If goingForward is true, denies next page when mail connection is not working. */
    @Override
    protected boolean commit(boolean goingForward) {
        if (goingForward) {
            final boolean connectionOk = connectionTest(false); // false: not showing success dialog
            if (connectionOk == false) 
                return false; // connection is not working!
        }
        
        commitToMailConfiguration(getHike().getAlert().getMailConfiguration()); // commit to Hike data
        
        if (goingForward) {
            try { // silently save before going to route/times page
                getTrolley().save(getHike());
            }
            catch (Exception e) {
                System.err.println("ERROR: Could not save base data, error was "+e);
            }
        }

        return true;
    }
    
    
    private MailConfiguration commitToMailConfiguration(MailConfiguration mailConfiguration) {
        if (StringUtil.isNotEmpty(mailUserField.getText()))
            mailConfiguration.setMailUser(mailUserField.getText());
        
        mailConfiguration.setReceiveMailProtocol((String) receiveMailProtocolField.getSelectedItem());
        
        if (StringUtil.isNotEmpty((String) receiveMailHostField.getText()))
            mailConfiguration.setReceiveMailHost(receiveMailHostField.getText());
        
        final int receivePort = SwingUtil.getNumberValue(receiveMailPortField);
        if (receivePort > 0)
            mailConfiguration.setReceiveMailPort(receivePort);

        mailConfiguration.setSendMailProtocol((String) sendMailProtocolField.getSelectedItem());
        
        if (StringUtil.isNotEmpty(sendMailHostField.getText()))
            mailConfiguration.setSendMailHost(sendMailHostField.getText());
        
        final int sendPort = SwingUtil.getNumberValue(sendMailPortField);
        if (sendPort > 0)
            mailConfiguration.setSendMailPort(sendPort);
        
        if (StringUtil.isNotEmpty(sendMailFromAccountField.getText()))
            mailConfiguration.setSendMailFromAccount(sendMailFromAccountField.getText());
        
        final int maximumConnectionTestSeconds = SwingUtil.getNumberValue(maximumConnectionTestSecondsField);
        if (maximumConnectionTestSeconds > 0)
            mailConfiguration.setMaximumConnectionTestSeconds(maximumConnectionTestSeconds);
        
        if (customPropertiesToCommit != null) { // dialog edited custom properties
            mailConfiguration.getCustomProperties().clear();
            
            for (Map.Entry<Object,Object> entrySet : customPropertiesToCommit.entrySet()) {
                final List<String> tuple = new ArrayList<>(2);
                tuple.add((String) entrySet.getKey());
                tuple.add((String) entrySet.getValue());
                
                mailConfiguration.getCustomProperties().add(tuple);
            }
        }
        else { // dialog did no changes to custom properties
            mailConfiguration.setCustomProperties(
                    getHike().getAlert().getMailConfiguration().getCustomProperties());
        }

        return mailConfiguration;
    }

    private void openCustomMailPropertiesDialog() {
        final MailConfiguration mailConfigurationForDialog = new MailConfiguration();
        commitToMailConfiguration(mailConfigurationForDialog); // fill from UI fields
        
        final MailProperties coreProperties = new MailProperties(mailConfigurationForDialog); // no custom properties
        
        final List<List<String>> persistentCustomProperties = getHike().getAlert().getMailConfiguration().getCustomProperties();
        
        // merge all possible properties together
        final Properties customProperties = mergePropertiesOverDefaults(
                persistentCustomProperties,
                this.customPropertiesToCommit, 
                MailProperties.customProperties()); // is an always new clone
        
        // merge included properties
        final Properties propertiesToInclude = mergePropertiesWhenCommitable(
                persistentCustomProperties, // they were included
                this.customPropertiesToCommit); // uncommitted included
        final Properties customPropertiesBackup = (Properties) propertiesToInclude.clone(); // change tracking
        
        final CustomPropertiesEditDialog propertiesEditor = new CustomPropertiesEditDialog(
                getFrame(), 
                coreProperties, // rendered read-only on top
                customProperties, // will be editable below
                propertiesToInclude, // set include flags for editable
                i18n("Mail Properties"));
        propertiesEditor.setVisible(true); // is modal
        
        // dialog finished
        if (propertiesEditor.wasCommitted() && customPropertiesBackup.equals(customProperties) == false)
            // edited clone, only those with include-flag will be in customProperties
            this.customPropertiesToCommit = customProperties;
    }
    
    private Properties mergePropertiesOverDefaults(
            List<List<String>> persistentCustomProperties,
            Properties customPropertiesToCommit, 
            Properties defautCustomProperties)
    {
        if (persistentCustomProperties != null) // adopt persistent property values
            for (List<String> tuple : persistentCustomProperties)
                defautCustomProperties.setProperty(tuple.get(0), tuple.get(1));
        
        if (customPropertiesToCommit != null) // uncommitted properties overwrite persistet values
            for (Map.Entry<Object,Object> entry : customPropertiesToCommit.entrySet())
                defautCustomProperties.setProperty((String) entry.getKey(), (String) entry.getValue());
        
        return defautCustomProperties;
    }

    private Properties mergePropertiesWhenCommitable(
            List<List<String>> persistentCustomProperties,
            Properties customPropertiesToCommit)
    {
        final Properties properties = new Properties(customPropertiesToCommit);
        
        if (persistentCustomProperties != null) // adopt persistent property values
            for (List<String> tuple : persistentCustomProperties)
                if (customPropertiesToCommit == null || customPropertiesToCommit.containsKey(tuple.get(0)))
                    properties.setProperty(tuple.get(0), tuple.get(1));
        
        return properties;
    }

    private boolean connectionTest(boolean showSuccessDialog) {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        commitToMailConfiguration(mailConfiguration);
        
        String error = null;
        try {
            final ConnectionCheck connectionCheck = 
                    new ConnectionCheck(mailConfiguration, getTrolley().getAuthenticator());
            
            final boolean success = connectionCheck.trySendAndReceive();
            
            if (success) // reuse it in the StateMachine's Mailer
                getTrolley().setAuthenticator(connectionCheck.getValidAuthenticator()); 
            else
                error = i18n("Either send or receive doesn't work, see console for errors.");
        }
        catch (MailException e) {
            System.err.println(e.toString());
            error = e.getMessage();
        }
        
        if (error != null)
            JOptionPane.showMessageDialog(getFrame(), error, 
                    i18n("Error"), JOptionPane.ERROR_MESSAGE);
        else if (showSuccessDialog)
            JOptionPane.showMessageDialog(getFrame(), i18n("Connection works!"), 
                    i18n("Success"), JOptionPane.INFORMATION_MESSAGE);
        
        return (error == null);
    }

    private void layoutFields(JLabel maximumConnectionTestSecondsLabel, JLabel seconds) {
        final JPanel receivePanel = new JPanel();
        receivePanel.setLayout(new BoxLayout(receivePanel, BoxLayout.Y_AXIS));
        receivePanel.setBorder(BorderFactory.createTitledBorder(i18n("Receive")));
        receivePanel.add(receiveMailHostField);
        receivePanel.add(receiveMailProtocolField);
        receivePanel.add(receiveMailPortField);
        receivePanel.add(Box.createRigidArea(new Dimension(1, 41)));
        
        final JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
        sendPanel.setBorder(BorderFactory.createTitledBorder(i18n("Send")));
        sendPanel.add(sendMailHostField);
        sendPanel.add(sendMailProtocolField);
        sendPanel.add(sendMailPortField);
        sendPanel.add(sendMailFromAccountField);
        
        final JPanel sendAndReceive = new JPanel(new GridLayout(1, 2));
        sendAndReceive.add(receivePanel);
        sendAndReceive.add(sendPanel);
        
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(mailTestButton);
        buttonsPanel.add(mailPropertiesButton);
        final JPanel maximumSecondsPanel = new JPanel();
        maximumSecondsPanel.add(maximumConnectionTestSecondsLabel);
        maximumSecondsPanel.add(maximumConnectionTestSecondsField);
        maximumSecondsPanel.add(seconds);
        final JPanel southPanel = new JPanel(new GridLayout(2, 1));
        southPanel.add(buttonsPanel);
        southPanel.add(maximumSecondsPanel);
        
        final JPanel all = new JPanel(new BorderLayout());
        all.add(mailUserField, BorderLayout.NORTH);
        all.add(sendAndReceive, BorderLayout.CENTER);
        all.add(southPanel, BorderLayout.SOUTH);
        
        getContentPanel().setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPanel().add(all);
    }
    
    private void bindProtocolToPort() {
        final ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final String protocol = (String) receiveMailProtocolField.getSelectedItem();
                if ("pop3".equals(protocol))
                    receiveMailPortField.setValue(110);
                else if ("pop3s".equals(protocol))
                    receiveMailPortField.setValue(995);
                else if ("imap".equals(protocol))
                    receiveMailPortField.setValue(143);
                else if ("imaps".equals(protocol))
                    receiveMailPortField.setValue(993);
            }
        };

        receiveMailProtocolField.addItemListener(itemListener);
    }

    private void installFocusValidation() {
        final JComponent[] focusComponents = new JComponent[] {
                mailUserField,
                receiveMailHostField,
                receiveMailPortField,
                (JComponent) receiveMailProtocolField.getEditor().getEditorComponent(),
                sendMailHostField,
                sendMailPortField,
                (JComponent) sendMailProtocolField.getEditor().getEditorComponent(),
                sendMailFromAccountField,
                maximumConnectionTestSecondsField,
        };
        final Consumer<Boolean> afterValidate = (valid) -> {
            mailPropertiesButton.setEnabled(valid);
            mailTestButton.setEnabled(valid);
        };
        installFocusListener(focusComponents, afterValidate);
    }

    
    
    /** Custom mail properties edit dialog. */
    private static class CustomPropertiesEditDialog extends PropertiesEditDialog
    {
        private final Properties readOnlyProperties;
        private final Set<String> propertiesToInclude;
        private boolean committed;
        
        CustomPropertiesEditDialog(
                Frame parent, 
                Properties readOnlyProperties, 
                Properties editableProperties, 
                Properties propertiesToInclude,
                String title)
        {
            super(parent, editableProperties, title);
            this.readOnlyProperties = readOnlyProperties;
            this.propertiesToInclude = propertiesToInclude.stringPropertyNames();
        }
        
        boolean wasCommitted() {
            return committed;
        }
        
        @Override
        protected Container buildUi() {
            final Container contentPane = super.buildUi();
            
            // add core properties table to top
            final JComponent readOnlyTable = buildReadOnlyTable(readOnlyProperties);
            readOnlyTable.setBorder(BorderFactory.createTitledBorder(i18n("Core Properties")));
            contentPane.add(readOnlyTable, BorderLayout.NORTH);
            
            ((JComponent) table.getParent().getParent()).setBorder(BorderFactory.createTitledBorder(i18n("Editable Custom Properties")));

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
                if (propertiesToInclude.contains(tripleName))
                    triple.set(2, Boolean.TRUE); // set include-flag from false to true
            }
        }
    }
}