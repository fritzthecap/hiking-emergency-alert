package fri.servers.hiking.emergencyalert.ui.swing.wizardpages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;

/**
 * Choose your UI language.
 */
public class LanguagePage extends AbstractWizardPage
{
    private JComboBox<Item> languageChoice;
    
    @Override
    protected AbstractWizardPage nextPage() {
        return new MailConfigurationPage();
    }
    
    @Override
    public AbstractWizardPage getNextPage() {
        final Locale locale = ((Item) languageChoice.getSelectedItem()).locale;
        loadStringResources(locale);
        return super.getNextPage();
    }
    
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
        
        setLayout(new GridBagLayout()); // center
        add(languageChoice);
    }
    
    private void loadStringResources(Locale locale) {
        System.out.println("TODO: load UI resources for "+locale.getDisplayName());
    }

    
    private static class Item
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