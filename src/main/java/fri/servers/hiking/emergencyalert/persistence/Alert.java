package fri.servers.hiking.emergencyalert.persistence;

import java.util.ArrayList;
import java.util.List;

public class Alert
{
    private String helpRequestTitle;
    private String helpRequestText;
    private List<String> procedureTodos = new ArrayList<>();
    private String passingToNextText;
    
    private String nameOfHiker = System.getProperty("user.name");
    private String addressOfHiker;
    private String phoneNumberOfHiker;
    
    private String iso639Language = "en";
    
    private List<Contact> alertContacts = new ArrayList<>();

    private MailConfiguration mailConfiguration = new MailConfiguration();
    
    public String getHelpRequestTitle() {
        return helpRequestTitle;
    }
    public void setHelpRequestTitle(String helpRequestTitle) {
        this.helpRequestTitle = helpRequestTitle;
    }
    public String getHelpRequestText() {
        return helpRequestText;
    }
    public void setHelpRequestText(String helpRequestText) {
        this.helpRequestText = helpRequestText;
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
    
    public List<Contact> getNonAbsentContacts() {
        final List<Contact> nonAbsent = new ArrayList<>();
        for (Contact contact : alertContacts)
            if ( ! contact.isAbsent() )
                nonAbsent.add(contact);
        return nonAbsent;
    }
}