package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import fri.servers.hiking.emergencyalert.persistence.entities.Day;
import fri.servers.hiking.emergencyalert.ui.swing.util.FileChooser;
import fri.servers.hiking.emergencyalert.ui.swing.util.ImageViewer;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;

class DayPanel extends JPanel
{
    private final SwingUtil.DateField plannedHomeDateField;
    private final SwingUtil.DateField plannedHomeTimeField;
    private final JTextArea routeField;
    private final JTable routeImagesField;
    private final FileChooser fileChooser;
    
    DayPanel(FileChooser fileChooser) {
        setLayout(new BorderLayout());
        
        this.fileChooser = fileChooser;

        routeField = SwingUtil.buildTextArea(
                i18n("A description of your hike path rescuers should be able to understand"), 
                true);
        routeField.setRows(6);
        
        routeImagesField = new JTable(buildTableModel());
        final JComponent imageTable = buildImagesTable(routeImagesField);
        
        plannedHomeDateField = SwingUtil.buildDateField(
                "* "+i18n("End Day"),
                i18n("Date when you will be home again"),
                null);
        plannedHomeTimeField = SwingUtil.buildTimeField(
                "* "+i18n("Time"),
                i18n("24-hour time when you will be home again, the first alert mail would be sent then"),
                null);
        
        // layout
        
        final JPanel timesPanel = new JPanel();
        timesPanel.setLayout(new BoxLayout(timesPanel, BoxLayout.Y_AXIS));
        
        final JPanel homePanel = new JPanel();
        final JLabel endLabel = new JLabel(i18n("Hike End"));
        homePanel.add(SwingUtil.increaseFontSize(SwingUtil.forceSize(endLabel, RouteAndTimesPage.labelSize), 140, true, false));
        homePanel.add(plannedHomeDateField);
        homePanel.add(plannedHomeTimeField);
        
        timesPanel.add(homePanel);
        
        final JSplitPane routePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        routePanel.setResizeWeight(0.5);
        routePanel.setTopComponent(SwingUtil.buildScrollPane("* "+i18n("Route Description"), routeField));
        routePanel.setBottomComponent(imageTable);
        final JPanel splitPanel = new JPanel(new BorderLayout());
        splitPanel.add(routePanel, BorderLayout.CENTER);
        
        final JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.add(timesPanel);
        all.add(splitPanel);
        
        add(all, BorderLayout.CENTER);
    }

    void populateUi(Day day) {
        if (StringUtil.isNotEmpty(day.getRoute()))
            routeField.setText(day.getRoute());
        
        routeImagesField.setModel(buildTableModel()); // remove all rows
        
        if (day.getRouteImages() != null)
            for (String imageFile : day.getRouteImages())
                addRouteImageToTable(imageFile);
        
        if (day.getPlannedHome() != null) {
            plannedHomeDateField.setDateValue(day.getPlannedHome());
            plannedHomeTimeField.setDateValue(day.getPlannedHome());
        }
        else {
            final Date nowPlus12 = DateUtil.addHours(DateUtil.now(), 12);
            plannedHomeDateField.setDateValue(nowPlus12);
            plannedHomeTimeField.setDateValue(nowPlus12);
        }
    }
    
