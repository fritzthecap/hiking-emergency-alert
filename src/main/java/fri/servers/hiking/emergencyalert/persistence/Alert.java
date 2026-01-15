package fri.servers.hiking.emergencyalert.persistence;

import java.util.List;

public class Alert
{
    private String helpRequestTitle;
    private String helpRequestText;
    private List<String> procedureTodos;
    private String passingToNextText;
    private String addressOfHiker;
    private Contact hikerContact;
    private MailConfiguration mailConfiguration;
    
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
    public String getAddressOfHiker() {
        return addressOfHiker;
    }
    public void setAddressOfHiker(String addressOfHiker) {
        this.addressOfHiker = addressOfHiker;
    }
    public Contact getHikerContact() {
        return hikerContact;
    }
    public void setHikerContact(Contact hikerContact) {
        this.hikerContact = hikerContact;
    }
    public MailConfiguration getMailConfiguration() {
        return mailConfiguration;
    }
    public void setMailConfiguration(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }
}