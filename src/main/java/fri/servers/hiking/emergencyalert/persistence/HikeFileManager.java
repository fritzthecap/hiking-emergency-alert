package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import fri.servers.hiking.emergencyalert.util.Platform;

public class HikeFileManager
{
    private static final String APP_DATA_PATH = 
            System.getProperty("user.dir")+File.separatorChar+
            "hiking-emergency-alert"+File.separatorChar;
    
    private static final String DEFAULT_JSON_FILE = APP_DATA_PATH+"hike.json";
    
    public String load() throws IOException {
        return load(DEFAULT_JSON_FILE);
    }
    
    public String load(String jsonFilePath) throws IOException {
        final Path jsonFile = Path.of(jsonFilePath);
        if (Files.isRegularFile(jsonFile))
            return Files.readString(jsonFile, Platform.CHARSET).replace(Platform.NEWLINE, "\n");
        return null;
    }

}
