package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.io.File;
import java.util.Objects;
import javax.swing.JButton;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import jakarta.mail.Authenticator;

/**
 * Data shipped between pages.
 */
public class Trolley
{
    static String buildNextButtonText() {
        return i18n("Next")+" >";
    }
    static String buildPreviousButtonText() {
        return "< "+i18n("Previous");
    }
    
    public final StateMachine stateMachine;
    private final JButton nextButton, previousButton;
    private String hikeCopy;
    private File hikeFile;
    
    private Authenticator authenticator;
    
    public Trolley(StateMachine stateMachine, JButton nextButton, JButton previousButton) {
        this.stateMachine = Objects.requireNonNull(stateMachine);
        
        this.nextButton = nextButton;
        this.previousButton = previousButton;

        refreshHikeCopy();
    }
    
    /** @return true when the hike was changed by the UI, done by comparison with a deep clone. */
    public boolean isHikeChanged() {
        final String currentHikeCopy = hikeToJsonString(stateMachine.getHike());
        return this.hikeCopy.equals(currentHikeCopy) == false;
    }

    /** Whoever has a valid authenticator can pass it to other pages. */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    /** When not null, this is a valid authenticator. */
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    
    public File getHikeFile() {
        return hikeFile;
    }
    public void setHikeFile(File hikeFile) {
        this.hikeFile = hikeFile;
    }
    
    public void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
    }
    public void setPreviousEnabled(boolean enabled) {
        previousButton.setEnabled(enabled);
    }
    
    /** Called on language change. */
    public void refreshLanguage() {
        nextButton.setText(buildNextButtonText());
        previousButton.setText(buildPreviousButtonText());
    }
    
    /** Called when loading a hike-file. */
    public void refreshHikeCopy() {
        this.hikeCopy = hikeToJsonString(stateMachine.getHike());
    }
    
    private String hikeToJsonString(Hike hike) {
        return new JsonGsonSerializer<Hike>().toJson(hike);
    }
}