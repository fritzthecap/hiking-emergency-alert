package fri.servers.hiking.emergencyalert.ui.swing;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Redirect System.out and System.err to some UI window.
 */
public final class Log 
{
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    
    public static void redirectOutErr(JTextArea outputErrorArea, BufferedWriter logFile) {
        if (outputErrorArea != null) {
            redirect(true, outputErrorArea, logFile);
            redirect(false, outputErrorArea, logFile);
        }
    }
    
    public static void restoreOutErr() {
        restore(true);
        restore(false);
    }
    
    public static void addToOutErr(JTextArea outputArea, JTextArea errorArea) {
        if (outputArea != null)
            redirect(true, outputArea, null);
        if (errorArea != null)
            redirect(false, errorArea, null);
    }
    
    public static void removeFromOutErr(JTextArea outputArea, JTextArea errorArea) {
        if (outputArea != null)
            removeFrom(true, outputArea);
        if (errorArea != null)
            removeFrom(false, errorArea);
    }
    
    
    private static void redirect(boolean isOut, JTextArea textArea, BufferedWriter logFile) {
        final PrintStream streamBackup = (isOut ? originalOut : originalErr);
        if (streamBackup == null) {
            if (isOut) {
                System.out.flush(); // write all pending text to console
                originalOut = System.out;
            }
            else {
                System.err.flush();
                originalErr = System.err;
            }
        }
        
        final PrintStream streamInCharge = (isOut ? System.out : System.err);
        if (streamInCharge instanceof RedirectingPrintStream) {
            final RedirectingPrintStream printStream = (RedirectingPrintStream) streamInCharge;
            printStream.getStream().add(textArea);
        }
        else {
            final RedirectingPrintStream printStream = new RedirectingPrintStream(isOut, textArea, logFile);
            if (isOut)
                System.setOut(printStream);
            else
                System.setErr(printStream);
        }
    }
    
    private static void restore(boolean isOut) {
        final PrintStream streamBackup = (isOut ? originalOut : originalErr);
        if (streamBackup != null) {
            final RedirectingPrintStream printStream = getFlushedRedirectingPrintStream(isOut);
            if (printStream != null) {
                printStream.getStream().release();
                
                if (isOut) // restore system stream
                    System.setOut(streamBackup);
                else
                    System.setErr(streamBackup);
            }
        }
    }
    
    private static void removeFrom(boolean isOut, JTextArea textArea) {
        final RedirectingPrintStream printStream = getFlushedRedirectingPrintStream(isOut);
        if (printStream != null)
            printStream.getStream().remove(textArea);
    }
    
    private static RedirectingPrintStream getFlushedRedirectingPrintStream(boolean isOut) {
        final PrintStream streamInCharge = (isOut ? System.out : System.err);
        if ((streamInCharge instanceof RedirectingPrintStream) == false)
            return null;
        
        streamInCharge.flush(); // write all pending text to all text-areas
        return (RedirectingPrintStream) streamInCharge;
    }
    
    
    private static class RedirectingPrintStream extends PrintStream
    {
        RedirectingPrintStream(boolean isOut, JTextArea textArea, BufferedWriter logFile) {
            super(new RedirectingOutputStream(isOut, textArea, logFile), true); // true: autoFlush on newlines
        }
        
        RedirectingOutputStream getStream() {
            return (RedirectingOutputStream) out;
        }
    }
    
    private static class RedirectingOutputStream extends ByteArrayOutputStream
    {
        private final boolean isOut;
        private final List<JTextArea> textAreas = new ArrayList<>();
        private final BufferedWriter logFile;
        
        RedirectingOutputStream(boolean isOut, JTextArea textArea, BufferedWriter logFile) {
            this.isOut = isOut;
            this.logFile = logFile;
            textAreas.add(textArea);
        }
        
        void add(JTextArea textArea) {
            textAreas.add(textArea);
        }
        
        void remove(JTextArea textArea) {
            textAreas.remove(textArea);
        }

        void release() {
            textAreas.clear();
        }
        
        @Override
        public void flush() throws IOException {
            final String line = toString(); // ends with newline
            
            reset(); // else next toString() would give all output again
            
            if (logFile != null) {
                logFile.write(line);
                logFile.flush();
            }
            
            if (isOut)
                originalOut.print(line);
            else
                originalErr.print(line);
            
            if (SwingUtilities.isEventDispatchThread())
                writeToAllTextAreas(line);
            else
                SwingUtilities.invokeLater(() -> writeToAllTextAreas(line));
        }
        
        private void writeToAllTextAreas(String line) {
            for (JTextArea textArea : textAreas)
                textArea.append(line);
        }
    }
    
    
    private Log() {} // do not instantiate
}