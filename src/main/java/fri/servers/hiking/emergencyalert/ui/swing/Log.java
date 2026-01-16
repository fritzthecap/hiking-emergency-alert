package fri.servers.hiking.emergencyalert.ui.swing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JTextArea;

public final class Log 
{
    public static void redirectOutAndErr(final JTextArea outputArea) {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                final String line = toString();
                outputArea.append(line);
                reset(); // else toString() would give all output again next time
            }
            
        };
        final PrintStream out = new PrintStream(outStream, true); // true: autoFLush on newlines
        System.setOut(out);
        System.setErr(out);
    }
    
    private Log() {} // do not instantiate
}
