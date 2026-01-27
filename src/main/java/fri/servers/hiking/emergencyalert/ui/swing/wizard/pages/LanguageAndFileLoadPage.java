package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.ui.swing.util.FileChooser;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.Language;

/**
 * Choose your UI language and/or load a hike file.
 */
public class LanguageAndFileLoadPage extends AbstractWizardPage
{
    private final Item[] languageItems = new Item[] {
            new Item("English", Locale.ENGLISH),
            new Item("Deutsch", Locale.GERMAN),
            new Item("Français", Locale.FRENCH),
            new Item("Italiano", Locale.ITALIAN),
            new Item("Español", Language.toLocale("es")),
    };
    
    private JComboBox<Item> languageChoiceField;
    private JButton loadFile;
    //private JCheckBox autoLoadSameFile;
    private FileChooser fileChooser;
    
    private final ItemListener languageSelectionListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED)
                rebuildWithNewLanguage(getSelectedLocale(), true);
        }
    };
    
    private final ActionListener loadFileListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadHike();
        }
    };

    /** Language for title would not get updated, so return no title! */
    @Override
    protected String getTitle() {
        return "";
    }
    
    @Override
    protected void buildUi() {
        fileChooser = new FileChooser(getContentPanel(), null);
        
        languageChoiceField = new JComboBox<Item>(); // does NOT yet consider hike language!
        for (Item item : languageItems)
            languageChoiceField.addItem(item);
        
        languageChoiceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(i18n("Choose Your Language")),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        languageChoiceField.setPreferredSize(new Dimension(180, 70));
        
        languageChoiceField.addItemListener(languageSelectionListener);
        
        loadFile = new JButton(i18n("Choose Hike File"));
        loadFile.setToolTipText(i18n("If you don't load a file, the default file will be used for optionally saving your inputs"));
        loadFile.addActionListener(loadFileListener);
        loadFile.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        //autoLoadSameFile = new JCheckBox(i18n("Next Time Load It Automatically"), true);
        //autoLoadSameFile.setToolTipText(i18n("When activated, the inputs from the chosen file will be used next time"));
        //autoLoadSameFile.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        // layout
        
        final JPanel loadFilePanel = new JPanel();
        loadFilePanel.setLayout(new BoxLayout(loadFilePanel, BoxLayout.Y_AXIS));
        loadFilePanel.add(Box.createVerticalGlue());
        loadFilePanel.add(loadFile);
        //loadFilePanel.add(autoLoadSameFile);
        loadFilePanel.add(Box.createVerticalGlue());
        
        final JPanel languageCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        languageCenterPanel.add(languageChoiceField);
        final JPanel languageToMiddlePanel = new JPanel(new BorderLayout());
        languageToMiddlePanel.add(languageCenterPanel, BorderLayout.CENTER);
        
        final JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(loadFilePanel);
        centerPanel.add(languageToMiddlePanel);
        
        getContentPanel().add(centerPanel);
    }
    
    /** Adjusts <code>languageChoiceField</code> to the hike's language. */
    @Override
    protected void populateUi(Hike hike) {
        final Locale newLocale = isUiLanguageChanged(hike);
        if (newLocale != null) {
            for (int i = 0; i < languageChoiceField.getItemCount(); i++) {
                final Item item = languageChoiceField.getItemAt(i);
                
                if (item.locale.equals(newLocale)) {
                    // avoid endless recursion through loadStringResources(),
                    // because selected item always will be ENGLISH after buildUi()
                    languageChoiceField.removeItemListener(languageSelectionListener);
                    languageChoiceField.setSelectedItem(item);
                    languageChoiceField.addItemListener(languageSelectionListener);
                }
            }
        }
    }

    @Override
    protected boolean commit(boolean goingForward) {
        getHike().getAlert().setIso639Language(getSelectedLocale().getLanguage());
        return true;
    }
    

    /** Called with true when another language gets selected by user. Rebuilds the whole UI. */
    private void rebuildWithNewLanguage(Locale locale, boolean commitLanguageToHike) {
        if (commitLanguageToHike)
            commit(true);
        
        // for correctness, remove listeners before rebuilding UI
        languageChoiceField.removeItemListener(languageSelectionListener);
        loadFile.removeActionListener(loadFileListener);
        
        // load text resources of selected language
        Language.load(locale);
        
        // rebuild the complete UI
        getContentPanel().removeAll();
        
        buildUi();
        populateUi(getHike());
        
        getContentPanel().revalidate();
        getContentPanel().repaint();
        
        getTrolley().refreshLanguage(); // changes texts on wizard buttons
    }
    

    private Locale isUiLanguageChanged(Hike hike) {
        final Locale selectedLocale = getSelectedLocale();
        final Locale hikeLocale = getHikeLocale(hike);
        return (hikeLocale != null && selectedLocale.equals(hikeLocale) == false) 
                ? hikeLocale
                : null;
    }
    
    private Locale getSelectedLocale() {
        return ((Item) languageChoiceField.getSelectedItem()).locale;
    }
    
    private Locale getHikeLocale(Hike hike) {
        try {
            final String iso639Language = hike.getAlert().getIso639Language();
            return Language.toLocale(iso639Language);
        }
        catch (Exception e) {
            showError(e);
            return null;
        }
    }
    
    private void loadHike() {
        final File[] hikeFile = fileChooser.open(true, "json"); // extension
        if (hikeFile != null)
            loadHike(hikeFile[0]);
    }
    
    private void loadHike(File hikeFile) {
        try {
            final String hikeJson = new HikeFileManager().load(hikeFile.getAbsolutePath());
            final Hike hike = new JsonGsonSerializer<Hike>().fromJson(hikeJson, Hike.class);
            getStateMachine().getUserInterface().registerHike(hike); // getHike() will return new hike now
            
            final Locale newLocale = isUiLanguageChanged(hike);
            if (newLocale != null) // the language of the loaded hike is different from current one
                rebuildWithNewLanguage(newLocale, false);
            
            // when no error occurred until now, we can use this as save-file
            getTrolley().setHikeFile(hikeFile);
            // refresh the hike copy used to detect changes
            getTrolley().refreshHikeCopy();
        }
        catch (Exception e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(
                getFrame(), 
                e.toString(), 
                i18n("File Load Error"), 
                JOptionPane.ERROR_MESSAGE);
        System.err.println(e.toString());
    }

    
    private static class Item // Language JComboBox Item
    {
        public final String label;
        public final Locale locale;
        
        public Item(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}