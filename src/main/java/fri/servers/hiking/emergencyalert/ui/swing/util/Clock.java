package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Clock extends JPanel
{
    private final JLabel hour;
    private final JLabel minute;
    private final JLabel second;
    
    public Clock() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        add(hour = newLabel("00"));
        add(newLabel(":"));
        add(minute = newLabel("00"));
        add(newLabel(":"));
        add(second = newLabel("00"));
        
        final Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Calendar currentTime = Calendar.getInstance();
                hour.setText(pad(currentTime.get(Calendar.HOUR_OF_DAY)));
                minute.setText(pad(currentTime.get(Calendar.MINUTE)));
                second.setText(pad(currentTime.get(Calendar.SECOND)));
            }
        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
    
    private JLabel newLabel(String text) {
        return (JLabel) SwingUtil.increaseFontSize(new JLabel(text), 120, true, false);
    }

    private String pad(int value) {
        final String number = String.valueOf(value);
        return (number.length() < 2) ? "0"+number : number;
    }
    
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Clock(), BorderLayout.NORTH);
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}