package fri.servers.hiking.emergencyalert.persistence.entities;

public class Contact
{
    private String mailAddress;
    private String firstName;
    private String lastName;
    private boolean absent = false; // absent contacts will be ignored on mail sending
    private int detectionMinutes = Alert.DEFAULT_ALERT_INTERVAL_MINUTES; // how long this person needs to detect a mail
    private boolean needsProcedure = true; // whether "steps-to-be-taken" should be contained in mail
    
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
    public boolean isNeedsProcedure() {
        return needsProcedure;
    }
    public void setNeedsProcedure(boolean needsProcedure) {
        this.needsProcedure = needsProcedure;
    }
}