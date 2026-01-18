package fri.servers.hiking.emergencyalert.ui.swing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JTextArea;

/**
 * Redirect System.out and System.err to some UI window.
 */
public final class Log 
{
    private static class RedirectingOutputStream extends ByteArrayOutputStream
    {
        private final JTextArea textArea;
        
        RedirectingOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void flush() throws IOException {
            final String line = toString();
            textArea.append(line);
            reset(); // else toString() would give all output again next time
        }
    }
    
    public static void redirectOut(JTextArea outputArea) {
        final ByteArrayOutputStream outStream = new RedirectingOutputStream(outputArea);
        System.setOut(new PrintStream(outStream, true)); // true: autoFlush on newlines
    }
    public static void redirectErr(JTextArea errorArea) {
        final ByteArrayOutputStream errStream = new RedirectingOutputStream(errorArea);
        System.setErr(new PrintStream(errStream, true)); // true: autoFlush on newlines
    }
    
    private Log() {} // do not instantiate
}