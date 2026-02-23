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
    private static class RedirectingOutputStream extends ByteArrayOutputStream
    {
        private final JTextArea textArea;
        
        RedirectingOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void flush() throws IOException {
            final String line = toString();
            if (SwingUtilities.isEventDispatchThread())
                textArea.append(line);
            else
                SwingUtilities.invokeLater(() -> textArea.append(line));
            
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
    
    
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JButton stdoutButton = new JButton("Write to System.out");
//            stdoutButton.addActionListener(event -> System.out.println("Hello System.out at "+new Date()));
//            
//            JButton stderrButton = new JButton("Write to System.err");
//            stderrButton.addActionListener(event -> System.err.println("Hello System.err at "+new Date()));
//
//            JToolBar toolbar = new JToolBar();
//            toolbar.add(stdoutButton);
//            toolbar.add(stderrButton);
//            
//            JTextArea textArea = new JTextArea();
//            textArea.setLineWrap(true);
//            textArea.setWrapStyleWord(true);
//            
//            Log.redirectOut(textArea);
//            Log.redirectErr(textArea);
//            
//            JPanel panel = new JPanel(new BorderLayout());
//            panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
//            panel.add(toolbar, BorderLayout.NORTH);
//            
//            JFrame frame = new JFrame();
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.getContentPane().add(panel);
//            frame.setSize(new Dimension(400, 200));
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//    }
}