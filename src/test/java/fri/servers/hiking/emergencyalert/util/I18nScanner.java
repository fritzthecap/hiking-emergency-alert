package fri.servers.hiking.emergencyalert.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Scans Java sources for i18n identifiers and outputs
 * them as properties file content.
 * Option -v for outputting keys, -v for values, both possible too.
 * <ol>
 * <li>Use I18nScanner to create an ordered list of keys, put it into a file.</li>
 * <li>Use I18nFileJoiner with that key file and strings.properties to create 
 *      an ordered list of English text resource properties.
 *      This reports missing values before outputting properties.</li>
 * <li>Complete strings.properties until no missing values are reported any more.</li>
 * <li>When no values are missing any more, replace strings.properties content
 *      by the I18nFileJoiner output properties.</li>
 * <li>You can do the same with other strings_xx.properties unless they are empty.</li>
 * 
 * <li>When empty, it is time for a bulk translation instead of doing it manually.</li>
 * <li>Use I18nFileSplitter with key file and strings.properties to get an ordered 
 *      line-list of (right-side) English text resources (values, not keys).</li>
 * <li>Translate this line-list on DeepL to another language, keeping line order.</li>
 * <li>Use I18nFileLinesJoiner to join the translated line-list with the key file,
 *      they should be in same order and have same line-count.</li>
 * <li>Put the result into strings_xx.properties, xx being the other language.</li>
 * </ol>
 */
public class I18nScanner
{
    private static final String START_TOKEN = "i18n(\"";
    private static final String END_TOKEN = "\")"; // must be on same line!
    
    private static final String EXTENSION = ".java";
    
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
        new I18nScanner(doKey, doValue).scan(directory);
    }
    
    
    private boolean doKey;
    private boolean doValue;
    private PrintStream out = System.out;
    
    public I18nScanner(boolean doKey, boolean doValue) {
        this.doKey = doKey;
        this.doValue = doValue;
        
        if (doKey == false && doValue == false) {
            this.doKey = true;
            this.doValue = true;
        }
    }
    
    public void scan(String directory) throws IOException {
        Map<String,String> map = new LinkedHashMap<>();
        
        RecursiveFileVisitor fileVisitor = new RecursiveFileVisitor() {
            @Override
            protected void visit(File file) throws FileNotFoundException, IOException {
                readJavaSource(file, map);
            }
        };
        int fileCount = fileVisitor.traverse(new File(directory), EXTENSION);
        
        if (checkPropertiesCompatibility(map))
            output(map, doKey, doValue);
        
        System.err.println("Collected "+map.size()+" i18n identifiers from "+fileCount+" "+EXTENSION+" files");
    }

    private void readJavaSource(File file, Map<String,String> map) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int startIndex = line.indexOf(START_TOKEN);
                while (startIndex >= 0) {
                    int endIndex = line.indexOf(END_TOKEN, startIndex);
                    if (endIndex > startIndex) {
                        String value = line.substring(startIndex + START_TOKEN.length(), endIndex);
                        startIndex = line.indexOf(START_TOKEN, endIndex); // skip to next in line
                        
                        String key = replace(value);
                        map.put(key, value);
                    }
                    else {
                        System.err.println("I18n not terminated in file "+file.getName()+": "+line);
                        startIndex = -1;
                    }
                }
            }
        }
    }

    private String replace(String resourceKey) {
        return resourceKey
                .replace(' ', '_')
                .replace("=", "_")
                .replace(":", "_")
                ;
    }

    private boolean checkPropertiesCompatibility(Map<String,String> map) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry : map.entrySet()) {
            String propertiesLine = entry.getKey() + " = " + entry.getValue();
            sb.append(propertiesLine + "\n");
        }

        Properties properties = new Properties();
        properties.load(new StringReader(sb.toString()));
        
        int wrongKey = 0, wrongValue = 0;
        for (Map.Entry<String,String> entry : map.entrySet()) {
            String value = properties.getProperty(entry.getKey());
            
            if (value == null) {
                System.err.println("Key not found in Properties: "+entry.getKey());
                wrongKey++;
            }
            else if (value.equals(entry.getValue()) == false) {
                System.err.println("Value in Properties is wrong: "+value);
                wrongValue++;
            }
        }
        
        if (wrongKey > 0 || wrongValue > 0)
            System.err.println("From "+properties.size()+
                " properties: wrong keys: "+wrongKey+", wrong values: "+wrongValue);
        
        return wrongKey <= 0 && wrongValue <= 0;
    }

    private void output(Map<String,String> map, boolean key, boolean value) {
        for (Map.Entry<String, String> entry : map.entrySet())
            if (key && ! value)
                out.println(entry.getKey());
            else if ( ! key && value)
                out.println(entry.getValue());
            else if (key && value)
                out.println(entry.getKey()+" = "+entry.getValue());
    }
    
    
    private static abstract class RecursiveFileVisitor
    {
        private int fileCount;
        
        public int traverse(File file, String extension) throws FileNotFoundException, IOException {
            fileCount = 0;
            loop(file, extension);
            return fileCount;
        }

        protected abstract void visit(File file) throws FileNotFoundException, IOException;

        private void loop(File file, String extension) throws FileNotFoundException, IOException {
            if (file.isFile()) {
                if (extension == null || file.getName().endsWith(extension)) {
                    visit(file);
                    fileCount++;
                }
            }
            else if (file.isDirectory()) {
                final String[] list = file.list();
                for (int i = 0; list != null && i < list.length; i++)
                    loop(new File(file, list[i]), extension);
            }
        }
    }
}