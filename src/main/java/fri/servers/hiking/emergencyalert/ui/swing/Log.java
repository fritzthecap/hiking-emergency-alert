package fri.servers.hiking.emergencyalert.ui.swing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import javax.swing.JTextArea;

public final class Log
{
    public static void setOut(JTextArea output) {
//        OutputStream outStream = new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                throw new RuntimeException("Implement me!");
//            }
//        };
//        OutputStreamWriter outStreamWriter = new OutputStreamWriter() {
//            
//        };
//        PrintStream out = new PrintStream(outStream);
    }
    
    public static void setErr(JTextArea errorOutput) {
    }
    
    private Log() {} // do not instantiate
}
