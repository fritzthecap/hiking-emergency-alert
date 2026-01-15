package fri.servers.hiking.emergencyalert.mail;

public class MailException extends Exception
{
    public MailException() {
    }
    public MailException(Exception cause) {
        super(cause);
    }
}