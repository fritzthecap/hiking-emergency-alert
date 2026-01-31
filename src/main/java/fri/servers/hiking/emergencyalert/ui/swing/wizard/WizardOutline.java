package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;

/**
 * The bullet line above "Back" and "Forward" buttons showing the wizard status.
 */
public class WizardOutline extends JPanel
{
    private static final Color normalColor = Color.GRAY;
    private static final Color highlightColor = Color.RED;
    
    private List<JLabel> balls = new ArrayList<>();
    
    public WizardOutline(int pageCount) {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        final JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        add(bar); // centers bar
        
        for (int i = 0; i < pageCount; i++)
            bar.add(ball(balls, i + 1, pageCount));
    }
    
    public void setHighlight(int index)  {
        for (JLabel ball : balls)
            ball.setForeground(normalColor);
        balls.get(index).setForeground(highlightColor);
    }

    private Component ball(List<JLabel> balls, int pageIndex, int pageCount) {
        final String label = (pageIndex == 1) ? "\u25CF\u2500"
                : (pageIndex == pageCount) ? "\u2500\u25CF"
                    : "\u2500\u25CF\u2500";
        final JLabel ball = new JLabel(label);
        ball.setToolTipText(""+pageIndex);
        SwingUtil.increaseFontSize(ball, 120, false, false);
        balls.add(ball);
        return ball;
    }
}