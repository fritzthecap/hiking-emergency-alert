package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** Global font size Swing convenience, to be called initially. */
public class FontSizer
{
    /**
     * Reads system-property <code>-Dhike.fontPercent</code>.
     */
    public static int getFontPercent() {
        final String fontPercentProperty = System.getProperty("hike.fontPercent");
        if (fontPercentProperty != null) {
            try {
                final int fontPercent = Integer.valueOf(fontPercentProperty);
                if (fontPercent != 100 && fontPercent >= 80 && fontPercent <= 140)
                    return fontPercent;
            }
            catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        return -1;
    }

    /**
     * Reads system-property <code>-Dhike.fontPercent</code> 
     * and sizes fonts when between 0 and 140 percent.
     */
    public static void checkFontSize() {
        final int fontPercent = getFontPercent();
        if (fontPercent != -1)
            FontSizer.changeFontSize(fontPercent);
    }

    /** This does not work for components that were sized explicitly! */
    private static void changeFontSize(int percentChange) {
        final Enumeration<Object> keys = UIManager.getDefaults().keys();
        
        while (keys.hasMoreElements()) {
            final Object key = keys.nextElement();
            final Object value = UIManager.get(key);
            
            if (value instanceof FontUIResource) {
                final FontUIResource original = (FontUIResource) value;
                int fontSize = original.getSize();
                int changedFontSize = (int) Math.round((float) fontSize * (float) percentChange / 100.0);
                
                final Font font = new Font(original.getFontName(), original.getStyle(), changedFontSize);
                
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }
}