package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import fri.servers.hiking.emergencyalert.persistence.entities.Day;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.EditableTabbedPane;
import fri.servers.hiking.emergencyalert.ui.swing.util.FileChooser;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * Begin-date/time of hike.
 * Multiple days with end-date/time, route description and image files.
 */
public class RouteAndTimesPage extends AbstractWizardPage
{
    static final Dimension labelSize = new Dimension(120, 24);
    
    private SwingUtil.DateField plannedBeginDateField;
    private SwingUtil.DateField plannedBeginTimeField;
    private EditableTabbedPane daysTabbedPane;
    private FileChooser fileChooser;

    @Override
    protected String getTitle() {
        return i18n("Times and Route");
    }
    
    @Override
    protected void buildUi() {
        if (fileChooser == null)
            fileChooser = new FileChooser(getContentPanel(), null);

        plannedBeginDateField = SwingUtil.buildDateField(
                i18n("Begin Day"),
                i18n("Date when your hike will start, given numerically as YEAR-MONTH-DAY"),
                null);
        plannedBeginTimeField = SwingUtil.buildTimeField(
                i18n("Time"),
                i18n("24-hour time when your hike will start, and the time observation will begin"),
                null);
        
        daysTabbedPane = new EditableTabbedPane(i18n("Add Day"), i18n("Remove Day")) {
            /** Overridden to renew focus listeners. */
            @Override
            public void addTab(String title, Component component) {
                super.addTab(title, component);
                if (getTabCount() > 1) // not only the "+" tab is present
                    installFocusValidation();
            }
            /** @return a new DayPanel. */
            @Override
            protected NewTab newTab(int tabIndex) {
                final DayPanel dayPanel = new DayPanel(fileChooser);
                dayPanel.populateUi(buildDay(tabIndex));
                return new NewTab(buildTabTitle(tabIndex), dayPanel);
            }
            /** Do not let remove first tab. */
            @Override
            protected boolean shouldAddCloseButtonAt(int index) {
                return (index != 0);
            }
            /** Ask user if removal is OK. */
            @Override
            protected void closeTab(String title, Component component) {
                if (confirmDayRemove(title)) {
                    super.closeTab(title, component);
                    reorganizeTabTitles();
                    installFocusValidation();
                }
            }
        };
        
        // layout
        
        final JPanel hikeBeginPanel = new JPanel();
        hikeBeginPanel.setLayout(new BoxLayout(hikeBeginPanel, BoxLayout.Y_AXIS));
        
        final JPanel beginPanel = new JPanel();
        final JLabel beginLabel = new JLabel(i18n("Hike Begin"));
        beginPanel.add(SwingUtil.increaseFontSize(SwingUtil.forceSize(beginLabel, labelSize), 140, true, false));
        beginPanel.add(plannedBeginDateField);
        beginPanel.add(plannedBeginTimeField);
        
        hikeBeginPanel.add(beginPanel);
        
        final JPanel beginTimeAndDays = new JPanel();
        beginTimeAndDays.setLayout(new BoxLayout(beginTimeAndDays, BoxLayout.Y_AXIS));
        beginTimeAndDays.add(hikeBeginPanel);
        beginTimeAndDays.add(daysTabbedPane);
        
        getContentPanel().add(beginTimeAndDays, BorderLayout.CENTER);
    }

    @Override
    protected void populateUi(Hike hike) {
        if (hike.getPlannedBegin() != null) {
            plannedBeginDateField.setDateValue(hike.getPlannedBegin());
            plannedBeginTimeField.setDateValue(hike.getPlannedBegin());
        }
        
        daysTabbedPane.removeAll();

        final List<Day> days = hike.getDays();
        for (int i = 0; i < days.size(); i++) {
            final DayPanel dayPanel = new DayPanel(fileChooser);
            daysTabbedPane.addTab(buildTabTitle(i), dayPanel);
            final Day day = days.get(i);
            dayPanel.populateUi(day);
        }
    }
    
    @Override
    protected String validateFields() {
        final List<Day> validationDays = new ArrayList<>();
        
        for (JComponent tab : daysTabbedPane.getTabs()) {
            final DayPanel dayPanel = (DayPanel) tab;
            
            final String error = dayPanel.validateFields();
            if (error != null) {
                daysTabbedPane.setSelectedComponent(dayPanel);
                return error;
            }
            
            final Day day = new Day();
            day.setPlannedHome(dayPanel.homeDateTime());
            validationDays.add(day); 
        }
        
        final Date beginDate = plannedBeginDateField.getDateValue();
        final Date beginTime = plannedBeginTimeField.getDateValue();
        final Date beginDateTime;
        if (beginDate != null && beginTime != null)
            beginDateTime = DateUtil.mergeDateAndTime(beginDate, beginTime);
        else
            beginDateTime = null;
        
        return validateHikeTimes(beginDateTime, validationDays);
    }

    @Override
    protected boolean commit(boolean goingForward) {
        final Hike hike = getHike();
        
        final List<JComponent> tabs = daysTabbedPane.getTabs();
        final List<Day> days = hike.getDays();
        
        while (days.size() > tabs.size())
            days.remove(days.size() - 1);
        
        while (days.size() < tabs.size())
            days.add(new Day());
        
        for (int i = 0; i < days.size(); i++) {
            final Day day = days.get(i);
            final DayPanel dayPanel = (DayPanel) tabs.get(i);
            
            dayPanel.commit(goingForward, day);
        }
        
        Date beginDate = plannedBeginDateField.getDateValue();
        final Date beginTime = plannedBeginTimeField.getDateValue();
        if (beginTime != null && beginDate == null) // take today as default
            beginDate = DateUtil.now();
        
        if (beginDate != null && beginTime != null)
            hike.setPlannedBegin(DateUtil.mergeDateAndTime(beginDate, beginTime));
        else
            hike.setPlannedBegin(null);
        
        return true;
    }
    

    private Day buildDay(int tabIndex) {
        final DayPanel firstDayPanel = (DayPanel) daysTabbedPane.getComponentAt(0); // there is always a first tab
        final Date firstDate = firstDayPanel.homeDateTime();
        final Date tabDate = DateUtil.addDays(firstDate, tabIndex);
        final Day day = new Day();
        day.setPlannedHome(tabDate);
        return day;
    }

    private boolean confirmDayRemove(String title) {
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                getFrame(),
                i18n("Do you really want to remove")+" "+title+" ?",
                i18n("Confirm Removal"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }
    
    private String buildTabTitle(int tabIndex) {
        return i18n("Day")+" "+(tabIndex + 1);
    }
    
    private void reorganizeTabTitles() {
        final List<JComponent> tabs = daysTabbedPane.getTabs();
        for (int i = 1; i < tabs.size(); i++)
            daysTabbedPane.setTitleAt(i, buildTabTitle(i));
    }
    
    private void installFocusValidation() {
        final List<JComponent> focusValidationFields = new ArrayList<>();
        for (JComponent tab : daysTabbedPane.getTabs()) {
            final DayPanel dayPanel = (DayPanel) tab;
            focusValidationFields.addAll(dayPanel.getFocusValidationFields());
        }
        focusValidationFields.add(plannedBeginDateField);
        focusValidationFields.add(plannedBeginTimeField);
        
        installFocusListener(
                focusValidationFields.toArray(new JComponent[focusValidationFields.size()]), 
                null);
    }
}