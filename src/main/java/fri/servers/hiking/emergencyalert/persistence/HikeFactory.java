package fri.servers.hiking.emergencyalert.persistence;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.persistence.entities.MailConfiguration;

/** 
 * Creation of a new Hike object.
 */
public class HikeFactory
{
    public record Result(Hike hike, IOException jsonException, boolean fileLoaded)
    {
    }
    
    public Result newHike() {
        final Result constructResult = constructHike();
        
        final Hike hike = constructResult.hike();
        final MailConfiguration mailConfiguration = hike.getAlert().getMailConfiguration();
        final List<List<String>> customProperties = mailConfiguration.getCustomProperties();
        
        // load mail property defaults from System.getProperties()
        final Properties systemProperties = System.getProperties();
        for (Object key : systemProperties.keySet()) {
            if (key instanceof String && ((String) key).startsWith("mail.")) {
                final String name = (String) key;
                final String value = systemProperties.getProperty(name);
                
                if (name.equals("mail.user"))
                    mailConfiguration.setMailUser(value);
                else if (name.equals("mail.transport.protocol"))
                    mailConfiguration.setSendMailProtocol(value);
                else if (name.equals("mail.store.protocol"))
                    mailConfiguration.setReceiveMailProtocol(value);
                else if (name.equals("mail.smtp.from") || name.equals("mail.smtps.from"))
                    mailConfiguration.setSendMailFromAccount(value);
                else if (name.startsWith("mail.smtp") && name.endsWith(".host"))
                    mailConfiguration.setSendMailHost(value);
                else if (name.startsWith("mail.smtp") && name.endsWith(".port"))
                    toInt(value, (Integer port) -> mailConfiguration.setSendMailPort(port));
                else if ((name.startsWith("mail.imap") || name.startsWith("mail.pop3")) && name.endsWith(".host"))
                    mailConfiguration.setReceiveMailHost(value);
                else if ((name.startsWith("mail.imap") || name.startsWith("mail.pop3")) && name.endsWith(".port"))
                    toInt(value, (Integer port) -> mailConfiguration.setReceiveMailPort(port));
                else
                    customProperties.add(List.of(name, value));
            }
        }
        
        return constructResult;
    }
    
    private Result constructHike() {
        final String defaultHikeJson = readDefaultHikeJson();
        final Hike hike = new Hike();
        if (defaultHikeJson != null) {
            try {
                final Hike recentHike = new JsonGsonSerializer<Hike>().fromJson(defaultHikeJson, Hike.class);
                hike.setAlert(recentHike.getAlert()); // reuse stored contacts, texts and mail-configuration
            }
            catch (IOException e) {
                System.err.println("Failed to unpack persistent hike: "+e.toString());
                return new Result(hike, e, true);
            }
        }
        return new Result(hike, null, false);
    }
    
    private String readDefaultHikeJson() {
        try {
            return new HikeFileManager().load();
        }
        catch (IOException e) { // ignore missing default file
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    private void toInt(String intValue, Consumer<Integer> consumer) {
        try {
            consumer.accept(Integer.valueOf(intValue));
        }
        catch (NumberFormatException e) {
            System.err.println("Can not convert port to number: "+intValue);
        }
    }
}