package fri.servers.hiking.emergencyalert.ui.swing.util;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class ConsoleDialog extends JDialog
{
    private final JTextArea console;
    private boolean sized; // = false
    
    public ConsoleDialog(Frame parent, JTextArea console) {
        super(parent, i18n("Console"), false); // non-modal dialog
        
        this.console = console;
        
        getContentPane().add(buildToolbar(), BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(console), BorderLayout.CENTER);
        
        setSize(320, 600);
        setLocationRelativeTo(parent);
    }

    public void toggleVisibility() {
        if (sized == false) {
            sized = true;
            final Container parent = getParent(); // the Frame
            final Point parentLocation = parent.getLocationOnScreen();
            setLocation(parentLocation);
            setSize(330, parent.getHeight());
        }
        setVisible(isVisible() == false);
    }
    
    private JComponent buildToolbar() {
        final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.add(buildClearButton());
        toolBar.add(buildWrapLinesCheckbox());
        return toolBar;
    }

    private JButton buildClearButton() {
        final JButton clear = new JButton(i18n("Clear"));
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.setText("");
            }
        });
        return clear;
    }
    
    private JCheckBox buildWrapLinesCheckbox() {
        final JCheckBox wrapLines = new JCheckBox(i18n("Wrap Lines"), console.getLineWrap());
        wrapLines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.setLineWrap(console.getLineWrap() == false);
            }
        });
        return wrapLines;
    }
}