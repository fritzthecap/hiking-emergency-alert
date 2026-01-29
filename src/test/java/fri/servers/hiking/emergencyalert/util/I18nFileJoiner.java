package fri.servers.hiking.emergencyalert.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Reads a key file line by line, which is the first argument and not a properties file.
 * Then opens the second file, which must be a properties file, and assigns
 * the property-values of it to the identical keys of the first file,
 * whereby non-existing values are reported separately with line numbers.
 * Outputs the keys in their order with assigned values with a " = " between, 
 * non-existing values are left empty for manual correction
 * according to separately reported messages.
 * <p/>
 * This is for finding language resource keys that are in Java source but not yet
 * in language-properties. These language properties must be the second argument.
 */
public class I18nFileJoiner
{
    /**
     * First argument must be key-file, second value-file.
     * @throws Exception when files have different line counts.
     */
    public static void main(String[] args) throws Exception {
        new I18nFileJoiner().join(args[0], args[1]);
    }

    
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public void join(String keyFile, String propertiesFile) throws IOException {
        if (propertiesFile.endsWith(".properties") == false)
            throw new IllegalArgumentException("The second argument MUST be a properties file: "+propertiesFile);
            
        List<String> keyLines = Files.readAllLines(Path.of(keyFile));
        for (String line : keyLines)
            if (line.contains("="))
                throw new IllegalArgumentException("The first argument must NOT be a properties file: '"+line+"'");
        
        Properties properties = new Properties();
        properties.load(new InputStreamReader(new FileInputStream(propertiesFile)));
        
        StringBuilder missingValueKeys = new StringBuilder();
        StringBuilder propertiesLines = new StringBuilder();
        
        loopKeys(keyLines, properties, propertiesLines, missingValueKeys);
        
        if (missingValueKeys.length() > 0)
            err.println(missingValueKeys.toString());
        else
            out.println(propertiesLines.toString());
    }

    private void loopKeys(
            List<String> keyLines, 
            Properties properties, 
            StringBuilder propertiesLines, 
            StringBuilder missingValueKeys)
    {
        for (int lineNumber = 0; lineNumber < keyLines.size(); lineNumber++) {
            String key = keyLines.get(lineNumber);
            if (key.contains(" "))
                throw new IllegalArgumentException("A key must not contain spaces: >"+key+"<");
            
            String value = properties.getProperty(key);
            if (value == null)
                missingValueKeys.append(lineNumber+": "+key+"\n");
            else
                propertiesLines.append(key + " = "+value+"\n");
        }
    }
}