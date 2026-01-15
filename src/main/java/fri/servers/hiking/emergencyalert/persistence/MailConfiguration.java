package fri.servers.hiking.emergencyalert.persistence;

public class MailConfiguration
{
    private String mailUser; // mail.user, required
    
    private String receiveMailProtocol = "pop3"; // or "imap", mail.store.protocol
    private String receiveMailHost; // mail.pop3.host, required
    private int receiveMailPort = 110; // optional, 110 is POP3 default port, 143 is IMAP

    private String sendMailProtocol = "smtp"; // optional, mail.store.protocol
    private String sendMailHost; // mail.smtp.host, required
    private int sendMailPort = 25; // optional, 25 is SMTP default port
    private String sendMailFromAccount; // optional, mail.smtp.from, usually the same as mailUser
    
    /** Usually this is the same as sendMailFromAccount. */
    public String getMailUser() {
        return mailUser;
    }
    /** Usually this is the same as sendMailFromAccount. */
    public void setMailUser(String receiveMailUser) {
        this.mailUser = receiveMailUser;
    }
    
    public String getReceiveMailProtocol() {
        return receiveMailProtocol;
    }
    public void setReceiveMailProtocol(String receiveMailProtocol) {
        this.receiveMailProtocol = receiveMailProtocol;
    }
    public String getReceiveMailHost() {
        return receiveMailHost;
    }
    public void setReceiveMailHost(String receiveMailHost) {
        this.receiveMailHost = receiveMailHost;
    }
    public int getReceiveMailPort() {
        return receiveMailPort;
    }
    public void setReceiveMailPort(int receiveMailPort) {
        this.receiveMailPort = receiveMailPort;
    }
    
    public String getSendMailProtocol() {
        return sendMailProtocol;
    }
    public void setSendMailProtocol(String sendMailProtocol) {
        this.sendMailProtocol = sendMailProtocol;
    }
    public String getSendMailHost() {
        return sendMailHost;
    }
    public void setSendMailHost(String sendMailHost) {
        this.sendMailHost = sendMailHost;
    }
    public int getSendMailPort() {
        return sendMailPort;
    }
    public void setSendMailPort(int sendMailPort) {
        this.sendMailPort = sendMailPort;
    }
    /** Usually this is the same as mailUser. */
    public String getSendMailFromAccount() {
        return sendMailFromAccount;
    }
    /** Usually this is the same as mailUser. */
    public void setSendMailFromAccount(String sendMailFromAccount) {
        this.sendMailFromAccount = sendMailFromAccount;
    }
}