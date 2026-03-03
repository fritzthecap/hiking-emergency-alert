package fri.servers.hiking.emergencyalert.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import fri.servers.hiking.emergencyalert.util.Platform;
import fri.servers.hiking.emergencyalert.util.StringUtil;

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
        final String directoryProperty = "hike.home";
        final String configuredDirectory = System.getProperty(directoryProperty);
        final boolean propertyEmpty = StringUtil.isEmpty(configuredDirectory);
        final boolean invalidDirectoryProperty = (false == propertyEmpty && false == configuredDirectory.contains(File.separator));
        if (propertyEmpty || invalidDirectoryProperty) {
            if (invalidDirectoryProperty)
                System.err.println("ERROR: System property '"+directoryProperty+"' MUST contain a file-separator!");
            DEFAULT_JSON_PATH = joinPathParts(System.getProperty("user.home"), "hiking-emergency-alert");
        }
        else {
            DEFAULT_JSON_PATH = configuredDirectory;
        }
        
        final String fileProperty = "hike.file";
        final String defaultFile = "hike.json";
        final String configuredFile = System.getProperty(fileProperty, defaultFile); // property name, default value
        final boolean invalidFileProperty = (true == configuredFile.contains(File.separator));
        if (invalidFileProperty) {
            System.err.println("ERROR: System property '"+fileProperty+"' must NOT contain a file-separator!");
            DEFAULT_JSON_FILE = defaultFile;
        }
        else {
            DEFAULT_JSON_FILE = configuredFile + (configuredFile.toLowerCase().endsWith(".json") ? "" : ".json");
        }
        
        DEFAULT_JSON_FILEPATH = joinPathParts(DEFAULT_JSON_PATH, DEFAULT_JSON_FILE);
        
        System.out.println("Default JSON directory (property '"+directoryProperty+"') is '"+DEFAULT_JSON_PATH+"'");
        System.out.println("Default JSON filename (property '"+fileProperty+"') is '"+DEFAULT_JSON_FILE+"'");
    }
    
    private static String joinPathParts(String part1, String part2) {
        return part1 + (part1.endsWith(File.separator) ? "" : File.separator) + part2;
    }
    
    public String load() throws IOException {
        return load(DEFAULT_JSON_FILEPATH);
    }
    
    public String load(String jsonFilePath) throws IOException {
        final Path jsonFile = Path.of(jsonFilePath);
        if (Files.isRegularFile(jsonFile)) {
            final String hikeJson = Files.readString(jsonFile, Platform.CHARSET);
            System.out.println("Loaded hike from file "+jsonFile);
            
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
        
        System.out.println("Saved hike to file "+saveFile);
    }

    /** Delivers the default save directory, excluding file-name. */
    public String getSavePath() {
        final Path savePath = Path.of(DEFAULT_JSON_PATH);
        if (Files.isDirectory(savePath) == false)
            try {
                Files.createDirectories(savePath);
            }
            catch (IOException e) {
                System.err.println(e.toString());
            }
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
            final long count = fileStream
                .filter(p -> p.toFile().getName().equals(DEFAULT_JSON_FILE) == false)
                .count();
            return count <= 0;
        }
        catch (IOException e) {
            System.err.println("ERROR: Directory "+DEFAULT_JSON_PATH+" could not be listed, error was: "+e);
            return true;
        }
    }
}