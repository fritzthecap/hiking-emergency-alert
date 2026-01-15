package fri.servers.hiking.emergencyalert.mail;

public class MailSendException extends MailException
{
    public MailSendException() {
    }
    public MailSendException(Exception cause) {
        super(cause);
    }
}