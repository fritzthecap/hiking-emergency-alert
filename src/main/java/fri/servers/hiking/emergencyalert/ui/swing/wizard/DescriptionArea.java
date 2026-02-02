package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.util.Language;

/**
 * Left side of the split-pane, containing description texts for every wizard step.
 */
public class DescriptionArea
{
    private final JEditorPane descriptionArea;
    private final Map<String,String> descriptionCache = new HashMap<>();
    
    private JScrollPane scrollPane;
    private Class<? extends AbstractWizardPage> currentPageClass;
    
    public DescriptionArea() {
        this.descriptionArea = new JEditorPane();
        descriptionArea.setEditorKit(new HTMLEditorKit()); // .setContentType("text/html");
        descriptionArea.setEditable(false);
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
        String text = descriptionCache.get(cacheKey);
        
        if (text == null) {
            final String descriptionResourceName = cacheKey+".html";
            final InputStream stream = pageClass.getResourceAsStream(descriptionResourceName);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final StringBuilder sb = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null)
                    sb.append(line+"\n");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            text = sb.toString();
            descriptionCache.put(cacheKey, text);
        }
        
        descriptionArea.setText(text);
        descriptionArea.setCaretPosition(0);
    }
}