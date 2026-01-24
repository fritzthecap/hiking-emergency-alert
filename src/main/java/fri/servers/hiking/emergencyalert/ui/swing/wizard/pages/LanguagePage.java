package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;

/**
 * Choose your UI language.
 */
public class LanguagePage extends AbstractWizardPage
{
    private JComboBox<Item> languageChoiceField;
    
    @Override
    protected String getTitle() {
        return i18n("Language");
    }
    
    @Override
    protected void buildUi() {
        languageChoiceField = new JComboBox<Item>();
        
        languageChoiceField.addItem(new Item("English", Locale.ENGLISH));
        languageChoiceField.addItem(new Item("Deutsch", Locale.GERMAN));
        languageChoiceField.addItem(new Item("Français", Locale.FRENCH));
        languageChoiceField.addItem(new Item("Italiano", Locale.ITALIAN));
        languageChoiceField.addItem(new Item("Español", new Locale.Builder().setLanguage("es").build()));
        
        languageChoiceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(i18n("Language")),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        
        languageChoiceField.setPreferredSize(new Dimension(180, 70));
        
        getContentPanel().setLayout(new GridBagLayout()); // center
        getContentPanel().add(languageChoiceField);
        
        languageChoiceField.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    loadStringResources(getSelectedLocale());
            }
        });
    }
    
    @Override
    protected void populateUi(Hike hike) {
        final Locale selectedLocale = getSelectedLocale();
        final Locale hikeLocale = getHikeLocale(hike);
        if (hikeLocale != null && selectedLocale.equals(hikeLocale) == false) {
            for (int i = 0; i < languageChoiceField.getItemCount(); i++) {
                final Item item = languageChoiceField.getItemAt(i);
                if (item.locale.equals(hikeLocale))
                    languageChoiceField.setSelectedItem(item); // should trigger ItemListener
            }
        }
    }

    @Override
    protected boolean commit(boolean goingForward) {
        getHike().getAlert().setIso639Language(getSelectedLocale().getLanguage());
        return true;
    }
    

    private void loadStringResources(Locale locale) {
        throw new RuntimeException("Implement loading UI resources for "+locale.getDisplayName());
    }

    private Locale getSelectedLocale() {
        return ((Item) languageChoiceField.getSelectedItem()).locale;
    }
    
    private Locale getHikeLocale(Hike hike) {
        final String iso639Language = hike.getAlert().getIso639Language();
        try {
            return new Locale.Builder().setLanguage(iso639Language).build();
        }
        catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }
    
    
    private static class Item // JComboBox Item
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