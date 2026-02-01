package fri.servers.hiking.emergencyalert.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Reads a properties file and splits it line by line,
 * outputting either key (-k) or value (-v).
 */
public class I18nFileSplitter
{
    /**
     * Single argument must be a properties-file.
     */
    public static void main(String[] args) throws Exception {
        boolean doKey = false, doValue = false;
        String file = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.startsWith("-v"))
                    doValue = true;
                else if (arg.startsWith("-k"))
                    doKey = true;
            }
            else {
                if (file != null)
                    throw new IllegalArgumentException("Can process only one file, too much: "+arg);
                file = arg;
            }
        }
        new I18nFileSplitter(doKey, doValue).split(file);
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
    
    private void split(String propertiesFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(propertiesFile), Charset.forName("ISO-8859-1"))))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf("=");
                if (index > 0) {
                    String key = line.substring(0, index).trim();
                    String value = line.substring(index + 1);
                    while (value.startsWith(" "))
                        value = value.substring(1);
                    
                    if (doKey)
                        out.println(key);
                    else if (doValue)
                        out.println(value);
                }
            }
        }
    }
}