package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.io.File;
import java.util.Objects;
import javax.swing.JButton;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
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
    
    /** Listeners are able to navigate to an arbitrary page. */
    public interface PageRequestListener
    {
        /** Navigates to given page. */
        void gotoPage(Class<? extends AbstractWizardPage> requestedPage);
    }
    
    public final StateMachine stateMachine;
    private final JButton forwardButton, backwardButton;
    private final DescriptionArea descriptionArea;
    
    private String hikeCopy;
    private File hikeFile;
    
    private Authenticator authenticator;
    
    private PageRequestListener pageRequestListener;
    
    public Trolley(
            StateMachine stateMachine, 
            DescriptionArea descriptionArea,
            PageRequestListener pageRequestListener,
            JButton forwardButton, 
            JButton backwardButton)
    {
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.descriptionArea = descriptionArea;
        this.pageRequestListener = pageRequestListener;
        this.forwardButton = forwardButton;
        this.backwardButton = backwardButton;

        refreshHikeCopy(stateMachine.getHike());
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
    
    /** @return explicitly loaded hike file, or null when default. */
    public File getHikeFile() {
        return hikeFile;
    }
    /** Called when loading a hike-file from disk, or saving one to disk. */
    public void setHikeFile(File hikeFile, Hike hike) {
        this.hikeFile = hikeFile;
        refreshHikeCopy(hike);
    }
    
    public void setForwardEnabled(boolean enabled) {
        forwardButton.setEnabled(enabled);
    }
    public void setBackwardEnabled(boolean enabled) {
        backwardButton.setEnabled(enabled);
    }
    
    /** Makes it possible to go to an arbitrary page. */
    public void gotoPage(Class<? extends AbstractWizardPage> requestedPage) {
        pageRequestListener.gotoPage(requestedPage);
    }
    
    /** Called on language change. */
    public void refreshLanguage() {
        forwardButton.setText(buildNextButtonText());
        backwardButton.setText(buildPreviousButtonText());
        descriptionArea.refreshLanguage();
    }
    
    /**
     * Saves hike to persistence.
     * @param hike the hike to save as JSON.
     */
    public void save(Hike hike) throws Exception {
        save(new HikeFileManager(), getHikeFile(), hike);
    }
    
    /**
     * Saves hike to persistence. This always also writes to the default file.
     * @param hikeFileManager the file-manager that saves JSON files.
     * @param targetFile null or an explicitly chosen file.
     * @param hike the hike to save as JSON.
     */
    public void save(HikeFileManager hikeFileManager, File targetFile, Hike hike) throws Exception {
        final String json = hikeToJsonString(hike);
        
        if (targetFile != null) { // user explicitly chose a file
            hikeFileManager.save(targetFile.getAbsolutePath(), json);
            setHikeFile(targetFile, hike); // make the explicitly chosen file the file for future saves
        }
        else {
            refreshHikeCopy(hike); // refresh change-detection with current persistence-state
        }
        
        hikeFileManager.save(json); // always also save to default file
        
    }
    
    private String hikeToJsonString(Hike hike) {
        return new JsonGsonSerializer<Hike>().toJson(hike);
    }
    
    private void refreshHikeCopy(Hike hike) {
        this.hikeCopy = hikeToJsonString(hike);
    }
}