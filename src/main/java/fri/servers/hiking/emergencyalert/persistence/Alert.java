package fri.servers.hiking.emergencyalert.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Alert
{
    private String helpRequestTitle;
    private String helpRequestText;
    private List<String> procedureTodos = new ArrayList<>();
    private String passingToNextText;
    
    private String mailOfHiker;
    private String nameOfHiker;
    private String addressOfHiker;
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
    public String getMailOfHiker() {
        return mailOfHiker;
    }
    public void setMailOfHiker(String mailOfHiker) {
        this.mailOfHiker = mailOfHiker;
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
    
    public Alert copy() {
        final Alert alert = new Alert();
        alert.setHelpRequestTitle(getHelpRequestTitle());
        alert.setHelpRequestText(getHelpRequestText());
        alert.setProcedureTodos(new ArrayList<>(getProcedureTodos()));
        alert.setPassingToNextText(getPassingToNextText());
        alert.setMailOfHiker(getMailOfHiker());
        alert.setNameOfHiker(getNameOfHiker());
        alert.setAddressOfHiker(getAddressOfHiker());
        alert.setIso639Language(getIso639Language());
        
        alert.setMailConfiguration(getMailConfiguration().copy());
        
        final List<Contact> alertContacts = new ArrayList<>();
        for (Contact contact : getAlertContacts())
            alertContacts.add(contact.copy());
        alert.setAlertContacts(alertContacts);
        
        return alert;
    }
    
    public boolean isEqual(Alert alert) {
        return
            Objects.equals(alert.getHelpRequestTitle(), getHelpRequestTitle()) &&
            Objects.equals(alert.getHelpRequestText(), getHelpRequestText()) &&
            Objects.equals(alert.getProcedureTodos(), getProcedureTodos()) &&
            Objects.equals(alert.getPassingToNextText(), getPassingToNextText()) &&
            Objects.equals(alert.getMailOfHiker(), getMailOfHiker()) &&
            Objects.equals(alert.getNameOfHiker(), getNameOfHiker()) &&
            Objects.equals(alert.getAddressOfHiker(), getAddressOfHiker()) &&
            Objects.equals(alert.getIso639Language(), getIso639Language()) &&
            alert.getMailConfiguration().isEqual(getMailConfiguration()) &&
            isArrayEqual(alert.getAlertContacts(), getAlertContacts());
    }
    
    private boolean isArrayEqual(List<Contact> alertContacts1, List<Contact> alertContacts2) {
        if (alertContacts1.size() != alertContacts2.size())
            return false;
        
        for (int i = 0; i < alertContacts1.size(); i++) {
            final Contact contact1 = alertContacts1.get(i);
            final Contact contact2 = alertContacts2.get(i);
            if (contact1.isEqual(contact2) == false)
                return false;
        }
        return true;
    }
}