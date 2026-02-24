package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** Some static UI improvements. */
public final class UiAdjustments
{
    /**
     * Reads system-property <code>-Dhike.fontPercent</code>.
     * @return the percent amount, or -1 if 100 or not existing.
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
     * <ul>
     * <li>JList font should not be bold</li>
     * <li>Size all UI-font-resources according to system-property
     *      <code>-Dhike.fontPercent</code> when between 80 and 140 percent</li>
     * <li>Set tooltip delay to 30 seconds</li>
     * </ul>
     */
    public static void adjust() {
        final UIDefaults uiDefaults = UIManager.getDefaults();
        
        final int fontPercent = getFontPercent();
        if (fontPercent != -1)
            adjustFontSizes(fontPercent, uiDefaults);
        
        final String key = "List.font";
        final Object listFontResource = uiDefaults.get(key);
        if (listFontResource instanceof FontUIResource) {
            final FontUIResource fontResource = (FontUIResource) listFontResource;
            final Font font = new Font("Dialog", Font.PLAIN, fontResource.getSize());
            
            UIManager.put(key, new FontUIResource(font));
        }
        
        // globally show tooltips for 25 seconds
        ToolTipManager.sharedInstance().setDismissDelay(30000);
    }
    
    /** This does not work for components that were sized explicitly! */
    private static void adjustFontSizes(int percentChange, UIDefaults uiDefaults) {
        final Enumeration<Object> keys = uiDefaults.keys();
        
        while (keys.hasMoreElements()) {
            final Object key = keys.nextElement();
            final Object value = UIManager.get(key);
            
            if (value instanceof FontUIResource) {
                final FontUIResource original = (FontUIResource) value;
                int adjustedFontSize = (int) Math.round((float) original.getSize() * (float) percentChange / 100.0);
                final Font font = new Font(original.getFontName(), original.getStyle(), adjustedFontSize);
                
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }
    
    private UiAdjustments() {} // do not instantiate
}