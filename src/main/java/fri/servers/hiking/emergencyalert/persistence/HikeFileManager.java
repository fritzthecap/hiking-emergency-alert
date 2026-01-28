package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import fri.servers.hiking.emergencyalert.util.Platform;

/**
 * Files and directories to save and load hikes.
 */
public class HikeFileManager
{
    private static final String JAVA_NEWLINE = "\n";
    
    private static final String DEFAULT_JSON_PATH;
    private static final String DEFAULT_JSON_FILE;
    private static final String DEFAULT_JSON_FILEPATH;
    
    static {
        final String fileName = System.getProperty("hike.file", "hike.json");
        final boolean invalidFileProperty = (true == fileName.contains(File.separator));
        if (invalidFileProperty) {
            System.err.println("ERROR: System property 'hike.file' must NOT contain a file-separator character!");
            DEFAULT_JSON_FILE = "hike.json";
        }
        else {
            
            DEFAULT_JSON_FILE = fileName + (fileName.toLowerCase().endsWith(".json") ? "" : ".json");
        }
        
        final String directory = System.getProperty("hike.home");
        final boolean propertyEmpty = (directory == null);
        final boolean invalidDirectoryProperty = (false == propertyEmpty && false == directory.contains(File.separator));
        if (propertyEmpty || invalidDirectoryProperty) {
            if (invalidDirectoryProperty)
                System.err.println("ERROR: System property 'hike.home' MUST contain a file-separator character!");
            DEFAULT_JSON_PATH = System.getProperty("user.home") + File.separator + "hiking-emergency-alert";
        }
        else {
            DEFAULT_JSON_PATH = directory;
        }
        
        DEFAULT_JSON_FILEPATH = DEFAULT_JSON_PATH + File.separatorChar + DEFAULT_JSON_FILE;
        
        System.err.println("Default JSON file name is '"+DEFAULT_JSON_FILE+"'");
        System.err.println("Default JSON directory is '"+DEFAULT_JSON_PATH+"'");
    }
    
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

    /** Delivers the default save directory, excluding file-name. */
    public String getSavePath() {
        return DEFAULT_JSON_PATH;
    }

    /** Delivers the default save path, including file-name. */
    public String getSavePathFile() {
        return DEFAULT_JSON_FILEPATH;
    }

    /** Delivers the default save file-name, excluding directory. */
    public String getSaveFilename() {
        return DEFAULT_JSON_FILE;
    }

    /**
     * Creates given directory when not existing.
     * @return null when error, else the given directory.
     */
    public String ensurePathExists(String directory) {
        final Path path = Path.of(directory);
        if (Files.isDirectory(path))
            return directory;
        
        try {
            Files.createDirectories(path);
            return directory;
        }
        catch (IOException e) {
            System.err.println("ERROR: Could not create directory "+directory+", error was: "+e);
            return null;
        }
    }

    /** @return true when there are no files in default save-directory. */
    public boolean isSavePathEmpty() {
        final Path path = Path.of(DEFAULT_JSON_PATH);
        if (Files.isDirectory(path) == false)
            return true; // not existing, thus empty
        
        try (Stream<Path> fileStream = Files.list(path)) {
            return (fileStream.count() <= 0L);
        }
        catch (IOException e) {
            System.err.println("ERROR: Directory "+DEFAULT_JSON_PATH+" could not be listed, error was: "+e);
            return true;
        }
    }
}