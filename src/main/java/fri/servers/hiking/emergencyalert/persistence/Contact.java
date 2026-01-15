package fri.servers.hiking.emergencyalert.persistence;

import java.util.List;

public class Contact
{
    private String mailAddress;
    private String firstName;
    private String lastName;
    private Boolean absent = Boolean.FALSE;
    private List<Contact> alertContacts; // children
    
    public String getMailAddress() {
        return mailAddress;
    }
    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public List<Contact> getAlertContacts() {
        return alertContacts;
    }
    public void setAlertContacts(List<Contact> alertContacts) {
        this.alertContacts = alertContacts;
    }
    public Boolean isAbsent() {
        return absent;
    }
    public void setAbsent(Boolean absent) {
        this.absent = absent;
    }
}