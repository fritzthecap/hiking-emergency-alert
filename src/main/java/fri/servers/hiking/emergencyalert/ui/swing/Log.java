package fri.servers.hiking.emergencyalert.ui.swing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Redirect System.out and System.err to some UI window.
 */
public final class Log 
{
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    
    public static void redirectOut(JTextArea outputArea) {
        if (originalOut == null)
            originalOut = System.out;
        
        final ByteArrayOutputStream outStream = new RedirectingOutputStream(outputArea);
        System.setOut(new PrintStream(outStream, true)); // true: autoFlush on newlines
    }
    
    public static void restoreOut() {
        if (originalOut != null) {
            System.out.flush();
            System.setOut(originalOut);
        }
    }
    
    public static void redirectErr(JTextArea errorArea) {
        if (originalErr == null)
            originalErr = System.err;
        
        final ByteArrayOutputStream errStream = new RedirectingOutputStream(errorArea);
        System.setErr(new PrintStream(errStream, true)); // true: autoFlush on newlines
    }
    
    public static void restoreErr() {
        if (originalErr != null) {
            System.err.flush();
            System.setErr(originalErr);
        }
    }
    
    
    private static class RedirectingOutputStream extends ByteArrayOutputStream
    {
        private final JTextArea textArea;
        
        RedirectingOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void flush() throws IOException {
            final String line = toString();
            reset(); // else toString() would give all output again next time
            
            if (SwingUtilities.isEventDispatchThread())
                textArea.append(line);
            else
                SwingUtilities.invokeLater(() -> textArea.append(line));
        }
    }
    
    private Log() {} // do not instantiate
}