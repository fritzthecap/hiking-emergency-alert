package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.Color;
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
        setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        
        add(hour = newLabel("00"));
        add(newLabel(":"));
        add(minute = newLabel("00"));
        add(newLabel(":"));
        add(second = newLabel("00"));
        
        final ActionListener timeRenderer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Calendar currentTime = Calendar.getInstance();
                hour.setText(pad(currentTime.get(Calendar.HOUR_OF_DAY)));
                minute.setText(pad(currentTime.get(Calendar.MINUTE)));
                second.setText(pad(currentTime.get(Calendar.SECOND)));
            }
        };
        timeRenderer.actionPerformed(null); // put initial values
        
        final Timer timer = new Timer(1000, timeRenderer);
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
    
    private JLabel newLabel(String text) {
        final JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        return (JLabel) SwingUtil.increaseFontSize(label, 124, true, false);
    }

    private String pad(int value) {
        final String number = Integer.toString(value);
        return (number.length() < 2) ? "0"+number : number;
    }
    
    
    /*public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new Clock());
        frame.setSize(200, 100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }*/
}