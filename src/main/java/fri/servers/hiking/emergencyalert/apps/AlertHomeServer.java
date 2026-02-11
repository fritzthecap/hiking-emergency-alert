package fri.servers.hiking.emergencyalert.apps;

import java.io.IOException;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import fri.servers.hiking.emergencyalert.Version;
import fri.servers.hiking.emergencyalert.mail.impl.MailerImpl;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.time.HikeTimer;
import fri.servers.hiking.emergencyalert.ui.UserInterface;
import fri.servers.hiking.emergencyalert.ui.swing.SwingAlertHomeServer;
import fri.servers.hiking.emergencyalert.ui.swing.SwingUserInterface;

/**
 * Application that should be started before you go on a hike.
 * This server must run until you come home again in time.
 * It will watch the time and send alert messages on overdue,
 * while polling for alert responses.
 * <p/>
 * You should clean your mailbox before using this application,
 * so fetching mail lists will be faster!
 * <p/>
 * Mind that although this application can be driven from
 * command-line it depends on Swing for the mail connection
 * password input.
 */
public class AlertHomeServer
{
    /**
     * Runs either from command-line with an UTF-8 JSON file,
     * or opens a graphical user-interface to edit data.
     * @param arguments optional, a JSON file containing the Hike.
     * @throws IOException when JSON file reading or parsing doesn't work.
     */
    @SuppressWarnings("unused")
    public static void main(String[] arguments) throws IOException {
        if (arguments.length > 0) { // Hikes given as JSON files
            for (int i = 0; i < arguments.length; i++) {
                final String argument = arguments[i];
                
                if (argument.equals("-")) { // read from stdin
                    final Scanner scanner = new Scanner(System.in);
                    final StringBuilder json = new StringBuilder();
                    while (scanner.hasNextLine())
                        json.append(scanner.nextLine()+"\n");
                    scanner.close();
                    
                    new AlertHomeServer(json.toString());
                }
                else {
                    final String json = new HikeFileManager().load(argument);
                    if (json != null)
                        new AlertHomeServer(json);
                    else
                        syntax("Error: not a file: "+argument);
                }
            }
        }
        else {
            SwingUtilities.invokeLater(() -> {
                try {
                    new AlertHomeServer(null);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    
    private static void syntax(String errorMessage) {
        System.err.println(errorMessage);
        
        System.out.println("SYNTAX: java "+AlertHomeServer.class.getName()+" [ hike.json | - ]");
        System.out.println("    The hike.json file must contain a Hike's JSON.");
        System.out.println("    The argument '-' reads JSON from commandline input.");
        System.out.println("    For JSON structure see Java-class Hike, which is the top of the data hierarchy.");
    }
    
    
    /** @param hikeJson JSON containing the hike to start, or null to run Swing UI. */
    public AlertHomeServer(String hikeJson) throws IOException {
        final String title = "Hiking-Emergency-Alert "+Version.get();
        System.out.println(title);
        
        if (hikeJson == null)
            new SwingAlertHomeServer().show(title);
            // complete UserInterface to edit and run Hike
        else
            run(hikeJson, new SwingUserInterface());
            // minimal UserInterface with interactive password dialog
    }

    /**
     * @param hikeJson required, JSON containing Hike data.
     * @param user at least a minimal UserInterface that will
     *      serve as password authenticator and alert-confirmation notifier.
     * @throws IOException when JSON is invalid.
     */
    private void run(String hikeJson, UserInterface user) throws IOException {
        // read hike data
        final Hike hike = new JsonGsonSerializer<Hike>().fromJson(hikeJson, Hike.class);
        
        // configure and start a StateMachine
        final StateMachine stateMachine = new StateMachine(hike, new MailerImpl(), new HikeTimer(), user);
        stateMachine.dispatchEvent(Event.REGISTRATION, hike);
        stateMachine.dispatchEvent(Event.ACTIVATION, hike); // starts timer
        
        // app ends when last timer-thread terminates, 
        // so no "while (stateMachine.isRunning())" loop is needed
    }
}