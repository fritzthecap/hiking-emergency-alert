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
import javax.swing.JCheckBox;
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
 * Choose your UI language.
 */
public class LanguagePage extends AbstractWizardPage
{
    private JComboBox<Item> languageChoiceField;
    private JCheckBox autoLoadSameFile;
    private FileChooser fileChooser;
    
    private final ItemListener languageSelectionListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED)
                loadStringResources(getSelectedLocale());
        }
    };

    @Override
    protected String getTitle() {
        return ""; //i18n("File and Language");
    }
    
    @Override
    protected void buildUi() {
        fileChooser = new FileChooser(getContentPanel(), null);    
        
        languageChoiceField = new JComboBox<Item>();
        
        languageChoiceField.addItem(new Item("English", Locale.ENGLISH));
        languageChoiceField.addItem(new Item("Deutsch", Locale.GERMAN));
        languageChoiceField.addItem(new Item("Français", Locale.FRENCH));
        languageChoiceField.addItem(new Item("Italiano", Locale.ITALIAN));
        languageChoiceField.addItem(new Item("Español", Language.toLocale("es")));
        
        languageChoiceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(i18n("Choose Your Language")),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        
        languageChoiceField.setPreferredSize(new Dimension(180, 70));
        
        languageChoiceField.addItemListener(languageSelectionListener);
        
        final JButton loadFile = new JButton(i18n("Choose Hike File"));
        loadFile.setToolTipText(i18n("If you don't load a file, the default file will be used for optionally saving your inputs"));
        loadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File[] hikeFile = fileChooser.open(true, "hike"); // extension
                if (hikeFile != null)
                    loadHike(hikeFile[0]);
            }
        });
        loadFile.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        final JCheckBox autoLoadSameFile = new JCheckBox(i18n("Next Time Load It Automatically"));
        autoLoadSameFile.setToolTipText(i18n("When activated, the inputs from the chosen file will be used next time"));
        autoLoadSameFile.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        // layout
        
        final JPanel loadFilePanel = new JPanel();
        loadFilePanel.setLayout(new BoxLayout(loadFilePanel, BoxLayout.Y_AXIS));
        loadFilePanel.add(Box.createVerticalGlue());
        loadFilePanel.add(loadFile);
        loadFilePanel.add(autoLoadSameFile);
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
    
    @Override
    protected void populateUi(Hike hike) {
        final Locale selectedLocale = getSelectedLocale();
        final Locale hikeLocale = getHikeLocale(hike);
        if (hikeLocale != null && selectedLocale.equals(hikeLocale) == false) {
            for (int i = 0; i < languageChoiceField.getItemCount(); i++) {
                final Item item = languageChoiceField.getItemAt(i);
                if (item.locale.equals(hikeLocale)) {
                    // to avoid recursion and stackoverflow we must remove the listener
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
    

    private void loadStringResources(Locale locale) {
        commit(true);
        
        Language.load(locale);
        
        getContentPanel().removeAll();
        buildUi();
        populateUi(getHike());
        
        getContentPanel().revalidate();
        getContentPanel().repaint();
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
    
    private void loadHike(File hikeFile) {
        try {
            final String hikeJson = new HikeFileManager().load(hikeFile.getAbsolutePath());
            final Hike hike = new JsonGsonSerializer<Hike>().fromJson(hikeJson, Hike.class);
            getStateMachine().getUserInterface().registerHike(hike);
            
            populateUi(hike);
            
            // when no error occurred until now, we can use this as save-file
            getTrolley().setHikeFile(hikeFile);
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