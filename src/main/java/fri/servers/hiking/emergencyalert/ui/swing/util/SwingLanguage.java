package fri.servers.hiking.emergencyalert.ui.swing.util;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import javax.swing.UIManager;

/**
 * Workaround for internationalization of JOptionPane button labels.
 */
public final class SwingLanguage
{
    public static void setJOptionPaneButtonLabels() {
        UIManager.put("OptionPane.cancelButtonText", i18n("Cancel"));
        UIManager.put("OptionPane.okButtonText", i18n("Ok"));
        UIManager.put("OptionPane.yesButtonText", i18n("Yes"));
        UIManager.put("OptionPane.noButtonText", i18n("No"));
    }

    private SwingLanguage() {} // do not instantiate
}
