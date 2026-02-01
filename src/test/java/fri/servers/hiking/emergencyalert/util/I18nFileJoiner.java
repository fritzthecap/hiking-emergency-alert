package fri.servers.hiking.emergencyalert.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Reads a key file line by line, which is the first argument and not a properties file.
 * Then opens the second file, which must be a properties file, and assigns
 * the property-values of it to the identical keys of the first file,
 * whereby non-existing values are reported separately with line numbers.
 * When all values are existing, outputs the keys in their order with assigned values
 * (with a " = " between) into the third argument with ISO-8859-1 encoding.
 * Else just outputs on stderr the keys that are missing their values.
 */
public class I18nFileJoiner
{
    /**
     * First argument must be key-file, 
     * second source-properties-file, 
     * third result-properties-file.
     */
    public static void main(String[] args) throws Exception {
        new I18nFileJoiner().join(args[0], args[1], args[2]);
    }

    private void join(String keyFile, String sourcePropertiesFile, String resultPropertiesFile) throws IOException {
        if (sourcePropertiesFile.endsWith(".properties") == false)
            throw new IllegalArgumentException("The second argument MUST be a properties file: "+sourcePropertiesFile);
        
        List<String> keyLines = Files.readAllLines(Path.of(keyFile));
        for (String line : keyLines)
            if (line.contains("="))
                throw new IllegalArgumentException("The first argument must NOT be a properties file: '"+line+"'");
        
        Properties sourceProperties = new Properties();
        sourceProperties.load(new FileInputStream(sourcePropertiesFile));
        
        StringBuilder missingValueKeys = new StringBuilder();
        Properties resultProperties = new Properties();
        
        loopKeys(keyLines, sourceProperties, resultProperties, missingValueKeys);
        
        if (missingValueKeys.length() > 0) {
            System.err.println(missingValueKeys.toString());
        }
        else {
            Writer out = new OutputStreamWriter(
                    new FileOutputStream(new File(resultPropertiesFile)),
                    Charset.forName("ISO-8859-1"));
            
            for (int lineNumber = 0; lineNumber < keyLines.size(); lineNumber++) {
                String key = keyLines.get(lineNumber);
                String value = resultProperties.getProperty(key);
                String property = key + " = " + value;
                out.write(property);
                out.write('\n');
            }
            out.close();
            
        }
    }

    private void loopKeys(
            List<String> keyLines, 
            Properties sourceProperties, 
            Properties resultProperties, 
            StringBuilder missingValueKeys)
    {
        for (int lineNumber = 0; lineNumber < keyLines.size(); lineNumber++) {
            String key = keyLines.get(lineNumber);
            if (key.contains(" "))
                throw new IllegalArgumentException("A key must not contain spaces: >"+key+"<");
            
            String value = sourceProperties.getProperty(key);
            if (value == null)
                missingValueKeys.append((lineNumber + 1)+": "+key+"\n");
            else
                resultProperties.setProperty(key, value);
        }
    }
}