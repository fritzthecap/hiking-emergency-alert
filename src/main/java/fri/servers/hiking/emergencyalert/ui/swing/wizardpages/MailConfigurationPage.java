package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.MailConfiguration;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;

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
    
//    mailProps.put("mail.smtp.username", username);
//    mailProps.put("mail.transport.protocol", "smtp");
//    mailProps.put("mail.smtp.from", from);
//    mailProps.put("mail.smtp.auth", "true");
//    mailProps.put("mail.smtp.socketFactory.port", port);
//    mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//    mailProps.put("mail.smtp.socketFactory.fallback", "false");
//    mailProps.put("mail.smtp.starttls.enable", "true");

//    mail.smtp.ssl.enable: Set to "true" to enable SSL for SMTP connections (e.g., port 465). 
//    mail.smtp.port: Use port 465 for SSL or 587 for STARTTLS. 
//    mail.smtp.auth: Set to "true" if authentication is required. 
//    mail.smtp.ssl.protocols: Set to "TLSv1.2" or "TLSv1.3" for modern TLS support (recommended). 
//    mail.smtp.ssl.trust: Set to "*" to trust all servers (use cautiously in production; better to specify trusted hosts). 
//    mail.smtp.socketFactory.class: Use "javax.net.ssl.SSLSocketFactory" when using SSL.
//    mail.smtp.socketFactory.port: Set to the SSL port (e.g., 465). 
//    mail.smtp.socketFactory.fallback: Set to "false" to prevent fallback to non-SSL.
    
//    mail.smtp.connectiontimeout: e.g., 10000 ms
//    mail.smtp.timeout: e.g., 10000 ms
    
//    mail.imaps.host: IMAP server host (e.g., imap.gmail.com)
//    mail.imaps.port: 993
//    mail.imaps.ssl.enable: "true"
//    mail.imaps.ssl.trust: "*" (or specific host)
    
//    mail.pop3s.host: POP3 server host (e.g., pop.gmail.com)
//    mail.pop3s.port: 995
//    mail.pop3s.ssl.enable: "true"
//    mail.pop3s.ssl.trust: "*"
    
    
    @Override
    protected AbstractWizardPage nextPage() {
        return new ContactsPage();
    }
    
    @Override
    protected void buildUi() {
        mailUser = SwingUtil.buildTextField(
                i18n("Mail User"), 
                i18n("Normally this is your mail address"), 
                null);
        
        final JComponent mailPassword = new JLabel(i18n("The mail password will be requested when activating the hike."));
        
        receiveMailProtocol = new JComboBox<>(new String [] { "pop3", "imap" });
        receiveMailProtocol.setEditable(true);
        receiveMailProtocol.setBorder(BorderFactory.createTitledBorder(i18n("Protocol")));
        receiveMailProtocol.setToolTipText(i18n("Protocol used for receiving mail"));
        
        receiveMailHost = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Something like 'pop.provider.domain' or 'imap.provider.domain'"), 
                null);
        
        receiveMailPort = SwingUtil.buildNumberField(
                i18n("Port"), 
                i18n("POP3 uses 110 or 995 (secure), IMAP uses 143 or 993 (secure)"),
                110, 
                "####");
        
        sendMailProtocol = new JComboBox<>(new String [] { "smtp" });
        sendMailProtocol.setEditable(true);
        sendMailProtocol.setBorder(BorderFactory.createTitledBorder(i18n("Protocol")));
        sendMailProtocol.setToolTipText(i18n("Protocol used for sending mail"));
        
        sendMailHost = SwingUtil.buildTextField(
                i18n("Host"), 
                i18n("Something like 'smtp.provider.domain'"), 
                null);
        
        sendMailPort = SwingUtil.buildNumberField(
                i18n("Port"), 
                i18n("SMTP uses 25 or 587 (secure)"),
                25, 
                "####");
        
        sendMailFromAccount = SwingUtil.buildTextField(
                i18n("'From' Mail Address"), 
                i18n("Needed only when the mail-user is not a mail-address"), 
                null);
        
        final JButton mailPropertiesButton = new JButton(i18n("More Properties"));
        mailPropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new RuntimeException("Implement me!");
            }
        });
        
        final JButton mailTestButton = new JButton(i18n("Test Connection"));
        mailTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new RuntimeException("Implement me!");
            }
        });
        
        final JPanel mailUserPanel = new JPanel();
        mailUserPanel.setLayout(new BoxLayout(mailUserPanel, BoxLayout.Y_AXIS));
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
    }
}