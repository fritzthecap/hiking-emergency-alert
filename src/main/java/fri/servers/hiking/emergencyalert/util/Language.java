package fri.servers.hiking.emergencyalert.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * UI internationalization.
 */
public final class Language
{
    private static ResourceBundle languageBundle;

    public static void load(Locale locale)  {
        String className = Language.class.getName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        try {
            languageBundle = ResourceBundle.getBundle(packageName+".strings", locale);
        }
        catch (MissingResourceException e)  {
            System.err.println("Could not load resource bundle for language "+locale+", error was: "+e);
        }
        
        if (languageBundle == null)
            languageBundle = ResourceBundle.getBundle(packageName+".strings", Locale.ENGLISH);
    }

    public static String i18n(String text) {
        return text;
        
//        text = text.replace(' ', '_');
//        text = text.replace('.', '_');
//        text = text.replace('-', '_');
//        text = text.replace(',', '_');
//        return languageBundle.getString(text);
    }
    
    private Language() {} // do not instantiate
}