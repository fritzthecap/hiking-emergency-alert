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
        field.setToolTipText(tooltip);
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
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createTitledBorder(title));
        field.setValue(initial);
        
        return field;
    }
    
    public static JComboBox<String> buildComboBox(String title, String tooltip, String[] values) {
        final JComboBox<String> combo = new JComboBox<>(values);
        combo.setEditable(true);
        combo.setBorder(BorderFactory.createTitledBorder(title));
        combo.setToolTipText(tooltip);
        return combo;
    }

    
    private SwingUtil() {} // do not instantiate
}