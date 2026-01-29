package fri.servers.hiking.emergencyalert.util;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads two files and joins them line by line,
 * putting a " = " between left and right part.
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

    public void join(String keyFile, String valueFile) throws IOException {
        List<String> keyLines = Files.readAllLines(Path.of(keyFile));
        List<String> valueLines = Files.readAllLines(Path.of(valueFile));
        
        if (keyLines.size() != valueLines.size()) {
            throw new IllegalStateException(
                    "Key file has "+keyLines.size()+
                    " lines, value file has "+valueLines.size()+" lines!");
        }
        
        for (int i = 0; i < keyLines.size(); i++) {
            String key = keyLines.get(i);
            if (key.contains(" "))
                throw new IllegalArgumentException("A key must not contain spaces: >"+key+"<");
            
            String value = valueLines.get(i);
            
            out.println(key + " = "+value);
        }
    }
}