    String validateFields() {
        if (noRouteNoImages())
            return i18n("The Route description must not be empty!");
        
        if (plannedHomeDateField.getDateValue() == null)
            return i18n("The planned end date must be given!");
        
        if (plannedHomeTimeField.getDateValue() == null)
            return i18n("The planned end time must be given!");
        
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    boolean commit(boolean goingForward, Day day) {
        if (StringUtil.isNotEmpty(routeField.getText()))
            day.setRoute(routeField.getText());
        
        final Vector<Vector> dataVector = getImagesFromTable();
        if (day.getRouteImages() == null)
            day.setRouteImages(new ArrayList<>());
        else
            day.getRouteImages().clear();
        
        for (int row = 0; row < dataVector.size(); row++) {
            final String imageFileName = (String) dataVector.get(row).get(0);
            final String imagePath = (String) dataVector.get(row).get(1);
            final String filePath =
                    imagePath+
                    (imagePath.endsWith(File.separator) ? "" : File.separator)+
                    imageFileName;
            day.getRouteImages().add(filePath);
        }
        
        final Date homeDate = plannedHomeDateField.getDateValue();
        final Date homeTime = plannedHomeTimeField.getDateValue();
        if (homeDate != null && homeTime != null)
            day.setPlannedHome(DateUtil.mergeDateAndTime(homeDate, homeTime));
        
        return true;
    }
    
    Date homeDateTime() {
        final Date homeDate = plannedHomeDateField.getDateValue();
        final Date homeTime = plannedHomeTimeField.getDateValue();
        return DateUtil.mergeDateAndTime(homeDate, homeTime);
    }

    @SuppressWarnings("rawtypes")
    boolean noRouteNoImages() {
        final Vector<Vector> dataVector = getImagesFromTable();
        final boolean noImages = (dataVector == null || dataVector.size() <= 0);
        final boolean noText = StringUtil.isEmpty(routeField.getText());
        return (noImages && noText);
    }
    
    Collection<? extends JComponent> getFocusValidationFields() {
        return List.of(
                routeField,
                routeImagesField,
                plannedHomeDateField,
                plannedHomeTimeField);
    }
    
    // privates
    
    @SuppressWarnings("rawtypes")
    private Vector<Vector> getImagesFromTable() {
        final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
        return model.getDataVector();
    }
    
    private JComponent buildImagesTable(JTable routeImagesField) {
        routeImagesField.getTableHeader().setReorderingAllowed(false);
        routeImagesField.setToolTipText(i18n("Attachments for any Alert Mail"));
        
        final JScrollPane scrollTable = new JScrollPane(routeImagesField);
        
        final JButton add = SwingUtil.getAddOrRemoveButton(
                true, // "+"
                i18n("Add an image file"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        chooseImageFile();
                    }
                });
        
        final JButton remove = SwingUtil.getAddOrRemoveButton(
                false, // "-"
                i18n("Remove an image file"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeSelectedImage();
                    }
                });
        remove.setEnabled(false);
        
        final JButton view = SwingUtil.getSmallButton(
                "\u2315", // magnifying glass
                i18n("View image file"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        viewSelectedImage();
                    }
                });
        view.setEnabled(false);
        
        routeImagesField.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        routeImagesField.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final boolean selectionExists = (e.getFirstIndex() >= 0 && e.getLastIndex() >= 0);
                remove.setEnabled(selectionExists);
                view.setEnabled(selectionExists);
            }
        });
        
        SwingUtil.makeComponentFocusable(routeImagesField);
        
        // layout
        
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(add);
        buttonsPanel.add(remove);
        buttonsPanel.add(view);
        
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(i18n("Route-Images")));
        panel.add(scrollTable, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void addRouteImageToTable(String imageFile) {
        final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
        final File file = new File(imageFile);
        final Vector<Object> newRow = new Vector<>();
        newRow.addElement(file.getName());
        newRow.addElement(file.getParent());
        model.addRow(newRow);
    }
    
    private TableModel buildTableModel() {
        final Vector<Object> columnNames = new Vector<>();
        columnNames.add(i18n("File Name"));
        columnNames.add(i18n("Path"));
        
        final Vector<Vector<Object>> data = new Vector<>();
        
        return new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // no cell editing
            }
        };
    }
    
    private void chooseImageFile() {
        final File[] files = fileChooser.open(false, null);
        if (files != null)
            for (File file : files)
                addRouteImageToTable(file.getAbsolutePath());
    }

    private void removeSelectedImage() {
        final int[] selectedIndexes = routeImagesField.getSelectedRows();
        if (selectedIndexes.length > 0) {
            final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
            for (int i = selectedIndexes.length - 1; i >= 0; i--)
                model.removeRow(selectedIndexes[i]);
        }
    }

    private void viewSelectedImage() {
        final int[] selectedIndexes = routeImagesField.getSelectedRows();
        if (selectedIndexes.length >= 0) {
            final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
            final File[] files = new File[selectedIndexes.length];
            for (int i = 0; i < selectedIndexes.length; i++) {
                final String fileName = (String) model.getValueAt(selectedIndexes[i], 0);
                final String parentPath = (String) model.getValueAt(selectedIndexes[i], 1);
                files[i] = new File(parentPath, fileName);
            }
            ImageViewer.showImages((JFrame) SwingUtilities.windowForComponent(this), files);
        }
    }
}