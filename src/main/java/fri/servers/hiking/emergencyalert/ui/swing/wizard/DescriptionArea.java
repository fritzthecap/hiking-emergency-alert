package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import fri.servers.hiking.emergencyalert.ui.swing.util.FontSizer;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.util.Language;

/**
 * Left side of the split-pane, containing description texts for every wizard step.
 */
public class DescriptionArea
{
    /** Swing sizes of HTML elements used in description texts, empirical. */
    private static final Map<String,Integer> fontSizes = Map.of(
            "h2", 18, // header
            "p",  14, // paragraph
            "li", 14 // list element
        );

    private final JEditorPane descriptionArea;
    
    private JScrollPane scrollPane;
    private Class<? extends AbstractWizardPage> currentPageClass;
    
    public DescriptionArea() {
        this.descriptionArea = new JEditorPane();
        descriptionArea.setEditable(false);
        
        final HtmlEditorKitWithLocalStyles editorKit = new HtmlEditorKitWithLocalStyles();
        final StyleSheet localStyleSheet = new StyleSheet();
        localStyleSheet.addStyleSheet(editorKit.getGlobalStyleSheet()); // merge JDK default styles into empty sheet
        editorKit.setStyleSheet(localStyleSheet);
        
        final int fontPercent = FontSizer.getFontPercent();
        if (fontPercent != -1) {
            for (Map.Entry<String,Integer> fontSize : fontSizes.entrySet()) {
                final int scaledFontSize = (int) Math.round((double) fontSize.getValue() * (double) fontPercent / 100.0);
                final String ruleString = fontSize.getKey()+" { font-size : "+scaledFontSize+" }";
                localStyleSheet.addRule(ruleString);
            }
        }

        descriptionArea.setEditorKit(editorKit); // instead of setContentType("text/html")
        
        descriptionArea.setPreferredSize(new Dimension(260, 260));
    }
    
    public JComponent getAddablePanel() {
        if (scrollPane == null)
            scrollPane = SwingUtil.buildScrollPane(i18n("Description"), descriptionArea);
        return scrollPane;
    }
    
    public void refreshLanguage() {
        loadTextFor(currentPageClass);
        scrollPane.setBorder(BorderFactory.createTitledBorder(i18n("Description")));
    }
    
    public void loadTextFor(Class<? extends AbstractWizardPage> pageClass) {
        currentPageClass = pageClass;
        
        final String cacheKey = Language.getLanguage()+"/"+pageClass.getSimpleName();
        final String descriptionResourceName = cacheKey+".html";
        final URL url = pageClass.getResource(descriptionResourceName);
        try {
            final PropertyChangeListener loadFinishedListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event)   {
                    if (event.getPropertyName().equals("page")) { // no constant exists for this!
                        descriptionArea.removePropertyChangeListener(this); // stop listening
                        descriptionArea.setCaretPosition(0);
                    }
                }
            };
            descriptionArea.addPropertyChangeListener(loadFinishedListener);

            descriptionArea.setPage(url);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * HTMLEditorKit with private local CSS styles.
     * Bugfix for global styles in AppContext that affect even JLabel HTML texts.
     * @see https://stackoverflow.com/questions/43408539/how-does-one-properly-initialize-a-jtextpane-stylesheet-so-no-other-html-enable
     */
    private static class HtmlEditorKitWithLocalStyles extends HTMLEditorKit
    {
        private StyleSheet styleSheet;
        
        /** Overridden to return the private local style-sheet. */
        @Override
        public StyleSheet getStyleSheet() {
            return styleSheet;
        }
        
        /** Overridden to set the private local style-sheet. */
        @Override
        public void setStyleSheet(StyleSheet styleSheet) {
            this.styleSheet = styleSheet;
        }
        
        /** Delivers static global JDK styles. */
        StyleSheet getGlobalStyleSheet() {
            return super.getStyleSheet();
        }
    }
}