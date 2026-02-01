package fri.servers.hiking.emergencyalert.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;

/**
 * Reads a key file line by line, which is the first argument, not a .properties file.
 * Then opens the second argument file, which can be a .properties file or a .txt file,
 * and assigns either the value for key on line from the .properties file, or the value 
 * from same line number from the .txt file.
 * Non-existing .properties values are reported separately on stderr with line numbers.
 * In case of .txt file, an exception is thrown when both files do not have the same number of lines.
 * The application ends and does not write the result properties file when values are missing.
 * But when all values can be found, it outputs the keys, in their order, with assigned values
 * into the third argument properties file, using ISO-8859-1 encoding.
 */
public class I18nFileJoiner extends AbstractI18n
{
    public static void main(String[] args) throws Exception {
        if (args.length != 3)
            throw new IllegalArgumentException(
                    "SYNTAX: java I18nFileJoiner keys.txt source.properties result.properties\n"+
                    "    or: java I18nFileJoiner keys.txt values.txt result.properties");
            
        I18nFileJoiner joiner = new I18nFileJoiner(args[0], args[1]);
        joiner.join(args[2]);
    }


    private final List<String> keyLines;
    private final String valueFile;
    
    public I18nFileJoiner(String keyFile, String valueFile) throws IOException {
        assertPropertiesFile(1, keyFile, false);
        
        this.keyLines = readIso88591Keys(keyFile);
        this.valueFile = valueFile;
    }
    
    private void join(String resultPropertiesFile) throws IOException {
        assertPropertiesFile(3, resultPropertiesFile, true);
        
        boolean isPropertiesJoin = valueFile.endsWith(".properties");
        if (isPropertiesJoin)
            joinFromProperties(valueFile, resultPropertiesFile);
        else
            joinLines(valueFile, resultPropertiesFile);
    }
    
    private void joinFromProperties(String sourcePropertiesFile, String resultPropertiesFile) throws IOException {
        assertPropertiesFile(2, sourcePropertiesFile, true);
        
        Properties sourceProperties = new Properties();
        sourceProperties.load(new FileInputStream(sourcePropertiesFile));
        
        StringBuilder missingValueKeys = new StringBuilder();
        Properties resultProperties = new Properties();
        
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
        
        if (missingValueKeys.length() > 0) {
            System.err.println(missingValueKeys.toString());
        }
        else {
            BiFunction<String,Integer,String> valueProvider = 
                    (key, lineNumber) -> resultProperties.getProperty(key);
                    
            writeResultProperties(keyLines, valueProvider, resultPropertiesFile);
        }
    }

    private void joinLines(String valueFile, String resultPropertiesFile) throws IOException {
        assertPropertiesFile(2, valueFile, false);
        
        List<String> valueLines = Files.readAllLines(Path.of(valueFile)); // values in text-files are in UTF-8
        
        if (keyLines.size() != valueLines.size())
            throw new IllegalStateException(
                    "Key file has "+keyLines.size()+
                    " lines, value file has "+valueLines.size()+" lines!");
        
        BiFunction<String,Integer,String> valueProvider = 
                (key, lineNumber) -> valueLines.get(lineNumber);
                
        writeResultProperties(keyLines, valueProvider, resultPropertiesFile);
    }
}