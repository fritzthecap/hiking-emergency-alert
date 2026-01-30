package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import fri.servers.hiking.emergencyalert.mail.Mail;
import fri.servers.hiking.emergencyalert.mail.MailBuilder;
import fri.servers.hiking.emergencyalert.persistence.Alert;
import fri.servers.hiking.emergencyalert.persistence.Contact;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.time.IntervalModel;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Shows all data to user and asks for activation.
 * On window close, ask for saving data to file!
 */
public class ActivationPage extends AbstractWizardPage
{
    private JTextField hikeTimes;
    private JTextArea hikerData;
    private JTextArea contactsAndSendTimes;
    private JTextField alertMailSubject;
    private JTextArea alertMailText;
    private JList<String> attachmentFileNames;
    private JTextField passingToNextMailSubject;
    private JTextArea passingToNextMailText;
    
    @Override
    protected String getTitle() {
        return i18n("Activation Check");
    }
    
    @Override
    protected void buildUi() {
        hikeTimes = SwingUtil.buildTextField(
                i18n("Hike Times"), 
                i18n("Please check your inputs, go back and correct them if wrong"), 
                null);
        SwingUtil.increaseFontSize(Font.BOLD, 14, hikeTimes);
        hikeTimes.setEditable(false);
        hikeTimes.setBackground(Color.WHITE);
        hikeTimes.setHorizontalAlignment(JTextField.CENTER);
        ((TitledBorder) hikeTimes.getBorder()).setTitleJustification(TitledBorder.CENTER);
        
        hikerData = SwingUtil.buildTextArea(i18n("You"));
        
        contactsAndSendTimes = SwingUtil.buildTextArea(null);
        contactsAndSendTimes.setRows(3);
        
        alertMailSubject = SwingUtil.buildTextField(i18n("Alert Mail Subject"), null, null);
        alertMailSubject.setEditable(false);
        alertMailSubject.setBackground(Color.WHITE);
        
        alertMailText = SwingUtil.buildTextArea(null);
        
        attachmentFileNames = new JList<>();
        
        passingToNextMailSubject = SwingUtil.buildTextField(i18n("Continue-to-next Mail Subject"), null, null);
        passingToNextMailSubject.setBackground(Color.WHITE);
        passingToNextMailSubject.setEditable(false);
        
        passingToNextMailText = SwingUtil.buildTextArea(null);
        
        // layout
        
        final JPanel timesAndHikerPanel = new JPanel();
        timesAndHikerPanel.setLayout(new BoxLayout(timesAndHikerPanel, BoxLayout.Y_AXIS));
        
        timesAndHikerPanel.add(hikeTimes);
        
        final JPanel hikerAndContactsPanel = new JPanel(new GridLayout(1, 2));
        hikerAndContactsPanel.add(hikerData);
        hikerAndContactsPanel.add(SwingUtil.buildScrollPane(
                i18n("Planned Alert Times for Contacts"), 
                contactsAndSendTimes));
        timesAndHikerPanel.add(hikerAndContactsPanel);
       
        final JSplitPane alertMailSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        alertMailSplitPane.setResizeWeight(0.8);
        alertMailSplitPane.setOneTouchExpandable(true);
        alertMailSplitPane.setLeftComponent(
                SwingUtil.buildScrollPane(i18n("Alert Mail Text"), alertMailText));
        alertMailSplitPane.setRightComponent(
                SwingUtil.buildScrollPane(i18n("Attachments"), attachmentFileNames));
        
        final JPanel alertMailPanel = new JPanel(new BorderLayout());
        alertMailPanel.add(
                alertMailSplitPane, 
                BorderLayout.CENTER);
        alertMailPanel.add(
                alertMailSubject, 
                BorderLayout.NORTH);
        
        final JPanel passingToNextMailPanel = new JPanel(new BorderLayout());
        passingToNextMailPanel.add(
                passingToNextMailSubject,
                BorderLayout.NORTH);
        passingToNextMailPanel.add(
                SwingUtil.buildScrollPane(i18n("Continue-to-next Mail Text"), passingToNextMailText),
                BorderLayout.CENTER);
        
        final JSplitPane mailsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mailsSplit.setResizeWeight(0.7);
        mailsSplit.setOneTouchExpandable(true);
        mailsSplit.setTopComponent(alertMailPanel);
        mailsSplit.setBottomComponent(passingToNextMailPanel);
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(timesAndHikerPanel, BorderLayout.NORTH);
        contentPanel.add(mailsSplit, BorderLayout.CENTER);

        getContentPanel().add(contentPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final String plannedBegin  = (hike.getPlannedBegin() != null) ? DateUtil.toString(hike.getPlannedBegin()) : "";
        final String plannedEnd = DateUtil.toString(hike.getPlannedHome());
        
        hikeTimes.setText(plannedBegin+"  \u2192  "+plannedEnd);
        
        final Alert alert = hike.getAlert();
        
        hikerData.setText(buildHikerInfos(alert));
        
        contactsAndSendTimes.setText(buildContactsAndTimesInfos(hike));
        
        final Contact firstContact = alert.getAlertContacts().get(0);
        final MailBuilder mailBuilder = new MailBuilder(firstContact, hike);
        final Mail alertMail = mailBuilder.buildAlertMail();
        
        alertMailSubject.setText(alertMail.subject());
        alertMailText.setText(alertMail.text());
        attachmentFileNames.setListData(buildAttachmentsList(alertMail));
        
        final Mail passingToNextMail = mailBuilder.buildPassingToNextMail();
        passingToNextMailSubject.setText(passingToNextMail.subject());
        passingToNextMailText.setText(passingToNextMail.text());
        
        passingToNextMailSubject.setEnabled(alert.isUsePassingToNextMail());
        passingToNextMailText.setEnabled(alert.isUsePassingToNextMail());
        passingToNextMailText.getParent().getParent().setEnabled(alert.isUsePassingToNextMail());
    }
    
    /**
     * Check if times are still valid.
     * When coming back from observation, or this page was open for a while,
     * times could have slipped into past.
     */
    @Override
    protected String validateFields() {
        final Hike hike = getHike();
        return validateHikeTimes(hike.getPlannedBegin(), hike.getPlannedHome());
    }
    
    @Override
    protected boolean commit(boolean goingForward) {
        if (goingForward) {
            final String message = 
                    i18n("Are you sure that you want to start the hike now?");
            final int response = JOptionPane.showConfirmDialog(
                    getFrame(),
                    message,
                    i18n("Confirm Hike Begin"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (response != JOptionPane.YES_OPTION)
                return false;
            
            return askForSaveWhenChanged(i18n("Data were changed"), true);
        }
        return true; // nothing else to commit here
    }

    
    private String buildHikerInfos(Alert alert) {
        final StringBuilder hikerText = new StringBuilder();
        
        if (StringUtil.isNotEmpty(alert.getNameOfHiker()))
            hikerText.append(i18n("Name")+":\t"+alert.getNameOfHiker()+"\n");
        if (StringUtil.isNotEmpty(alert.getAddressOfHiker()))
            hikerText.append(i18n("Address")+":\t"+alert.getAddressOfHiker()+"\n");
        if (StringUtil.isNotEmpty(alert.getPhoneNumberOfHiker()))
            hikerText.append(i18n("Phone")+":\t"+alert.getPhoneNumberOfHiker()+"\n");
        hikerText.append(i18n("Mail")+":\t"+alert.getMailConfiguration().getMailFromAddress());
        
        return hikerText.toString();
    }
    
    private String buildContactsAndTimesInfos(Hike hike) {
        final List<Contact> contacts = hike.getAlert().getNonAbsentContacts();
        Date alertDate = hike.getPlannedHome();
        String currentDay = DateUtil.toDateString(alertDate);
        final StringBuilder contactsText = new StringBuilder(currentDay+"\n"); // first day header
        final IntervalModel intervalModel = new IntervalModel(hike);
        
        for (Contact contact : contacts) {
            final String alertDay = DateUtil.toDateString(alertDate);
            if (currentDay.equals(alertDay) == false) { // write subsequent day header
                currentDay = alertDay;
                contactsText.append(alertDay+"\n");
            }
            
            final String time = DateUtil.toTimeString(alertDate);
            contactsText.append("    "+time+"    "+contact.getMailAddress()+"\n");
            
            alertDate = DateUtil.addMinutes(alertDate, intervalModel.nextIntervalMinutes());
        }
        return contactsText.toString();
    }
    
    private String[] buildAttachmentsList(Mail alertMail) {
        final List<File> attachments = alertMail.attachments();
        final int size = attachments.size();
        if (size <= 0) // JList layout workaround
            return new String[] { " " }; // JList would be VERY wide when empty, despite split resizeWeight
        
        final String[] attachmentNames = new String[size];
        for (int i = 0; i < size; i++)
            attachmentNames[i] = attachments.get(i).getName()+"\n";
        
        return attachmentNames;
    }
}