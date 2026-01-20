package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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
    private JComboBox<Item> languageChoice;
    
    @Override
    protected void buildUi() {
        languageChoice = new JComboBox<Item>();
        
        languageChoice.addItem(new Item("English", Locale.ENGLISH));
        languageChoice.addItem(new Item("Deutsch", Locale.GERMAN));
        languageChoice.addItem(new Item("Français", Locale.FRENCH));
        languageChoice.addItem(new Item("Italiano", Locale.ITALIAN));
        languageChoice.addItem(new Item("Español", new Locale.Builder().setLanguage("es").build()));
        
        languageChoice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(i18n("Language")),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        
        languageChoice.setPreferredSize(new Dimension(180, 70));
        
        getContentPanel().setLayout(new GridBagLayout()); // center
        getContentPanel().add(languageChoice);
    }
    
    @Override
    protected void populateUi(Hike hike) {
        if (isLocaleDifferent(hike)) {
            final Locale hikeLocale = getHikeLocale(hike);
            for (int i = 0; i < languageChoice.getItemCount(); i++) {
                final Item item = languageChoice.getItemAt(i);
                if (item.locale.equals(hikeLocale))
                    languageChoice.setSelectedItem(item);
            }
        }
    }
    
    @Override
    protected boolean commit() {
        final Locale locale = ((Item) languageChoice.getSelectedItem()).locale;
        getHike().getAlert().setIso639Language(locale.getLanguage());
        loadStringResources(locale);
        return true;
    }
    
    
    private void loadStringResources(Locale locale) {
        System.out.println("TODO: load UI resources for "+locale.getDisplayName()); // TODO
    }

    private Locale getHikeLocale(Hike hike) {
        final String iso639Language = hike.getAlert().getIso639Language();
        try {
            return new Locale.Builder().setLanguage(iso639Language).build();
        }
        catch (Exception e) {
            System.err.println(e.toString());
            return Locale.ENGLISH;
        }
    }
    
    private Locale getSelectedLocale() {
        return ((Item) languageChoice.getSelectedItem()).locale;
    }
    
    private boolean isLocaleDifferent(Hike hike) {
        return getSelectedLocale().equals(getHikeLocale(hike)) == false;
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