package fri.servers.hiking.emergencyalert.persistence;

import java.util.Objects;

public class Contact
{
    private String mailAddress;
    private String firstName;
    private String lastName;
    private Boolean absent = Boolean.FALSE; // will be ignored on mail sending
    private int detectionMinutes = 60; // how long this person needs to detect a mail
    
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
    public Boolean isAbsent() {
        return absent;
    }
    public void setAbsent(Boolean absent) {
        this.absent = absent;
    }
    public int getDetectionMinutes() {
        return detectionMinutes;
    }
    public void setDetectionMinutes(int detectionMinutes) {
        this.detectionMinutes = detectionMinutes;
    }
    
    public Contact copy() {
        final Contact contact = new Contact();
        contact.setMailAddress(getMailAddress());
        contact.setFirstName(getFirstName());
        contact.setLastName(getLastName());
        contact.setAbsent(isAbsent());
        contact.setDetectionMinutes(getDetectionMinutes());
        return contact;
    }
    
    public boolean isEqual(Contact contact) {
        return 
            Objects.equals(contact.getMailAddress(), getMailAddress()) && 
            Objects.equals(contact.getFirstName(), getFirstName()) && 
            Objects.equals(contact.getLastName(), getLastName()) && 
            Objects.equals(contact.isAbsent(), isAbsent()) && 
            contact.getDetectionMinutes() == getDetectionMinutes(); 
    }
}