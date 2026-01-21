package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public final class SwingUtil
{
    public static JTextField buildTextField(String title, String tooltip, String defaultValue) {
        final JTextField field = new JTextField();
        if (tooltip != null)
            field.setToolTipText(tooltip);
        if (title != null)
            field.setBorder(BorderFactory.createTitledBorder(title));
        if (defaultValue != null)
            field.setText(defaultValue);
        return field;
    }
    
    public static JFormattedTextField buildNumberField(String title, String tooltip, int initial) {
        final NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);

        final NumberFormatter numberFormatter = new NumberFormatter(format);
        numberFormatter.setValueClass(Integer.class); 
        numberFormatter.setAllowsInvalid(false);

        final JFormattedTextField field = new JFormattedTextField(numberFormatter);
        if (tooltip != null)
            field.setToolTipText(tooltip);
        if (title != null)
            field.setBorder(BorderFactory.createTitledBorder(title));
        field.setValue(initial);
        
        return field;
    }
    
    public static JComboBox<String> buildComboBox(String title, String tooltip, String[] values) {
        final JComboBox<String> combo = new JComboBox<>(values);
        combo.setEditable(true);
        if (title != null)
            combo.setBorder(BorderFactory.createTitledBorder(title));
        if (tooltip != null)
            combo.setToolTipText(tooltip);
        return combo;
    }

    
    private SwingUtil() {} // do not instantiate
}