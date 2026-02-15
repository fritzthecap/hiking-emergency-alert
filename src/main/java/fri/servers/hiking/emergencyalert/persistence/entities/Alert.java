package fri.servers.hiking.emergencyalert.persistence.entities;

import java.util.ArrayList;
import java.util.List;

public class Alert
{
    private String helpRequestSubject;
    private String helpRequestIntroduction;
    private List<String> procedureTodos = new ArrayList<>();
    
    private String passingToNextText;
    private boolean usePassingToNextMail = true;
    
    private String nameOfHiker = System.getProperty("user.name");
    private String addressOfHiker;
    private String phoneNumberOfHiker;
    
    private String iso639Language = "en";
    
    private int alertIntervalMinutes = 60; // wait-time until alerting next contact
    private float alertIntervalShrinking = 0.0f; // between 0.0 and 1.0, how the wait-time gets smaller with every alert
    private boolean useContactDetectionMinutes = false; // use minutes of contacts
    
    private int confirmationPollingMinutes = 2; // confirmation polling interval
    
    private List<Contact> alertContacts = new ArrayList<>();

    private MailConfiguration mailConfiguration = new MailConfiguration();
    
    public String getHelpRequestSubject() {
        return helpRequestSubject;
    }
    public void setHelpRequestSubject(String helpRequestSubject) {
        this.helpRequestSubject = helpRequestSubject;
    }
    public String getHelpRequestIntroduction() {
        return helpRequestIntroduction;
    }
    public void setHelpRequestIntroduction(String helpRequestIntroduction) {
        this.helpRequestIntroduction = helpRequestIntroduction;
    }
    public List<String> getProcedureTodos() {
        return procedureTodos;
    }
    public void setProcedureTodos(List<String> procedureTodos) {
        this.procedureTodos = procedureTodos;
    }
    
    public String getPassingToNextText() {
        return passingToNextText;
    }
    public void setPassingToNextText(String passingToNextText) {
        this.passingToNextText = passingToNextText;
    }
    public boolean isUsePassingToNextMail() {
        return usePassingToNextMail;
    }
    public void setUsePassingToNextMail(boolean usePassingToNextMail) {
        this.usePassingToNextMail = usePassingToNextMail;
    }
    
    public String getNameOfHiker() {
        return nameOfHiker;
    }
    public void setNameOfHiker(String nameOfHiker) {
        this.nameOfHiker = nameOfHiker;
    }
    public String getAddressOfHiker() {
        return addressOfHiker;
    }
    public void setAddressOfHiker(String addressOfHiker) {
        this.addressOfHiker = addressOfHiker;
    }
    public String getPhoneNumberOfHiker() {
        return phoneNumberOfHiker;
    }
    public void setPhoneNumberOfHiker(String phoneNumberOfHiker) {
        this.phoneNumberOfHiker = phoneNumberOfHiker;
    }
    
    public String getIso639Language() {
        return iso639Language;
    }
    public void setIso639Language(String iso639Language) {
        this.iso639Language = iso639Language;
    }
    
    public int getAlertIntervalMinutes() {
        return alertIntervalMinutes;
    }
    public void setAlertIntervalMinutes(int messageIntervalMinutes) {
        this.alertIntervalMinutes = messageIntervalMinutes;
    }
    public float getAlertIntervalShrinking() {
        return alertIntervalShrinking;
    }
    public void setAlertIntervalShrinking(float alertIntervalShrinking) {
        this.alertIntervalShrinking = alertIntervalShrinking;
    }
    public boolean isUseContactDetectionMinutes() {
        return useContactDetectionMinutes;
    }
    public void setUseContactDetectionMinutes(boolean useContactDetectionMinutes) {
        this.useContactDetectionMinutes = useContactDetectionMinutes;
    }
    public int getConfirmationPollingMinutes() {
        return confirmationPollingMinutes;
    }
    public void setConfirmationPollingMinutes(int confirmationPollingMinutes) {
        this.confirmationPollingMinutes = confirmationPollingMinutes;
    }
    
    public MailConfiguration getMailConfiguration() {
        return mailConfiguration;
    }
    public void setMailConfiguration(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }
    
    public List<Contact> getAlertContacts() {
        return alertContacts;
    }
    public void setAlertContacts(List<Contact> alertContacts) {
        this.alertContacts = alertContacts;
    }
    
    
    /** Convenience method that delivers a list of contacts that are not marked as absent. */
    public List<Contact> getNonAbsentContacts() {
        final List<Contact> nonAbsent = new ArrayList<>();
        for (Contact contact : alertContacts)
            if ( ! contact.isAbsent() )
                nonAbsent.add(contact);
        return nonAbsent;
    }
}