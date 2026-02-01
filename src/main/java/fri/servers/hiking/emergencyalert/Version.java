package fri.servers.hiking.emergencyalert;

import java.util.Properties;

/** The version String of this application. */
public class Version
{
    private static String version;
    
    /** @return the version String of this application. */
    public static String get() {
        if (version == null)
            version = readVersionFromMavenPomProperties();
        return version;
    }
    
    private static String readVersionFromMavenPomProperties() {
        final String path = "/version.properties";
        final Properties mavenPomProperties = new Properties();
        try {
            mavenPomProperties.load(Version.class.getResourceAsStream(path));
            return mavenPomProperties.getProperty("version");
        }
        catch (Throwable e) {
            //e.printStackTrace();
            return "-";
        }
    }

}
