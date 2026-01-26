package fri.servers.hiking.emergencyalert.ui.swing.wizard.pages;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.ui.swing.util.FileChooser;
import fri.servers.hiking.emergencyalert.ui.swing.util.ImageViewer;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.util.DateUtil;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Route description text and image file chooser,
 * begin- and end-date/time of hike.
 */
public class RouteAndTimesPage extends AbstractWizardPage
{
    private JTextArea routeField;
    private JTable routeImagesField;
    private SwingUtil.DateField plannedBeginDateField;
    private SwingUtil.DateField plannedBeginTimeField;
    private SwingUtil.DateField plannedHomeDateField;
    private SwingUtil.DateField plannedHomeTimeField;
    
    private FileChooser fileChooser;

    @Override
    protected String getTitle() {
        return i18n("Times and Route");
    }
    
    @Override
    protected void buildUi() {
        fileChooser = new FileChooser(getContentPanel(), null);

        routeField = SwingUtil.buildTextArea(
                i18n("A description of your hike path rescuers should be able to understand"), 
                true);
        routeField.setRows(6);
        
        final JComponent imageTable = buildImagesTable();
        
        plannedBeginDateField = SwingUtil.buildDateField(
                i18n("Begin Day"),
                i18n("Date when your hike will start, given numerically as YEAR-MONTH-DAY"),
                null);
        plannedBeginTimeField = SwingUtil.buildTimeField(
                i18n("Time"),
                i18n("24-hour time when your hike will start, and the time observation will begin"),
                null);
        
        plannedHomeDateField = SwingUtil.buildDateField(
                i18n("End Day"),
                i18n("Date when you will be home again"),
                null);
        plannedHomeTimeField = SwingUtil.buildTimeField(
                i18n("Time"),
                i18n("24-hour time when you will be home again, the first alert mail would be sent then"),
                null);
        
        // layout
        
        final JPanel timesPanel = new JPanel();
        timesPanel.setLayout(new BoxLayout(timesPanel, BoxLayout.Y_AXIS));
        
        final Dimension labelSize = new Dimension(120, 24);
        
        final JPanel beginPanel = new JPanel();
        final JLabel beginLabel = new JLabel(i18n("Hike Begin"));
        beginPanel.add(increaseFontSize(Font.BOLD, 16, forceSize(beginLabel, labelSize)));
        beginPanel.add(plannedBeginDateField);
        beginPanel.add(plannedBeginTimeField);
        
        final JPanel homePanel = new JPanel();
        final JLabel endLabel = new JLabel(i18n("Hike End"));
        homePanel.add(increaseFontSize(Font.BOLD, 16, forceSize(endLabel, labelSize)));
        homePanel.add(plannedHomeDateField);
        homePanel.add(plannedHomeTimeField);
        
        timesPanel.add(beginPanel);
        timesPanel.add(homePanel);
        
        final JSplitPane routePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        routePanel.setResizeWeight(0.5);
        routePanel.setTopComponent(SwingUtil.buildScrollPane(i18n("Route Description"), routeField));
        routePanel.setBottomComponent(imageTable);
        final JPanel splitPanel = new JPanel(new BorderLayout());
        splitPanel.add(routePanel, BorderLayout.CENTER);
        
        final JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.add(timesPanel);
        all.add(Box.createRigidArea(new Dimension(1, 20)));
        all.add(splitPanel);
        
        getContentPanel().add(all, BorderLayout.CENTER);
        
        installFocusListeners();
    }

    @Override
    protected void populateUi(Hike hike) {
        if (StringUtil.isNotEmpty(hike.getRoute()))
            routeField.setText(hike.getRoute());
        
        routeImagesField.setModel(buildTableModel()); // remove all rows
        
        if (hike.getRouteImages() != null)
            for (String imageFile : hike.getRouteImages())
                addRouteImageToTable(imageFile);
        
        if (hike.getPlannedBegin() != null) {
            plannedBeginDateField.setDateValue(hike.getPlannedBegin());
            plannedBeginTimeField.setDateValue(hike.getPlannedBegin());
        }
        // else: they would have now as default
        
        if (hike.getPlannedHome() != null) {
            plannedHomeDateField.setDateValue(hike.getPlannedHome());
            plannedHomeTimeField.setDateValue(hike.getPlannedHome());
        }
        else {
            final Date now = DateUtil.now();
            plannedHomeDateField.setDateValue(now);
            plannedHomeTimeField.setDateValue(DateUtil.addHours(now, 12));
        }
    }
    
