package fri.servers.hiking.emergencyalert.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

public class AbstractI18n
{
    protected void assertPropertiesFile(int nthArgument, String filename, boolean mustBe) {
        if (filename.endsWith(".properties") != mustBe)
            throw new IllegalArgumentException("The "+nthArgument+". argument "+(mustBe ? "MUST" : "MUST NOT")+" be a properties file: "+filename);
    }
    
    protected final List<String> readIso88591Lines(String propertiesFile) throws IOException {
        assertPropertiesFile(0, propertiesFile, true);
        return Files.readAllLines(Path.of(propertiesFile), Charset.forName("ISO-8859-1"));
    }
    
    protected final List<String> readIso88591Keys(String keyFile) throws IOException {
        List<String> keyLines = Files.readAllLines(Path.of(keyFile), Charset.forName("ISO-8859-1"));
        
        for (String keyLine : keyLines)
            if (keyLine.contains("="))
                throw new IllegalArgumentException("A key must NOT contain an equals-sign: >"+keyLine+"<, found in "+keyFile);
            else if (keyLine.contains(":"))
                throw new IllegalArgumentException("A key must not contain a colon: >"+keyLine+"<, found in "+keyFile);
            else if (keyLine.contains(" "))
                throw new IllegalArgumentException("A key must not contain spaces: >"+keyLine+"<, found in "+keyFile);
        
        return keyLines;
    }

    protected final void writeResultProperties(
            List<String> keyLines, 
            BiFunction<String,Integer,String> valueProvider, 
            String resultPropertiesFile) throws FileNotFoundException, IOException
    {
        try (Writer out = createIso88591Writer(resultPropertiesFile)) {
            for (int lineNumber = 0; lineNumber < keyLines.size(); lineNumber++) {
                String key = keyLines.get(lineNumber);
                String value = valueProvider.apply(null, lineNumber);
                
                if (value == null || value.isEmpty())
                    throw new IllegalArgumentException("Found no value for key '"+key+"'");
                
                out.write(key+" = "+value);
                out.write('\n');
            }
        }
    }
    
    protected final Writer createUtf8Writer(String resultPropertiesFile) throws FileNotFoundException {
        return createWriter(resultPropertiesFile, "UTF-8");
    }
    
    protected final Writer createIso88591Writer(String resultPropertiesFile) throws FileNotFoundException {
        return createWriter(resultPropertiesFile, "ISO-8859-1");
    }
    
    private final Writer createWriter(String resultPropertiesFile, String charsetName) throws FileNotFoundException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File(resultPropertiesFile)),
                        Charset.forName(charsetName)));
    }
}