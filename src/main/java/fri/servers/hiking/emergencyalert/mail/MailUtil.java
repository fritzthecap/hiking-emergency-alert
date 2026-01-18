package fri.servers.hiking.emergencyalert.mail;

import fri.servers.hiking.emergencyalert.util.StringUtil;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public final class MailUtil
{
    public static boolean isMailAddress(String mailAddress) {
        if (StringUtil.isEmpty(mailAddress))
            return false;
        
        try {
            new InternetAddress(mailAddress).validate();
            return true;
        }
        catch (AddressException ex) {
            return false;
        }
    }
    
    private MailUtil() {} // do not instantiate
}