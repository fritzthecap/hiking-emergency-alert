package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import fri.servers.hiking.emergencyalert.util.Platform;

public class HikeFileManager
{
    private static final String JAVA_NEWLINE = "\n";
    
    private static final String APP_DATA_PATH = 
            System.getProperty("user.home")+File.separatorChar+
            ".hiking-emergency-alert";
    
    private static final String DEFAULT_JSON_FILE = "hike.json";
    private static final String DEFAULT_JSON_FILEPATH = APP_DATA_PATH+File.separatorChar+DEFAULT_JSON_FILE;
    
    public String load() throws IOException {
        return load(DEFAULT_JSON_FILEPATH);
    }
    
    public String load(String jsonFilePath) throws IOException {
        final Path jsonFile = Path.of(jsonFilePath);
        if (Files.isRegularFile(jsonFile)) {
            final String hikeJson = Files.readString(jsonFile, Platform.CHARSET);
            if (JAVA_NEWLINE.equals(Platform.NEWLINE) == false)
                return hikeJson.replace(Platform.NEWLINE, JAVA_NEWLINE);
            return hikeJson;
        }
        return null;
    }
    
    public void save(String hikeJson) throws IOException {
        save(DEFAULT_JSON_FILEPATH, hikeJson);
    }
    
    public void save(String jsonFilePath, String hikeJson) throws IOException {
        final Path path = Path.of(jsonFilePath);
        final Path saveFile;
        if (Files.isDirectory(path)) // a directory was given
            saveFile = Path.of(jsonFilePath, DEFAULT_JSON_FILE);
        else
            saveFile = path;
        
        final Path directory = saveFile.getParent();
        if (Files.isDirectory(directory) == false)
            Files.createDirectories(directory);
        
        if (JAVA_NEWLINE.equals(Platform.NEWLINE) == false)
            hikeJson = hikeJson.replace(JAVA_NEWLINE, Platform.NEWLINE);
        
        Files.writeString(saveFile, hikeJson, Platform.CHARSET, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.TRUNCATE_EXISTING, 
                StandardOpenOption.CREATE);
    }

    /**
     * Delivers the default save path and file name.
     * @return the directory in array[0], the file name in array[1].
     */
    public String[] getSavePathAndFilename() {
        return new String[] { APP_DATA_PATH, DEFAULT_JSON_FILE };
    }

    /** Delivers the default save path, without file-name. */
    public String getSavePath() {
        return APP_DATA_PATH;
    }

    /** Delivers the default save path, including file-name. */
    public String getSaveFile() {
        return DEFAULT_JSON_FILEPATH;
    }

    public String ensureSavePathExists(String directory) {
        final Path path = Path.of(directory);
        if (Files.isDirectory(path) == false)
            try {
                Files.createDirectories(path);
            }
            catch (IOException e) {
                System.err.println("Could not create save-directory "+directory+", error was: "+e);
                return System.getProperty("user.dir");
            }
        return directory;
    }
}