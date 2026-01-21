package fri.servers.hiking.emergencyalert.persistence;

public class Contact
{
    private String mailAddress;
    private String firstName;
    private String lastName;
    private boolean absent; // absent contacts will be ignored on mail sending
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
    public boolean isAbsent() {
        return absent;
    }
    public void setAbsent(boolean absent) {
        this.absent = absent;
    }
    public int getDetectionMinutes() {
        return detectionMinutes;
    }
    public void setDetectionMinutes(int detectionMinutes) {
        this.detectionMinutes = detectionMinutes;
    }
}