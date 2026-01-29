package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.io.File;
import java.util.Objects;
import javax.swing.JButton;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
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
    private final DescriptionArea descriptionArea;
    
    private String hikeCopy;
    private File hikeFile;
    
    private Authenticator authenticator;
    
    public Trolley(StateMachine stateMachine, DescriptionArea descriptionArea, JButton nextButton, JButton previousButton) {
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.descriptionArea = descriptionArea;
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
        refreshHikeCopy();
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
        descriptionArea.refreshLanguage();
    }
    
    /** Called when loading a hike-file from disk, or saving one to disk. */
    private void refreshHikeCopy() {
        this.hikeCopy = hikeToJsonString(stateMachine.getHike());
    }
    
    public void save(Hike hike) throws Exception {
        save(new HikeFileManager(), getHikeFile(), hike);
    }
    
    /**
     * Saves hike to persistence.
     * @param hikeFileManager saves to file.
     * @param targetFile null for default file, or an explicitly chosen file.
     * @param hike the hike to save as JSON.
     * @return the file where hike was written to.
     */
    public File save(HikeFileManager hikeFileManager, File targetFile, Hike hike) throws Exception {
        final String json = hikeToJsonString(hike);
        final File actualFile;
        
        if (targetFile != null) { // user explicitly chose a file
            hikeFileManager.save(targetFile.getAbsolutePath(), json);
            setHikeFile(actualFile = targetFile); // make the explicitly chosen file the file for future saves
        }
        else {
            hikeFileManager.save(json);
            actualFile = new File(hikeFileManager.getSavePathFile());
        }
        
        refreshHikeCopy(); // refresh change-detection with new data
        
        return actualFile;
    }
    
    private String hikeToJsonString(Hike hike) {
        return new JsonGsonSerializer<Hike>().toJson(hike);
    }
}