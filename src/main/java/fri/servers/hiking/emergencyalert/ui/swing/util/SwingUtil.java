package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public final class SwingUtil
{
    public static JTextField buildTextField(String title, String tooltip, String initialValue) {
        final JTextField field = new JTextField();
        setTitleAndTooltip(title, tooltip, field);
        if (initialValue != null)
            field.setText(initialValue);
        
        return field;
    }
    
    public static JFormattedTextField buildNumberField(String title, String tooltip, int initialValue) {
        final NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        final NumberFormatter numberFormatter = new NumberFormatter(format);
        numberFormatter.setValueClass(Integer.class); 
        numberFormatter.setAllowsInvalid(false);

        final JFormattedTextField field = new JFormattedTextField(numberFormatter);
        setTitleAndTooltip(title, tooltip, field);
        field.setValue(initialValue);
        
        return field;
    }
    
    public static int getValue(JFormattedTextField numberField) {
        final int errorReturn = 1;
        try {
            numberField.commitEdit();
        }
        catch (ParseException e) {
            System.err.println(e.toString());
            return errorReturn;
        }
        final Object value = numberField.getValue();
        return (value == null) ? errorReturn : ((Integer) value).intValue();
    }
    
    public static JComboBox<String> buildComboBox(String title, String tooltip, String[] values) {
        final JComboBox<String> field = new JComboBox<>(values);
        field.setEditable(true);
        setTitleAndTooltip(title, tooltip, field);
        
        return field;
    }

    public static JTextArea buildTextArea(String title, String tooltip, String initialValue) {
        final JTextArea field = new JTextArea();
        setTitleAndTooltip(title, tooltip, field);
        if (initialValue != null)
            field.setText(initialValue);
        
        return field;
    }

    
    private static void setTitleAndTooltip(String title, String tooltip, final JComponent field) {
        if (title != null)
            field.setBorder(BorderFactory.createTitledBorder(title));
        if (tooltip != null)
            field.setToolTipText(tooltip);
    }
    
    
    private SwingUtil() {} // do not instantiate
}