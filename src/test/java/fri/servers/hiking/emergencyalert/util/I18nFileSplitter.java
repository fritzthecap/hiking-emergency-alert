package fri.servers.hiking.emergencyalert.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

/**
 * Reads two files and joins them line by line,
 * putting a " = " between left and right part.
 */
public class I18nFileSplitter
{
    /**
     * First argument must be key-file, second value-file.
     * @throws Exception when files have different line counts.
     */
    public static void main(String[] args) throws Exception {
        boolean doKey = false, doValue = false;
        String directory = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.startsWith("-v"))
                    doValue = true;
                else if (arg.startsWith("-k"))
                    doKey = true;
            }
            else {
                directory = arg;
            }
        }
        new I18nFileSplitter(doKey, doValue).split(directory);
    }

    
    private boolean doKey;
    private boolean doValue;
    private PrintStream out = System.out;
    
    public I18nFileSplitter(boolean doKey, boolean doValue) {
        this.doKey = doKey;
        this.doValue = doValue;
        
        if (doKey == doValue)
            throw new IllegalArgumentException("Can not split both keys and values!");
    }
    
    public void split(String propertiesFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new InputStreamReader(new FileInputStream(propertiesFile)));
        
        for (Map.Entry<Object,Object> entry : properties.entrySet()) {
            if (doKey)
                out.println(entry.getKey());
            else if (doValue)
                out.println(entry.getValue());
        }
    }
}