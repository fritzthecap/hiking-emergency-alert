package fri.servers.hiking.emergencyalert.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * A reusable PasswordAuthentication source that stores the password in an
 * object-instance field and does not show a dialog any more once it was entered.
 */
public class InteractiveAuthenticator extends Authenticator
{
    private final Component parentWindow;
    private String user;
    private String password;

    public InteractiveAuthenticator(Component parentWindow)    {
        this.parentWindow = parentWindow;
    }

    /**
     * This gets called by <code>Authentictor.requestPasswordAuthentication()</code>
     * in super-class. The user-name has already been taken from mail-properties
     * and put into <code>getDefaultUserName()</code>, so we can use it here.
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication()    {
        if (user != null && user.length() > 0 && password != null && password.length() > 0)
            return new PasswordAuthentication(user, password);
            // user and password were already entered interactively
        
        if (getRequestingSite() == null) // happens when host is not reachable
            throw new IllegalArgumentException("Invalid mail host name, or host not reachable!");
        
        // getting interactive ...
        final String title = "Connecting to "+getRequestingSite().getHostName();
        final UI dialog = new UI(getDefaultUserName());
        
        if (dialog.display(parentWindow, title)) {
            final char[] enteredPassword = dialog.getPassword();
            if (enteredPassword.length > 0)
                return new PasswordAuthentication(
                        user = dialog.getUser(),
                        password = new String(enteredPassword)
                    );
        }
            
        return null;
    }
    
    
    private static class UI
    {
        private final JTextField usernameField;
        private final JPasswordField passwordField;
        private final JPanel panel;
        
        public UI(String user) {
            final JPanel left = new JPanel(new GridLayout(2, 1));
            left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
            final JPanel right = new JPanel(new GridLayout(2, 1));
            final JPanel leftAndRight = new JPanel(new BorderLayout());
            leftAndRight.add(left, BorderLayout.WEST);
            leftAndRight.add(right, BorderLayout.CENTER);

            left.add(new JLabel("User"));
            this.usernameField = new JTextField(user);
            right.add(usernameField);

            left.add(new JLabel("Password"));
            this.passwordField = new JPasswordField();
            right.add(passwordField);

            
            this.panel = leftAndRight;
            this.panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 70));
        }

        public boolean display(Component parent, String title) {
            // set initial focus
            final JComponent focus = (usernameField.getText().length() <= 0 ? usernameField : passwordField);
            final JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION) {
                @Override
                public void selectInitialValue() {
                    focus.requestFocusInWindow();
                }
            };
            
            final JDialog dialog = pane.createDialog(parent, title);
            dialog.setVisible(true);
            // is showing ...
            
            dialog.dispose();
            return (pane.getValue() != null && 
                    ((Integer) pane.getValue()).intValue() == JOptionPane.OK_OPTION);
        }
        
        public String getUser() {
            return usernameField.getText();
        }

        public char[] getPassword() {
            return passwordField.getPassword();
        }
    }
}