    @Override
    protected String validateFields() {
        if (StringUtil.isEmpty(routeField.getText()))
            return i18n("The Route description must not be empty!");
        
        if (plannedBeginDateField.getDateValue() == null)
            return i18n("The planned begin date must be given!");
        if (plannedBeginTimeField.getDateValue() == null)
            return i18n("The planned begin time must be given!");
        
        if (plannedHomeDateField.getDateValue() == null)
            return i18n("The planned end date must be given!");
        if (plannedHomeTimeField.getDateValue() == null)
            return i18n("The planned end time must be given!");
        
        return null;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    protected boolean commit(boolean goingForward) {
        final Hike hike = getHike();
        
        if (StringUtil.isNotEmpty(routeField.getText()))
            hike.setRoute(routeField.getText());
        
        final Vector<Vector> dataVector = getImagesFromTable();
        if (hike.getRouteImages() == null)
            hike.setRouteImages(new ArrayList<>());
        else
            hike.getRouteImages().clear();
        
        for (int row = 0; row < dataVector.size(); row++) {
            final String imageFileName = (String) dataVector.get(row).get(0);
            final String imagePath = (String) dataVector.get(row).get(1);
            final String filePath =
                    imagePath+
                    (imagePath.endsWith(File.separator) ? "" : File.separator)+
                    imageFileName;
            hike.getRouteImages().add(filePath);
        }
        
        final Date beginDate = plannedBeginDateField.getDateValue();
        final Date beginTime = plannedBeginTimeField.getDateValue();
        hike.setPlannedBegin(DateUtil.mergeDateAndTime(beginDate, beginTime));
        
        final Date homeDate = plannedHomeDateField.getDateValue();
        final Date homeTime = plannedHomeTimeField.getDateValue();
        hike.setPlannedHome(DateUtil.mergeDateAndTime(homeDate, homeTime));
        
        return true;
    }
    
    
    @SuppressWarnings("rawtypes")
    private Vector<Vector> getImagesFromTable() {
        final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
        return model.getDataVector();
    }
    
    private void addRouteImageToTable(String imageFile) {
        final DefaultTableModel model = (DefaultTableModel) routeImagesField.getModel();
        final File file = new File(imageFile);
        final Vector<Object> newRow = new Vector<>();
        newRow.addElement(file.getName());
        newRow.addElement(file.getParent());
        model.addRow(newRow);
    }
    
    private JComponent buildImagesTable() {
        routeImagesField = new JTable(buildTableModel());
        
        routeImagesField.getTableHeader().setReorderingAllowed(false);
        routeImagesField.setToolTipText(i18n("Attachments for any Alert Mail"));
        
        final JScrollPane scrollTable = new JScrollPane(routeImagesField);
        
        final JButton add = getAddOrRemoveButton(
                true, // "+"
                i18n("Add an image file"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        chooseImageFile();
                    }
                });
        
        final JButton remove = getAddOrRemoveButton(
                false, // "-"
                i18n("Remove an image file"),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeSelectedImage();
                    }
                });
        remove.setEnabled(false);
        
        final JButton view = getSmallButton(
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
        final File[] files = fileChooser.open();
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
            ImageViewer.showImages(getFrame(), files);
        }
    }

    
    private void installFocusListeners() {
        final FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validate();
            }
        };
        routeField.addFocusListener(focusListener);
        plannedBeginDateField.addFocusListener(focusListener);
        plannedBeginTimeField.addFocusListener(focusListener);
        plannedHomeDateField.addFocusListener(focusListener);
        plannedHomeTimeField.addFocusListener(focusListener);
    }
}