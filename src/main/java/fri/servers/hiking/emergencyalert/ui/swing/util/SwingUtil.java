package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public final class SwingUtil
{
//    public static <C extends Component> C forceSize(C component, Dimension size) {
//        component.setPreferredSize(size);
//        component.setMinimumSize(size);
//        component.setMaximumSize(size);
//        return component;
//    }

    public static JTextField buildTextField(String title, String tooltip, String defaultValue) {
        final JTextField field = new JTextField();
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createTitledBorder(title));
        if (defaultValue != null)
            field.setText(defaultValue);
        return field;
    }
    
    public static JFormattedTextField buildNumberField(String title, String tooltip, int initial, String mask) {
        final NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);

        final NumberFormatter numberFormatter = new NumberFormatter(format);
        numberFormatter.setValueClass(Integer.class); 
        numberFormatter.setAllowsInvalid(false);

        final JFormattedTextField field = new JFormattedTextField(numberFormatter);
        field.setEditable(true);
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createTitledBorder(title));
        return field;
    }
    
    private SwingUtil() {} // do not instantiate
}