package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
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
        final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        numberFormat.setGroupingUsed(false);
        final NumberFormatter numberFormatter = new NumberFormatter(numberFormat);
        numberFormatter.setValueClass(Integer.class); 
        numberFormatter.setAllowsInvalid(false);

        final JFormattedTextField field = new JFormattedTextField(numberFormatter);
        setTitleAndTooltip(title, tooltip, field);
        field.setValue(initialValue);
        
        return field;
    }
    
    public static int getNumberValue(JFormattedTextField numberField) {
        final int errorReturn = -1;
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

    public static DateField buildDateField(String title, String tooltip, Date initialValue) {
        final DateField field = new DateField(initialValue, "yyyy-MM-dd", "####-##-##");
        setTitleAndTooltip(title, tooltip, field);
        return field;
    }
    
    public static DateField buildTimeField(String title, String tooltip, Date initialValue) {
        final DateField field = new DateField(initialValue, "HH:mm", "##:##");
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
    
    /** Any contained component should lose focus on click onto given panel container. */
    public static void makeComponentFocusable(Container component) {
        if (component.getParent() instanceof JViewport) {
            makeComponentFocusable(component.getParent());
        }
        else {
            component.setFocusable(true);
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    component.requestFocusInWindow();
                }
            });
        }
    }

    
    private static void setTitleAndTooltip(String title, String tooltip, final JComponent field) {
        if (title != null)
            field.setBorder(BorderFactory.createTitledBorder(title));
        if (tooltip != null)
            field.setToolTipText(tooltip);
    }
    
    
    public static class DateField extends JFormattedTextField
    {
        private final SimpleDateFormat simpleDateFormat;

        /** 
         * @param date optional, initial value or null.
         * @param formatString matching SimpleDateFormat like "yyyy-MM-dd hh:mm"
         * @param formatMask matching MaskFormatter like "####-##-## ##:##"
         */
        public DateField(Date date, String formatString, String formatMask) {
            this.simpleDateFormat = new SimpleDateFormat(formatString);
            try {
                final MaskFormatter maskFormatter = new MaskFormatter(formatMask);
                setFormatterFactory(new DefaultFormatterFactory(maskFormatter));
                setDateValue(date);
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
            setColumns(formatMask.length());
        }
        
        public Date getDateValue() {
            try {
                commitEdit();
                return simpleDateFormat.parse(getText());
            }
            catch (ParseException e) {
                System.err.println(e.toString());
                return null;
            }
        }
        
        public void setDateValue(Date date) {
            final String text = simpleDateFormat.format((date != null) ? date : new Date());
            setText(text);
        }
    }
    
    
    private SwingUtil() {} // do not instantiate
}