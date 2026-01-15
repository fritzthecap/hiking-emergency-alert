package fri.servers.hiking.emergencyalert.mail;

public class MailReceiveException extends MailException
{
    public MailReceiveException() {
    }
    public MailReceiveException(Exception cause) {
        super(cause);
    }
}