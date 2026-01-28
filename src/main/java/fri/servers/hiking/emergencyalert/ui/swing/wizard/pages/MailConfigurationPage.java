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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
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
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.util.PropertiesEditDialog;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.StringUtil;
import jakarta.mail.Authenticator;

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

    private Authenticator validAuthenticator;
    
    @Override
    protected String getTitle() {
        return i18n("Mail Connection Configuration");
    }
    
    @Override
    protected void buildUi() {
        mailUserField = SwingUtil.buildTextField(
                i18n("Mail User"), 
                i18n("Normally this is your mail address"), 
                null);
        
        final JComponent mailPassword = 
                new JLabel(i18n("The mail password will be requested when activating the hike."));
        
        receiveMailProtocolField = SwingUtil.buildComboBox(
                i18n("Protocol"), 
                i18n("Protocol to use for reading INBOX mails"), 
                new String [] { "pop3", "imap" });
        receiveMailHostField = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Something like 'pop.provider.domain' or 'imap.provider.domain'"), 
                null);
        receiveMailPortField = SwingUtil.buildNumberField(
                i18n("Port"), 
                i18n("POP3 uses 110 or 995 (secure), IMAP uses 143 or 993 (secure)"),
                110);
        
        sendMailProtocolField = SwingUtil.buildComboBox(
                i18n("Protocol"), 
                i18n("Protocol to use for sending mail"), 
                new String [] { "smtp" });
        sendMailHostField = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Maybe the same as receive host, or something like 'smtp.provider.domain'"), 
                null);
        sendMailPortField = SwingUtil.buildNumberField(
                i18n("Port"), 
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
                connectionTest(true);
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
        
        layoutFields(mailPassword, maximumConnectionTestSecondsLabel, seconds);
        
        bindProtocolToPort();
        
        installFocusListeners();
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
    
    /** When goingForward is true, denies next page when mail connection is not working. */
    @Override
    protected boolean commit(boolean goingForward) {
        if (goingForward) {
            final boolean connectionOk = connectionTest(false); // false: not showing success dialog
            if (connectionOk == false) 
                return false; // connection is not working!
        }
        
        getTrolley().setAuthenticator(validAuthenticator); // for reusing in the StateMachine's Mailer

        commitToMailConfiguration(getHike().getAlert().getMailConfiguration()); // commit to Hike data
        
        try { // silently save before going to route and times
            final String json = new JsonGsonSerializer<Hike>().toJson(getHike());
            if (getTrolley().getHikeFile() == null)
                new HikeFileManager().save(json);
            else
                new HikeFileManager().save(getTrolley().getHikeFile().getAbsolutePath(), json);
        }
        catch (IOException e) {
            System.err.println("ERROR: Could not save base data, error was "+e);
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
        
        if (customPropertiesToCommit != null) {
            mailConfiguration.getCustomProperties().clear();
            
            for (Map.Entry<Object,Object> entrySet : customPropertiesToCommit.entrySet()) {
                final List<String> tuple = new ArrayList<>(2);
                tuple.add((String) entrySet.getKey());
                tuple.add((String) entrySet.getValue());
                
                mailConfiguration.getCustomProperties().add(tuple);
            }
        }

        return mailConfiguration;
    }

    private void openCustomMailPropertiesDialog() {
        final MailConfiguration mailConfigurationForDialog = new MailConfiguration();
        commitToMailConfiguration(mailConfigurationForDialog); // fill from UI fields
        
        final MailProperties coreProperties = new MailProperties(mailConfigurationForDialog);
        final Properties customProperties = MailProperties.customProperties(); // is a clone
        
        final CustomPropertiesEditDialog propertiesEditor = new CustomPropertiesEditDialog(
                getFrame(), 
                coreProperties, // rendered read-only
                customProperties, // will be edited
                mailConfigurationForDialog.getCustomProperties(), // set include flags for these
                i18n("Mail Properties"));
        propertiesEditor.setVisible(true); // is modal
        
        if (propertiesEditor.wasCommitted()) // dialog finished
            this.customPropertiesToCommit = customProperties;
            // edited clone, only those with include-flag will be in customProperties
    }
    
    private boolean connectionTest(boolean showSuccessDialog) {
        final MailConfiguration mailConfiguration = new MailConfiguration();
        commitToMailConfiguration(mailConfiguration);
        
        String error = null;
        setWaitCursor();
        try {
            final ConnectionCheck connectionCheck = new ConnectionCheck(mailConfiguration, validAuthenticator);
            final boolean success = connectionCheck.trySendAndReceive();
            error = (success ? null : i18n("Either send or receive doesn't work, see console for errors."));
            
            validAuthenticator = success ? connectionCheck.getValidAuthenticator() : null;
        }
        catch (MailException e) {
            System.err.println(e.toString());
            error = e.getMessage();
        }
        finally {
            setDefaultCursor();
        }
        
        if (error != null)
            JOptionPane.showMessageDialog(getFrame(), error, 
                    i18n("Error"), JOptionPane.ERROR_MESSAGE);
        else if (showSuccessDialog)
            JOptionPane.showMessageDialog(getFrame(), i18n("Connection works!"), 
                    i18n("Success"), JOptionPane.INFORMATION_MESSAGE);
        
        return (error == null);
    }

    private void layoutFields(JComponent mailPassword, JLabel maximumConnectionTestSecondsLabel, JLabel seconds) {
        final JPanel mailUserPanel = new JPanel(new BorderLayout());
        mailUserPanel.add(mailUserField, BorderLayout.CENTER);
        mailUserPanel.add(mailPassword, BorderLayout.SOUTH);
        
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
        all.add(mailUserPanel, BorderLayout.NORTH);
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

    private void installFocusListeners() {
        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                final boolean valid = validate();
                
                mailPropertiesButton.setEnabled(valid);
                mailTestButton.setEnabled(valid);
            }
        };
        focusListener.focusLost(null); // set initial state
        
        mailUserField.addFocusListener(focusListener);
        receiveMailHostField.addFocusListener(focusListener);
        receiveMailPortField.addFocusListener(focusListener);
        receiveMailProtocolField.getEditor().getEditorComponent().addFocusListener(focusListener);
        sendMailHostField.addFocusListener(focusListener);
        sendMailPortField.addFocusListener(focusListener);
        sendMailProtocolField.getEditor().getEditorComponent().addFocusListener(focusListener);
        sendMailFromAccountField.addFocusListener(focusListener);
        maximumConnectionTestSecondsField.addFocusListener(focusListener);
    }

    
    
    /** Custom mail properties edit dialog. */
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
                for (List<String> tuple : propertiesToBeMarkedIncluded) {
                    final String tupleName = tuple.get(0);
                    if (tripleName.equals(tupleName))
                        triple.set(2, Boolean.TRUE); // set include from false to true
                }
            }
        }
    }
}