package fri.servers.hiking.emergencyalert.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Reads a properties file and splits it line by line, outputting either key (-k) or value (-v).
 */
public class I18nFileSplitter extends AbstractI18n
{
    /** First argument must be a .properties-file, second a .txt file. */
    public static void main(String[] args) throws Exception {
        boolean doKey = false, doValue = false;
        String sourcePropertiesFile = null;
        String resultTextFile = null;
        
        for (String arg : args) {
            if (arg.equals("-k") && doKey == false)
                doKey = true;
            else if (arg.equals("-v") && doValue == false)
                doValue = true;
            else if (sourcePropertiesFile == null)
                sourcePropertiesFile = arg; 
            else if (resultTextFile == null)
                resultTextFile = arg;
            else                
                throw new IllegalArgumentException("Too many arguments: "+arg);
        }
        
        I18nFileSplitter i18nFileSplitter = new I18nFileSplitter(sourcePropertiesFile, resultTextFile);
        if (doKey)
            i18nFileSplitter.splitKeys();
        else if (doValue)
            i18nFileSplitter.splitValues();
        else
            throw new IllegalArgumentException("Either keys or values must be requested, giving one of -k or -v option");
    }

    
    private final String sourcePropertiesFile, resultTextFile;
    
    public I18nFileSplitter(String sourcePropertiesFile, String resultTextFile) throws IOException {
        assertPropertiesFile(2, sourcePropertiesFile, true);
        assertPropertiesFile(3, resultTextFile, false);
        
        this.sourcePropertiesFile = sourcePropertiesFile;
        this.resultTextFile = resultTextFile;
    }
    
    private void splitKeys() throws IOException {
        split(true);
    }

    private void splitValues() throws IOException {
        split(false);
    }

    private void split(boolean doKey) throws IOException {
        List<String> propertyLines = readIso88591Lines(sourcePropertiesFile);
        
        try (Writer writer = doKey 
                     ? createIso88591Writer(resultTextFile) 
                     : createUtf8Writer(resultTextFile)) // online translator needs UTF-8 strings
        {
            for (String propertyLine : propertyLines) {
                if (propertyLine.trim().startsWith("#") == false) {
                    int index = propertyLine.indexOf("=");
                    if (index < 0)
                        index = propertyLine.indexOf(":");
                    
                    if (index > 0) {
                        String key = propertyLine.substring(0, index).trim(); // key must not not contain spaces
                        String value = propertyLine.substring(index + 1);
                        while (value.startsWith(" ")) // cut off leading spaces
                            value = value.substring(1);
                        
                        if (doKey)
                            writer.write(key);
                        else
                            writer.write(value);
                        writer.write("\n");
                    }
                }
            }
        }
    }
}