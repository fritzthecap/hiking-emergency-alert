package fri.servers.hiking.emergencyalert.util;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * UI internationalization.
 */
public final class Language
{
    private static final String RESOURCE_FILE_BASENAME = "strings"; // ".properties" will be appended by ResourceBundle
    
    private static ResourceBundle languageBundle;

    public static void load()  {
        load(Locale.getDefault());
    }
    
    public static void load(Locale locale)  {
        String className = Language.class.getName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        try {
            languageBundle = ResourceBundle.getBundle(packageName+"."+RESOURCE_FILE_BASENAME, locale);
        }
        catch (MissingResourceException e)  {
            System.err.println("Could not load resource bundle for language "+locale+", error was: "+e);
        }
        
        if (languageBundle == null)
            languageBundle = ResourceBundle.getBundle(packageName+"."+RESOURCE_FILE_BASENAME, Locale.ENGLISH);
    }

    public static String i18n(String text) {
        final String key = replace(text);
        return languageBundle.getString(key);
    }
    
    public static Locale toLocale(String iso639Language) throws IllformedLocaleException {
        return new Locale.Builder().setLanguage(iso639Language).build();
    }

    private static String replace(String i18nParameter) {
        return i18nParameter
                .replace(' ', '_')
                .replace("=", "_")
                .replace(":", "_"); // all other special characters seem to be allowed!
    }
    
    private Language() {} // do not instantiate
}