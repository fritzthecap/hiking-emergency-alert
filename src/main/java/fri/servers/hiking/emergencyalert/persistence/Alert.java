package fri.servers.hiking.emergencyalert.persistence;

import java.util.ArrayList;
import java.util.List;

public class Alert
{
    private String helpRequestTitle;
    private String helpRequestText;
    private List<String> procedureTodos;
    private String passingToNextText;
    
    private String mailOfHiker;
    private String nameOfHiker;
    private String addressOfHiker;
    
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
}