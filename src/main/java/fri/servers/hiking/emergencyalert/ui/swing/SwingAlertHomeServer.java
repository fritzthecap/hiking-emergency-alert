package fri.servers.hiking.emergencyalert.ui.swing;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.Mail;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.ui.swing.util.UiAdjustments;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.HikeWizard;

/**
 * The main Swing-UI with wizard pages for starting a hike observation.
 */
public class SwingAlertHomeServer extends SwingUserInterface
{
    /**
     * Called from super-constructor, so do not use
     * instance-fields here, they are not yet initialized!
     */
    @Override
    protected JFrame buildUi() {
        return new JFrame();
    }
    
    /** Required call to show the window on screen. */
    public void show(String title) {
        initialize(title);
        
        frame.setTitle(title);
        frame.pack();
        frame.setSize(new Dimension(1000, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /** Overridden to notify the wizard's ObservationPage. */
    @Override
    public void showConfirmMail(Mail alertConfirmationMail) {
        SwingUtilities.invokeLater(() -> {
            final HikeWizard wizard = getHikeWizard();
            wizard.alertConfirmed();
        });
        super.showConfirmMail(alertConfirmationMail);
    }

    
    private void initialize(String title) {
        // redirect System outputs to UI and log-file
        final String logPathFile = new HikeFileManager().getLogPathFile();
        final File logFile = new File(logPathFile);
        
        System.out.println("You can find stdout and stderr in "+logPathFile); // write to OS console
        
        final Writer logWriter;
        try {
            logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        final JTextArea console = new JTextArea();
        console.setEditable(false);
        
        // START keep order of statements
        Log.redirectOutErr(console, logWriter); // redirect outputs to console and log
        System.out.println(title); // write title and version to log-file and Swing-console
        Log.activateOriginalStreams(true); // now activate System.out and System.err
        // END keep order of statements
        
        // do some UI corrections
        UiAdjustments.adjust();
        
        // create StateMachine
        final StateMachine stateMachine = new StateMachine(
                new Hike(), // will be replaced by HikeWizard/HikeFactory, StateMachine allows no null 
                new MailerImpl(), 
                new HikeTimer(), 
                this);
        
        // build content pane
        // START keep order of statements
        frame.getContentPane().add(new HikeWizard(frame, stateMachine, console, logWriter));
        
        console.setToolTipText(i18n("This text can also be found in")+" "+logPathFile);
        // END keep order of statements
    }
    
    private HikeWizard getHikeWizard() {
        return (HikeWizard) frame.getContentPane().getComponent(0);
    }
}