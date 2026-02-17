package fri.servers.hiking.emergencyalert.ui.swing;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
        final String title = getRequestingSite().getHostName();
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
            this.usernameField = new JTextField(user);
            usernameField.setBorder(BorderFactory.createTitledBorder(i18n("User")));
            
            this.passwordField = new JPasswordField();
            passwordField.setBorder(BorderFactory.createTitledBorder(i18n("Password")));

            final JToggleButton viewPassword = new JToggleButton("\u23FF");
            viewPassword.setToolTipText(i18n("View"));
            viewPassword.setBorderPainted(false);
            
            viewPassword.addActionListener(new ActionListener() {
                private int originalEchoChar = -1;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (originalEchoChar == -1)
                        originalEchoChar = passwordField.getEchoChar();
                    
                    passwordField.setEchoChar((char) 
                            ((passwordField.getEchoChar() == 0) ? originalEchoChar : 0));
                }
            });

            final JPanel panel = new JPanel(new GridBagLayout());
            final GridBagConstraints cell = new GridBagConstraints();
            cell.fill = GridBagConstraints.HORIZONTAL;
            
            cell.gridx = 0; cell.gridy = 0; cell.gridwidth = 1;
            panel.add(usernameField, cell);

            cell.gridx = 0; cell.gridy = 1; cell.gridwidth = 1;
            panel.add(passwordField, cell);

            cell.gridx = 1; cell.gridy = 1; cell.gridwidth = 1;
            panel.add(viewPassword, cell);
            
            this.panel = panel;
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