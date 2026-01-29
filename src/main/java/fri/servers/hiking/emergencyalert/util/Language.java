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

    public static String getLanguage() {
        return (languageBundle != null) ? languageBundle.getLocale().getLanguage() : null;
    }

    public static Locale toLocale(String iso639Language) throws IllformedLocaleException {
        return new Locale.Builder().setLanguage(iso639Language).build();
    }

    public static String load()  {
        return load(Locale.getDefault());
    }
    
    public static String load(String iso639Language)  {
        try {
            final Locale locale = toLocale(iso639Language);
            return load(locale);
        }
        catch (Exception e) {
            System.err.println("ERROR: Loading language "+iso639Language+" failed, error was: "+e);
            return load();
        }
    }
    
    public static String load(Locale locale)  {
        if (locale == null)
            locale = Locale.ENGLISH;
        
        // the resource-bundle files must be in same package path as this class
        String className = Language.class.getName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        try {
            languageBundle = ResourceBundle.getBundle(packageName+"."+RESOURCE_FILE_BASENAME, locale);
            // if "user.language" is "de", this .... loads 
            // strings_de.properties instead of strings.properties for Locale.ENGLISH
            // when no strings_en.properties exists!
            // Thus you are forced to copy strings.properties to strings_en.properties ...
            // Found workaround: empty strings_en.properties solves the issue.
            // See https://hwellmann.blogspot.com/2010/02/misconceptions-about-java.html
        }
        catch (MissingResourceException e)  {
            System.err.println("ERROR: Could not load resource bundle for language "+locale.getLanguage()+", error was: "+e);
        }
        
        if (languageBundle == null)
            languageBundle = ResourceBundle.getBundle(packageName+"."+RESOURCE_FILE_BASENAME, locale = Locale.ENGLISH);
        
        return locale.getLanguage();
    }

    public static String i18n(String text) {
        final String key = replace(text);
        try {
            return languageBundle.getString(key);
        }
        catch (MissingResourceException e) {
            System.err.println("ERROR: Missing language-resource: >"+text+"<");
            return text;
        }
    }
    
    private static String replace(String i18nParameter) {
        return i18nParameter
                .replace(' ', '_')
                .replace("=", "_")
                .replace(":", "_"); // all other special characters seem to be allowed even without escaping!
    }
    
    private Language() {} // do not instantiate
}